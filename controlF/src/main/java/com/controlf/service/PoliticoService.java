package com.controlf.service;

import com.controlf.db.repository.*;
import com.controlf.db.schema.Calificacion;
import com.controlf.db.schema.Comentario;
import com.controlf.db.schema.Politico;
import com.controlf.db.schema.Promesa;
import com.controlf.db.schema.Usuario;
import com.controlf.dto.ActualizarCampoPoliticoRequestDTO;
import com.controlf.dto.CalificacionRequestDTO;
import com.controlf.dto.CartaPoliticoDTO;
import com.controlf.dto.ComentarioRequestDTO;
import com.controlf.dto.PromesaDTO;
import com.controlf.dto.PromesaRequestDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PoliticoService {

    private final PoliticoRepository politicoRepository;
    private final VinculoPromesaLeyRepository vinculoRepository;
    private final ComentarioRepository comentarioRepository;
    private final CalificacionRepository calificacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final PromesaRepository promesaRepository;
    private final ConfiguracionRepository configuracionRepository;
    private final VotoRepository votoRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public com.controlf.dto.PerfilPoliticoDTO getPoliticoProfile(Integer id) {
        Politico p = politicoRepository.findById(id).orElseThrow();
        Double coherencia = vinculoRepository.findAverageCoherenciaByPoliticoId(p.getId());

        // Índice de reputación consolidado a partir de las calificaciones ciudadanas (escala 1-5).
        Double reputacion = calificacionRepository.findAveragePuntajeByPoliticoId(p.getId());
        double reputacionRedondeada = reputacion != null ? Math.round(reputacion * 10.0) / 10.0 : 0.0;
        long totalCalificaciones = calificacionRepository.countByPoliticoId(p.getId());

        List<com.controlf.dto.HistorialCoherenciaDTO> historial = p.getPromesas().stream()
                .flatMap(promesa -> promesa.getVinculos().stream())
                .map(v -> com.controlf.dto.HistorialCoherenciaDTO.builder()
                        .leyTitulo(v.getLey().getTitulo())
                        .votoReal(findVotoForPolitico(v.getLey(), p))
                        .resultado(v.getNivelCoherencia().name())
                        .analisis(v.getAnalisisCoherencia())
                        .build())
                .collect(Collectors.toList());

        return com.controlf.dto.PerfilPoliticoDTO.builder()
                .id(p.getId().toString())
                .nombre(p.getNombreCompleto())
                .organizacion(p.getPartidoPolitico())
                .cargo(p.getCargoActual())
                .patrimonio(p.getPatrimonioDeclarado() != null ? "$" + p.getPatrimonioDeclarado().toString() : "No declarado")
                .fotoUrl(p.getFotoUrl())
                .antecedentes(p.getAntecedentes())
                .estaActivo(p.getEstaActivo())
                .porcentajeCoherencia(coherencia != null ? coherencia : 0.0)
                .estadoEtiqueta(determineEstadoEtiqueta(coherencia))
                .indiceReputacion(reputacionRedondeada)
                .totalCalificaciones(totalCalificaciones)
                .etiquetaReputacion(determineEtiquetaReputacion(reputacion, totalCalificaciones))
                .historial(historial)
                .historialCambios(parseHistorialCambios(p.getHistorialActualizaciones()))
                .comentarios(p.getComentarios().stream().filter(PoliticoService::esComentarioPublico).map(c -> com.controlf.dto.ComentarioDebateDTO.builder()
                        .id(c.getId().toString())
                        .usuario(c.getUsuario().getNombre())
                        .mensaje(c.getTexto())
                        .fecha(c.getFecha().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                        .avatarUrl(c.getUsuario().getAvatarUrl())
                        .puntaje(c.getPuntaje())
                        .build()).collect(Collectors.toList()))
                .build();
    }

    /**
     * Compara los patrones de votación entre dos o más políticos (CF-016): distribución de votos,
     * asistencia, coherencia y las leyes votadas en común con el sentido del voto de cada uno.
     */
    public com.controlf.dto.ComparacionVotosDTO compararPatronesVoto(List<Integer> ids) {
        List<Integer> unicos = ids == null ? List.of() : ids.stream().filter(java.util.Objects::nonNull).distinct().collect(Collectors.toList());
        if (unicos.size() < 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe seleccionar al menos dos políticos para comparar");
        }

        List<com.controlf.dto.ComparacionPoliticoDTO> resumenes = new ArrayList<>();
        // leyId -> (politicoId -> sentido del voto)
        Map<Integer, Map<Integer, String>> votosPorLey = new LinkedHashMap<>();
        Map<Integer, String> tituloPorLey = new LinkedHashMap<>();

        for (Integer pid : unicos) {
            Politico p = politicoRepository.findById(pid)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Político no encontrado: " + pid));
            List<com.controlf.db.schema.Voto> votos = votoRepository.findByPoliticoId(pid);

            long favor = 0, contra = 0, abstencion = 0, asistencias = 0, inasistencias = 0;
            for (com.controlf.db.schema.Voto v : votos) {
                com.controlf.db.schema.enums.TipoVoto tipo = v.getTipoVoto();
                if (tipo == com.controlf.db.schema.enums.TipoVoto.FAVOR) favor++;
                else if (tipo == com.controlf.db.schema.enums.TipoVoto.CONTRA) contra++;
                else if (tipo == com.controlf.db.schema.enums.TipoVoto.ABSTENCION) abstencion++;

                if (Boolean.TRUE.equals(v.getAsistencia())) asistencias++;
                else inasistencias++;

                if (v.getLey() != null) {
                    votosPorLey.computeIfAbsent(v.getLey().getId(), k -> new LinkedHashMap<>())
                            .put(pid, tipo != null ? tipo.name() : "—");
                    tituloPorLey.putIfAbsent(v.getLey().getId(), v.getLey().getTitulo());
                }
            }

            long total = votos.size();
            double pctAsistencia = total > 0 ? Math.round(asistencias * 1000.0 / total) / 10.0 : 0.0;
            Double coherencia = vinculoRepository.findAverageCoherenciaByPoliticoId(pid);

            resumenes.add(com.controlf.dto.ComparacionPoliticoDTO.builder()
                    .id(p.getId().toString())
                    .nombre(p.getNombreCompleto())
                    .organizacion(p.getPartidoPolitico())
                    .fotoUrl(p.getFotoUrl())
                    .totalVotos(total)
                    .votosFavor(favor)
                    .votosContra(contra)
                    .votosAbstencion(abstencion)
                    .asistencias(asistencias)
                    .inasistencias(inasistencias)
                    .porcentajeAsistencia(pctAsistencia)
                    .porcentajeCoherencia(coherencia != null ? coherencia : 0.0)
                    .build());
        }

        List<com.controlf.dto.ComparacionLeyDTO> leyesComparadas = new ArrayList<>();
        long enComun = 0;
        long coincidencias = 0;
        for (Map.Entry<Integer, Map<Integer, String>> entry : votosPorLey.entrySet()) {
            Map<Integer, String> porPolitico = entry.getValue();
            if (porPolitico.size() < 2) {
                continue; // solo interesa lo votado por dos o más de los comparados
            }
            enComun++;

            Map<String, String> votos = new LinkedHashMap<>();
            for (Integer pid : unicos) {
                votos.put(pid.toString(), porPolitico.getOrDefault(pid, "—"));
            }

            Set<String> sentidos = new HashSet<>(porPolitico.values());
            boolean coinciden = sentidos.size() == 1;
            if (coinciden) coincidencias++;

            leyesComparadas.add(com.controlf.dto.ComparacionLeyDTO.builder()
                    .leyId(entry.getKey().toString())
                    .leyTitulo(tituloPorLey.get(entry.getKey()))
                    .votos(votos)
                    .coinciden(coinciden)
                    .build());
        }

        double indice = enComun > 0 ? Math.round(coincidencias * 1000.0 / enComun) / 10.0 : 0.0;

        return com.controlf.dto.ComparacionVotosDTO.builder()
                .politicos(resumenes)
                .leyesComparadas(leyesComparadas)
                .leyesEnComun(enComun)
                .coincidencias(coincidencias)
                .indiceCoincidencia(indice)
                .build();
    }

    /**
     * Un comentario es público si está APROBADO. Los comentarios previos a la moderación
     * (estado nulo) se consideran aprobados para no ocultar contenido ya existente (CF-029).
     */
    static boolean esComentarioPublico(Comentario c) {
        return c.getEstado() == null || c.getEstado() == com.controlf.db.schema.enums.EstadoModeracion.APROBADO;
    }

    private String findVotoForPolitico(com.controlf.db.schema.Ley ley, Politico p) {
        return ley.getVotos().stream()
                .filter(v -> v.getPolitico().getId().equals(p.getId()))
                .map(v -> v.getTipoVoto().name())
                .findFirst()
                .orElse("N/A");
    }

    public com.controlf.dto.FiltrosPoliticoDTO getFiltros() {
        try {
            return com.controlf.dto.FiltrosPoliticoDTO.builder()
                    .partidos(politicoRepository.findDistinctPartidos())
                    .regiones(politicoRepository.findDistinctRegiones())
                    .comisiones(politicoRepository.findDistinctComisiones())
                    .build();
        } catch (Exception e) {
            return com.controlf.dto.FiltrosPoliticoDTO.builder()
                    .partidos(java.util.List.of())
                    .regiones(java.util.List.of())
                    .comisiones(java.util.List.of())
                    .build();
        }
    }

    public List<com.controlf.dto.SimpleItemDTO> getPoliticosImportables() {
        return politicoRepository.findAll().stream()
                .map(PoliticoService::mapToSimpleItemDTO)
                .collect(Collectors.toList());
    }

public com.controlf.dto.GrillaPoliticosDTO getPoliticosFiltrados(int pagina, int size, String nombre, String partido, String region, String comision) {
    try {
        org.springframework.data.jpa.domain.Specification<Politico> spec = (root, query, cb) -> cb.conjunction();

        if (nombre != null && !nombre.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("nombreCompleto")), "%" + nombre.toLowerCase() + "%"));
        }

        if (partido != null && !partido.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("partidoPolitico"), partido));
        }

        if (region != null && !region.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("region"), region));
        }

        if (comision != null && !comision.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("comision"), comision));
        }

        org.springframework.data.domain.Page<Politico> page = politicoRepository.findAll(
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

        List<CartaPoliticoDTO> cartas = page.getContent().stream()
                .map(PoliticoService::mapToCartaDTO)
                .collect(Collectors.toList());

        return com.controlf.dto.GrillaPoliticosDTO.builder()
                .id("grilla-politicos")
                .cartas(cartas)
                .paginaActual(pagina)
                .totalPaginas(Math.max(1, page.getTotalPages()))
                .build();

    } catch (Exception e) {
        e.printStackTrace();
        return com.controlf.dto.GrillaPoliticosDTO.builder()
                .id("grilla-politicos")
                .cartas(java.util.List.of())
                .paginaActual(1)
                .totalPaginas(1)
                .build();
    }
}
  public List<CartaPoliticoDTO> getAllPoliticosAsCartas() {
    return politicoRepository.findAll(
            org.springframework.data.domain.Sort.by(
                    org.springframework.data.domain.Sort.Direction.DESC,
                    "id"
            )
    ).stream()
     .map(PoliticoService::mapToCartaDTO)
     .collect(Collectors.toList());
}

    @Transactional
    public void actualizarCampoPolitico(Integer politicoId, ActualizarCampoPoliticoRequestDTO request) {
        Politico p = politicoRepository.findById(politicoId).orElseThrow();

        String campo = request.getCampo() == null ? "" : request.getCampo().trim().toLowerCase();
        String valor = request.getValor();

        String valorAnterior = null;
        switch (campo) {
            case "patrimonio" -> {
                valorAnterior = p.getPatrimonioDeclarado() == null ? null : p.getPatrimonioDeclarado().toPlainString();
                p.setPatrimonioDeclarado(new BigDecimal(valor));
            }
            case "antecedentes" -> {
                valorAnterior = p.getAntecedentes();
                p.setAntecedentes(valor);
            }
            default -> throw new IllegalArgumentException("Campo no soportado: " + campo);
        }

        p.setHistorialActualizaciones(appendHistorialEntry(p.getHistorialActualizaciones(), campo, valorAnterior, valor));
        politicoRepository.save(p);
    }

    public void addComentario(Integer politicoId, ComentarioRequestDTO request, Integer currentUserId) {
        Politico p = politicoRepository.findById(politicoId).orElseThrow();
        Usuario u = usuarioRepository.findById(currentUserId).orElseThrow();

        Comentario c = new Comentario();
        c.setTexto(request.getTexto());
        c.setUsuario(u);
        c.setFecha(LocalDateTime.now());
        c.setEsBasadoEnHechos(false);
        c.setPuntaje(request.getPuntaje());

        comentarioRepository.save(c);
        p.getComentarios().add(c);
        politicoRepository.save(p);
    }

    public void addCalificacion(Integer politicoId, CalificacionRequestDTO request, Integer currentUserId) {
        Politico p = politicoRepository.findById(politicoId).orElseThrow();
        Usuario u = usuarioRepository.findById(currentUserId).orElseThrow();

        Calificacion cal = new Calificacion();
        cal.setPuntaje(request.getPuntaje());
        cal.setUsuario(u);
        cal.setFecha(LocalDateTime.now());

        calificacionRepository.save(cal);
        p.getCalificaciones().add(cal);
        politicoRepository.save(p);
    }

    @Transactional
    public PromesaDTO crearPromesa(Integer politicoId, PromesaRequestDTO request) {
        Politico politico = politicoRepository.findById(politicoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Político no encontrado"));

        Promesa promesa = new Promesa();
        promesa.setDescripcion(request.getDescripcion());
        promesa.setCategoria(request.getCategoria());
        promesa.setFechaCreacion(LocalDate.now());
        promesa.setPolitico(politico);

        Promesa saved = promesaRepository.save(promesa);
        if (politico.getPromesas() == null) {
            politico.setPromesas(new ArrayList<>());
        }
        politico.getPromesas().add(saved);
        politicoRepository.save(politico);

        return mapToPromesaDTO(saved);
    }

    public List<PromesaDTO> listarPromesasPorPolitico(Integer politicoId) {
        if (!politicoRepository.existsById(politicoId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Político no encontrado");
        }
        return promesaRepository.findByPoliticoId(politicoId).stream()
                .map(this::mapToPromesaDTO)
                .collect(Collectors.toList());
    }

    private PromesaDTO mapToPromesaDTO(Promesa promesa) {
        return PromesaDTO.builder()
                .id(promesa.getId())
                .descripcion(promesa.getDescripcion())
                .categoria(promesa.getCategoria())
                .fechaCreacion(promesa.getFechaCreacion())
                .politicoId(promesa.getPolitico() != null ? promesa.getPolitico().getId() : null)
                .build();
    }

    private static CartaPoliticoDTO mapToCartaDTO(Politico p) {
        Double coherencia = null;
        long proyectos = 0L;
        return CartaPoliticoDTO.builder()
                .id(p.getId().toString())
                .nombre(p.getNombreCompleto())
                .organizacion(p.getPartidoPolitico())
                .fotoUrl(p.getFotoUrl())
                .estaActivo(p.getEstaActivo())
                .porcentajeCoherencia(coherencia != null ? coherencia : 0.0)
                .cantidadProyectos(proyectos)
                .estadoEtiqueta("SIN DATOS")
                .build();
    }

    private static com.controlf.dto.SimpleItemDTO mapToSimpleItemDTO(Politico p) {
        return com.controlf.dto.SimpleItemDTO.builder()
                .id(p.getId().toString())
                .label(p.getNombreCompleto())
                .build();
    }

    private String appendHistorialEntry(String currentJson, String campo, String valorAnterior, String valorNuevo) {
        List<Map<String, Object>> entries = new ArrayList<>();
        if (currentJson != null && !currentJson.isBlank()) {
            try {
                entries = objectMapper.readValue(currentJson, new TypeReference<>() {});
            } catch (Exception ignored) {
                entries = new ArrayList<>();
            }
        }

        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("fecha", LocalDateTime.now().toString());
        entry.put("campo", campo);
        entry.put("valorAnterior", valorAnterior);
        entry.put("valorNuevo", valorNuevo);
        entries.add(entry);

        try {
            return objectMapper.writeValueAsString(entries);
        } catch (Exception e) {
            return currentJson;
        }
    }

    private String determineEstadoEtiqueta(Double coherencia) {
        if (coherencia == null) return "SIN DATOS";
        
        double alta = getThreshold("UMBRAL_COHERENCIA_ALTA", 70.0);
        double media = getThreshold("UMBRAL_COHERENCIA_MEDIA", 40.0);

        if (coherencia >= alta) return "COHERENTE";
        if (coherencia >= media) return "AMBIGUO";
        return "INCOHERENTE";
    }

    /**
     * Convierte el JSON persistido de historialActualizaciones en una lista ordenada de cambios
     * (más recientes primero) para exponerlos en el perfil (CF-005).
     */
    private List<com.controlf.dto.HistorialCambioPerfilDTO> parseHistorialCambios(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            List<Map<String, Object>> entries = objectMapper.readValue(json, new TypeReference<>() {});
            List<com.controlf.dto.HistorialCambioPerfilDTO> cambios = new ArrayList<>();
            for (Map<String, Object> entry : entries) {
                cambios.add(com.controlf.dto.HistorialCambioPerfilDTO.builder()
                        .campo(asText(entry.get("campo")))
                        .valorAnterior(asText(entry.get("valorAnterior")))
                        .valorNuevo(asText(entry.get("valorNuevo")))
                        .fecha(asText(entry.get("fecha")))
                        .build());
            }
            java.util.Collections.reverse(cambios); // más recientes primero
            return cambios;
        } catch (Exception e) {
            return List.of();
        }
    }

    private String asText(Object value) {
        return value == null ? null : value.toString();
    }

    private String determineEtiquetaReputacion(Double reputacion, long totalCalificaciones) {
        if (reputacion == null || totalCalificaciones == 0) return "SIN CALIFICACIONES";
        if (reputacion >= 4.0) return "MUY BUENA";
        if (reputacion >= 3.0) return "BUENA";
        if (reputacion >= 2.0) return "REGULAR";
        return "DEFICIENTE";
    }

    private double getThreshold(String clave, double defaultValue) {
        return configuracionRepository.findById(clave)
                .map(c -> Double.parseDouble(c.getValor()))
                .orElse(defaultValue);
    }
}
