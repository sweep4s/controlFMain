package com.controlf.service;

import com.controlf.db.repository.CalificacionRepository;
import com.controlf.db.repository.ComentarioRepository;
import com.controlf.db.repository.LeyRepository;
import com.controlf.db.repository.UsuarioRepository;
import com.controlf.db.repository.VotoRepository;
import com.controlf.db.schema.Calificacion;
import com.controlf.db.schema.Comentario;
import com.controlf.db.schema.Ley;
import com.controlf.db.schema.Usuario;
import com.controlf.db.schema.Voto;
import com.controlf.db.schema.enums.EstadoLey;
import com.controlf.db.schema.enums.TipoVoto;
import com.controlf.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeyService {

    private final LeyRepository leyRepository;
    private final VotoRepository votoRepository;
    private final CalificacionRepository calificacionRepository;
    private final ComentarioRepository comentarioRepository;
    private final UsuarioRepository usuarioRepository;
    private final com.controlf.db.repository.PoliticoRepository politicoRepository;
    private final GeminiService geminiService;

    private static final int CALIFICACION_MAXIMA = 5;

    public FiltrosLeyDTO getFiltros() {
        return buildFiltrosLeyDTO();
    }

  public GrillaLeyesDTO getLeyesFiltradas(int pagina, int size, String termino, String categoria, String estado) {
    try {
        org.springframework.data.jpa.domain.Specification<Ley> spec = (root, query, cb) -> cb.conjunction();

        if (termino != null && !termino.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("titulo")), "%" + termino.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("codigo")), "%" + termino.toLowerCase() + "%")
            ));
        }

        if (categoria != null && !categoria.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("categoria"), categoria));
        }

        if (estado != null && !estado.isEmpty()) {
            try {
                spec = spec.and((root, query, cb) ->
                        cb.equal(root.get("estado"), EstadoLey.valueOf(estado.toUpperCase())));
            } catch (IllegalArgumentException e) {
                // Estado inválido, ignorar filtro
            }
        }

        org.springframework.data.domain.Page<Ley> page = leyRepository.findAll(
                spec,
                org.springframework.data.domain.PageRequest.of(
                        Math.max(0, pagina - 1),
                        size,
                        org.springframework.data.domain.Sort.by(
                                org.springframework.data.domain.Sort.Direction.DESC,
                                "id"
                        )
                )
        );

        List<ExpedienteLegislativoDTO> leyes = page.getContent().stream()
                .map(LeyService::mapToExpedienteDTO)
                .collect(Collectors.toList());

        return GrillaLeyesDTO.builder()
                .id("grilla-leyes")
                .leyes(leyes)
                .paginaActual(pagina)
                .totalPaginas(Math.max(1, page.getTotalPages()))
                .build();

    } catch (Exception e) {
        return GrillaLeyesDTO.builder()
                .id("grilla-leyes")
                .leyes(java.util.List.of())
                .paginaActual(1)
                .totalPaginas(1)
                .build();
    }
}
    public PerfilLeyDTO getFullPerfilLey(Integer id) {
        return PerfilLeyDTO.builder()
                .contenido(getContenidoLey(id))
                .votacion(getResultadoVotacion(id))
                .auditoria(getAuditoriaCoherencia(id))
                .debate(getDebateCiudadano(id))
                .votingMatchSummary(getVotingMatchSummary(id))
                .build();
    }

    public List<ExpedienteLegislativoDTO> getAllLeyesAsExpedientes() {
        return leyRepository.findAll().stream()
                .map(LeyService::mapToExpedienteDTO)
                .collect(Collectors.toList());
    }

    /**
     * Agenda / calendario legislativo (CF-013). Construye eventos cronológicos a partir de datos
     * reales: el ingreso de cada expediente y la última votación registrada por ley.
     */
    public AgendaLegislativaDTO getAgendaLegislativa() {
        List<EventoAgendaDTO> eventos = new ArrayList<>();
        long ingresos = 0;
        long votaciones = 0;

        for (Ley ley : leyRepository.findAll()) {
            String estado = ley.getEstado() != null ? ley.getEstado().name() : null;

            if (ley.getFechaIngreso() != null) {
                eventos.add(EventoAgendaDTO.builder()
                        .tipo("INGRESO_LEY")
                        .fecha(ley.getFechaIngreso().toString())
                        .titulo(ley.getTitulo())
                        .detalle("Ingreso del expediente legislativo")
                        .categoria(ley.getCategoria())
                        .estado(estado)
                        .leyId(ley.getId().toString())
                        .build());
                ingresos++;
            }

            List<Voto> votos = votoRepository.findByLeyId(ley.getId());
            LocalDateTime ultimaVotacion = votos.stream()
                    .map(Voto::getFechaVoto)
                    .filter(Objects::nonNull)
                    .max(LocalDateTime::compareTo)
                    .orElse(null);

            if (ultimaVotacion != null) {
                eventos.add(EventoAgendaDTO.builder()
                        .tipo("VOTACION")
                        .fecha(ultimaVotacion.toLocalDate().toString())
                        .titulo(ley.getTitulo())
                        .detalle("Votación registrada en el pleno")
                        .categoria(ley.getCategoria())
                        .estado(estado)
                        .leyId(ley.getId().toString())
                        .conteoVotos((long) votos.size())
                        .build());
                votaciones++;
            }
        }

        // Orden cronológico descendente (las fechas ISO yyyy-MM-dd ordenan lexicográficamente).
        eventos.sort((a, b) -> b.getFecha().compareTo(a.getFecha()));

        return AgendaLegislativaDTO.builder()
                .eventos(eventos)
                .totalEventos(eventos.size())
                .totalIngresos(ingresos)
                .totalVotaciones(votaciones)
                .build();
    }

    /**
     * Seguimiento a debates y transcripciones (CF-014). Devuelve las leyes con su texto oficial
     * (transcripción / exposición de motivos) y su resumen simplificado, junto al resultado de la
     * votación. Se puede filtrar por estado ("EN_DEBATE" agrupa DEBATE y EN_DEBATE).
     */
    public List<DebateLegislativoDTO> getDebatesLegislativos(String estadoFiltro) {
        return leyRepository.findAll().stream()
                .filter(ley -> coincideEstadoDebate(ley, estadoFiltro))
                .sorted((a, b) -> {
                    LocalDate fa = a.getFechaIngreso();
                    LocalDate fb = b.getFechaIngreso();
                    if (fa == null && fb == null) return 0;
                    if (fa == null) return 1;
                    if (fb == null) return -1;
                    return fb.compareTo(fa);
                })
                .map(this::mapToDebateDTO)
                .collect(Collectors.toList());
    }

    private boolean coincideEstadoDebate(Ley ley, String estadoFiltro) {
        if (estadoFiltro == null || estadoFiltro.isBlank() || estadoFiltro.equalsIgnoreCase("TODOS")) {
            return true;
        }
        if (estadoFiltro.equalsIgnoreCase("EN_DEBATE")) {
            return ley.getEstado() == EstadoLey.DEBATE || ley.getEstado() == EstadoLey.EN_DEBATE;
        }
        try {
            return ley.getEstado() == EstadoLey.valueOf(estadoFiltro.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return true;
        }
    }

    private DebateLegislativoDTO mapToDebateDTO(Ley ley) {
        long favor = votoRepository.countByLeyIdAndTipoVoto(ley.getId(), TipoVoto.FAVOR);
        long contra = votoRepository.countByLeyIdAndTipoVoto(ley.getId(), TipoVoto.CONTRA);
        long abstencion = votoRepository.countByLeyIdAndTipoVoto(ley.getId(), TipoVoto.ABSTENCION);

        return DebateLegislativoDTO.builder()
                .leyId(ley.getId().toString())
                .titulo(ley.getTitulo())
                .codigo(ley.getCodigo())
                .estado(ley.getEstado() != null ? ley.getEstado().name() : "SIN ESTADO")
                .categoria(ley.getCategoria())
                .proponente(ley.getProponente())
                .fechaIngreso(ley.getFechaIngreso() != null ? ley.getFechaIngreso().toString() : null)
                .resumenOficial(ley.getDescripcionOriginal())
                .resumenSimplificado(ley.getDescripcionSimplificada())
                .votosFavor(favor)
                .votosContra(contra)
                .votosAbstencion(abstencion)
                .totalVotos(favor + contra + abstencion)
                .build();
    }

    @Transactional
    public void actualizarCategoriaLey(Integer leyId, CategoriaLeyRequestDTO request) {
        Ley ley = leyRepository.findById(leyId).orElseThrow();
        ley.setCategoria(request.getCategoria());
        leyRepository.save(ley);
    }

    @Transactional
    public void actualizarEstadoLey(Integer leyId, EstadoLeyRequestDTO request) {
        Ley ley = leyRepository.findById(leyId).orElseThrow();
        ley.setEstado(EstadoLey.valueOf(request.getEstado().toUpperCase(Locale.ROOT)));
        leyRepository.save(ley);
    }

    @Transactional
    public void actualizarAsistenciaVoto(Integer leyId, Integer votoId, AsistenciaVotoRequestDTO request) {
        Ley ley = leyRepository.findById(leyId).orElseThrow();
        Voto voto = votoRepository.findById(votoId).orElseThrow();
        if (!ley.getId().equals(voto.getLey().getId())) {
            throw new IllegalArgumentException("El voto no pertenece a la ley indicada");
        }
        voto.setAsistencia(request.getAsistencia());
        votoRepository.save(voto);
    }

    public void addComentario(Integer leyId, ComentarioRequestDTO request, Integer currentUserId) {
        Ley ley = leyRepository.findById(leyId).orElseThrow();
        Usuario u = usuarioRepository.findById(currentUserId).orElseThrow();

        Comentario c = new Comentario();
        c.setTexto(request.getTexto());
        c.setUsuario(u);
        c.setFecha(LocalDateTime.now());
        c.setEsBasadoEnHechos(false);
        c.setPuntaje(request.getPuntaje());

        comentarioRepository.save(c);
        ley.getComentarios().add(c);
        leyRepository.save(ley);
    }

    public void addCalificacion(Integer leyId, CalificacionRequestDTO request, Integer currentUserId) {
        Ley ley = leyRepository.findById(leyId).orElseThrow();
        Usuario u = usuarioRepository.findById(currentUserId).orElseThrow();

        Calificacion cal = new Calificacion();
        cal.setPuntaje(request.getPuntaje());
        cal.setUsuario(u);
        cal.setFecha(LocalDateTime.now());

        calificacionRepository.save(cal);
        ley.getCalificaciones().add(cal);
        leyRepository.save(ley);
    }

    public ContenidoLeyDTO getContenidoLey(Integer leyId) {
        Ley ley = leyRepository.findById(leyId).orElseThrow();
        return ContenidoLeyDTO.builder()
                .id(ley.getId().toString())
                .titulo(ley.getTitulo())
                .resumenEjecutivo(ley.getDescripcionSimplificada())
                .impactoSocial(ley.getImpactoSocial())
                .estado(ley.getEstado() != null ? ley.getEstado().name() : "SIN ESTADO")
                .categoria(ley.getCategoria())
                .build();
    }

    @Transactional
    public ContenidoLeyDTO explicarLey(Integer id) {
        Ley ley = leyRepository.findById(id).orElseThrow();
        
        if (ley.getDescripcionSimplificada() != null && !ley.getDescripcionSimplificada().isBlank()) {
            return getContenidoLey(id);
        }
        
        String explicacion = geminiService.generarExplicacion(ley.getTitulo(), ley.getDescripcionOriginal());
        ley.setDescripcionSimplificada(explicacion);
        leyRepository.save(ley);
        
        return getContenidoLey(id);
    }


    public DebateCiudadanoDTO getDebateCiudadano(Integer leyId) {
        Ley ley = leyRepository.findById(leyId).orElseThrow();
        Double avg = calificacionRepository.findAveragePuntajeByLeyId(leyId);

        return DebateCiudadanoDTO.builder()
                .id(ley.getId().toString())
                .titulo("Debate Ciudadano: " + ley.getTitulo())
                .puntuacionPromedio(avg != null ? avg : 0.0)
                .puntuacionMaxima(CALIFICACION_MAXIMA)
                .comentarios(ley.getComentarios().stream().filter(PoliticoService::esComentarioPublico).map(c -> ComentarioDebateDTO.builder()
                        .id(c.getId().toString())
                        .usuario(c.getUsuario().getNombre())
                        .mensaje(c.getTexto())
                        .fecha(c.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                        .avatarUrl(c.getUsuario().getAvatarUrl())
                        .puntaje(c.getPuntaje())
                        .build()).collect(Collectors.toList()))
                .placeholderComentario("Escribe tu opinión sobre esta ley...")
                .tieneBotonEnviar(true)
                .build();
    }

    public AuditoriaCoherenciaDTO getAuditoriaCoherencia(Integer leyId) {
        Ley ley = leyRepository.findById(leyId).orElseThrow();
        return AuditoriaCoherenciaDTO.builder()
                .id(ley.getId().toString())
                .titulo("Auditoría de Coherencia")
                .subtitulo("Cruce de datos para: " + ley.getTitulo())
                .filas(ley.getVinculos().stream().map(v -> FilaAuditoriaDTO.builder()
                        .id(v.getPromesa().getPolitico().getId().toString())
                        .nombre(v.getPromesa().getPolitico().getNombreCompleto())
                        .fotoUrl(v.getPromesa().getPolitico().getFotoUrl())
                        .bloque(v.getPromesa().getPolitico().getPartidoPolitico())
                        .voto(findVotoForPolitico(ley, v.getPromesa().getPolitico()))
                        .analisisCoherencia(v.getAnalisisCoherencia())
                        .nivelCoherencia(v.getNivelCoherencia().name().toLowerCase())
                        .build()).collect(Collectors.toList()))
                .textoVerMas("Ver detalles de la metodología de auditoría")
                .build();
    }

    private String findVotoForPolitico(Ley ley, com.controlf.db.schema.Politico p) {
        return ley.getVotos().stream()
                .filter(v -> v.getPolitico().getId().equals(p.getId()))
                .map(v -> v.getTipoVoto().name())
                .findFirst()
                .orElse("N/A");
    }

    public ResultadoVotacionDTO getResultadoVotacion(Integer leyId) {
        Ley ley = leyRepository.findById(leyId).orElseThrow();
        long favor = votoRepository.countByLeyIdAndTipoVoto(leyId, TipoVoto.FAVOR);
        long contra = votoRepository.countByLeyIdAndTipoVoto(leyId, TipoVoto.CONTRA);
        long abstencion = votoRepository.countByLeyIdAndTipoVoto(leyId, TipoVoto.ABSTENCION);
        long total = favor + contra + abstencion;

        return ResultadoVotacionDTO.builder()
                .id(ley.getId().toString())
                .titulo("Resultado Votación: " + ley.getTitulo())
                .votosFavor(favor)
                .votosContra(contra)
                .votosAbstencion(abstencion)
                .valorPrincipal((double) favor)
                .unitadPrincipal("Votos")
                .escalaMinima(0)
                .escalaMedia((int) (total > 0 ? total / 2 : 0))
                .escalaMaxima((int) total)
                .tieneMenuOpciones(true)
                .build();
    }

    @Transactional
    public ImportResultDTO importVotingDetailVotes(Integer leyId) {
        Ley ley = leyRepository.findById(leyId).orElseThrow();
        if (ley.getExternalId() == null) {
            return new ImportResultDTO(0, 0, 0, 0);
        }

        java.util.List<VotingDetailDTO> details;
        try {
            details = loadVotingDetailEntries(ley.getExternalId());
        } catch (Exception e) {
            throw new RuntimeException("No se pudo cargar el detalle de votación externa: " + e.getMessage(), e);
        }

        int total = details.size();
        int imported = 0;
        int ignored = 0;
        int duplicates = 0;

        // La fecha de la votación es la de la ley (todos los votos de un mismo
        // expediente ocurren en la misma sesión). El detalle externo no trae fecha
        // por votante, así que se usa fechaIngreso; sólo si faltara se cae a "ahora".
        LocalDateTime fechaVotacion = ley.getFechaIngreso() != null
                ? ley.getFechaIngreso().atStartOfDay()
                : LocalDateTime.now();

        for (VotingDetailDTO detail : details) {
            String fullName = buildFullName(detail.getFirstName(), detail.getLastname());
            if (fullName.isEmpty()) {
                ignored++;
                continue;
            }

            var politicoOpt = politicoRepository.findByNombreCompletoContainingIgnoreCase(fullName);
            if (politicoOpt.isEmpty()) {
                ignored++;
                continue;
            }

            var politico = politicoOpt.get();
            if (votoRepository.existsByPoliticoIdAndLeyId(politico.getId(), ley.getId())) {
                duplicates++;
                continue;
            }

            Voto voto = new Voto();
            voto.setPolitico(politico);
            voto.setLey(ley);
            voto.setTipoVoto(mapVote(detail.getDescription()));
            voto.setAsistencia(true);
            voto.setFechaVoto(fechaVotacion);
            votoRepository.save(voto);
            imported++;
        }

        return new ImportResultDTO(total, imported, ignored, duplicates);
    }

    public VotingMatchSummaryDTO getVotingMatchSummary(Integer leyId) {
        Ley ley = leyRepository.findById(leyId).orElseThrow();
        if (ley.getExternalId() == null) {
            return VotingMatchSummaryDTO.builder()
                    .found(0)
                    .notFound(0)
                    .total(0)
                    .build();
        }

        try {
            java.util.List<VotingDetailDTO> details = loadVotingDetailEntries(ley.getExternalId());
            int total = details.size();
            int found = 0;

            for (VotingDetailDTO detail : details) {
                String fullName = buildFullName(detail.getFirstName(), detail.getLastname());
                if (fullName.isEmpty()) {
                    continue;
                }
                if (politicoRepository.findByNombreCompletoContainingIgnoreCase(fullName).isPresent()) {
                    found++;
                }
            }

            return VotingMatchSummaryDTO.builder()
                    .found(found)
                    .notFound(Math.max(0, total - found))
                    .total(total)
                    .build();
        } catch (Exception e) {
            return VotingMatchSummaryDTO.builder()
                    .found(0)
                    .notFound(0)
                    .total(0)
                    .build();
        }
    }

    private java.util.List<VotingDetailDTO> loadVotingDetailEntries(Long externalId) throws Exception {
        if (externalId == null) {
            return java.util.List.of();
        }

        java.net.URI uri = java.net.URI.create("https://datos.asambleanacional.gob.ec/ecurul/assemblyman/votingDetail?idVoting=" + externalId);
        java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder()
                .followRedirects(java.net.http.HttpClient.Redirect.ALWAYS)
                .build();
        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(uri)
                .header("Accept", "application/json")
                .header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36")
                .GET()
                .build();

        java.net.http.HttpResponse<String> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200 || response.body() == null || response.body().isBlank()) {
            return java.util.List.of();
        }

        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        return mapper.readValue(response.body(), mapper.getTypeFactory().constructCollectionType(java.util.List.class, VotingDetailDTO.class));
    }

    private String buildFullName(String firstName, String lastname) {
        String trimmedFirst = firstName == null ? "" : firstName.trim();
        String trimmedLast = lastname == null ? "" : lastname.trim();
        return (trimmedFirst + " " + trimmedLast).trim();
    }

    private TipoVoto mapVote(String description) {
        if (description == null) {
            return TipoVoto.ABSTENCION;
        }
        return switch (description.trim().toUpperCase()) {
            case "SI" -> TipoVoto.FAVOR;
            case "NO" -> TipoVoto.CONTRA;
            case "ABSTENCION", "ABSTENCIÓN" -> TipoVoto.ABSTENCION;
            default -> TipoVoto.ABSTENCION;
        };
    }

    private FiltrosLeyDTO buildFiltrosLeyDTO() {
        return FiltrosLeyDTO.builder()
                .categorias(leyRepository.findDistinctCategorias())
                .estados(java.util.Arrays.stream(EstadoLey.values()).map(estado -> estado.name()).collect(Collectors.toList()))
                .build();
    }

    private String classifyLaw(String titulo, String descripcion) {
        String combined = ((titulo == null ? "" : titulo) + " " + (descripcion == null ? "" : descripcion)).toLowerCase(Locale.ROOT);
        if (combined.contains("educ") || combined.contains("escuela") || combined.contains("universidad")) {
            return "EDUCACION";
        }
        if (combined.contains("salud") || combined.contains("hospital") || combined.contains("medic")) {
            return "SALUD";
        }
        if (combined.contains("trabajo") || combined.contains("empleo") || combined.contains("labor")) {
            return "TRABAJO";
        }
        if (combined.contains("seguridad") || combined.contains("polic") || combined.contains("delito")) {
            return "SEGURIDAD";
        }
        return "GENERAL";
    }

    private static ExpedienteLegislativoDTO mapToExpedienteDTO(Ley ley) {
        return ExpedienteLegislativoDTO.builder()
                .id(ley.getId().toString())
                .codigoExpediente(ley.getCodigo())
                .titulo(ley.getTitulo())
                .tituloLey(ley.getTitulo())
                .categoria(ley.getCategoria())
                .estado(ley.getEstado().name())
                .estaAprobado(ley.getEstado() == EstadoLey.APROBADA)
                .proponente(ley.getProponente())
                .accionUrl("/leyes/" + ley.getId())
                .build();
    }

    private int loadVotingDetailCount(Long externalId) throws Exception {
        if (externalId == null) {
            return 0;
        }
        java.net.URI uri = java.net.URI.create("https://datos.asambleanacional.gob.ec/ecurul/assemblyman/votingDetail?idVoting=" + externalId);
        java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder()
                .followRedirects(java.net.http.HttpClient.Redirect.ALWAYS)
                .build();
        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(uri)
                .header("Accept", "application/json")
                .header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36")
                .GET()
                .build();
        java.net.http.HttpResponse<String> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200 || response.body() == null || response.body().isBlank()) {
            return 0;
        }
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        java.util.List<?> list = mapper.readValue(response.body(), mapper.getTypeFactory().constructCollectionType(java.util.List.class, java.util.Map.class));
        return list == null ? 0 : list.size();
    }
}
