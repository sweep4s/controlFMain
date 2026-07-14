package com.controlf.service;

import com.controlf.db.repository.ComentarioRepository;
import com.controlf.db.repository.LeyRepository;
import com.controlf.db.repository.PoliticoRepository;
import com.controlf.db.schema.Comentario;
import com.controlf.db.schema.enums.EstadoModeracion;
import com.controlf.dto.ComentarioModeracionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Flujo de validación de contenido ciudadano (CF-029). El rol VALIDADOR puede listar los
 * comentarios por estado y aprobar / rechazar / observar cada uno. Solo el contenido APROBADO
 * se publica en las vistas públicas (ver PoliticoService#esComentarioPublico).
 */
@Service
@RequiredArgsConstructor
public class ValidacionService {

    private final ComentarioRepository comentarioRepository;
    private final LeyRepository leyRepository;
    private final PoliticoRepository politicoRepository;

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Lista los comentarios para moderar. Si estadoFiltro es nulo o "TODOS" devuelve todos.
     */
    public List<ComentarioModeracionDTO> listarComentarios(String estadoFiltro) {
        Stream<Comentario> comentarios;
        if (estadoFiltro == null || estadoFiltro.isBlank() || estadoFiltro.equalsIgnoreCase("TODOS")) {
            comentarios = comentarioRepository.findAll().stream();
        } else {
            EstadoModeracion estado = parseEstado(estadoFiltro);
            // El estado nulo (legado) se considera APROBADO.
            comentarios = comentarioRepository.findAll().stream()
                    .filter(c -> estadoEfectivo(c) == estado);
        }

        return comentarios
                .sorted((a, b) -> {
                    if (a.getFecha() == null || b.getFecha() == null) return 0;
                    return b.getFecha().compareTo(a.getFecha());
                })
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public java.util.Map<String, Long> contarPorEstado() {
        java.util.Map<String, Long> conteo = new java.util.LinkedHashMap<>();
        for (EstadoModeracion estado : EstadoModeracion.values()) {
            conteo.put(estado.name(), 0L);
        }
        for (Comentario c : comentarioRepository.findAll()) {
            String clave = estadoEfectivo(c).name();
            conteo.merge(clave, 1L, Long::sum);
        }
        return conteo;
    }

    @Transactional
    public ComentarioModeracionDTO moderar(Integer comentarioId, String nuevoEstado, String nota) {
        Comentario comentario = comentarioRepository.findById(comentarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comentario no encontrado"));

        comentario.setEstado(parseEstado(nuevoEstado));
        if (nota != null && !nota.isBlank()) {
            comentario.setNotaModeracion(nota.trim());
        }
        comentarioRepository.save(comentario);
        return mapToDTO(comentario);
    }

    private EstadoModeracion estadoEfectivo(Comentario c) {
        return c.getEstado() == null ? EstadoModeracion.APROBADO : c.getEstado();
    }

    private EstadoModeracion parseEstado(String valor) {
        try {
            return EstadoModeracion.valueOf(valor.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado de moderación no válido: " + valor);
        }
    }

    private ComentarioModeracionDTO mapToDTO(Comentario c) {
        String contextoTipo = "N/D";
        String contextoTitulo = "";
        String contextoId = "";

        var leyOpt = leyRepository.findByComentarioId(c.getId());
        if (leyOpt.isPresent()) {
            contextoTipo = "LEY";
            contextoTitulo = leyOpt.get().getTitulo();
            contextoId = String.valueOf(leyOpt.get().getId());
        } else {
            var politicoOpt = politicoRepository.findByComentarioId(c.getId());
            if (politicoOpt.isPresent()) {
                contextoTipo = "POLITICO";
                contextoTitulo = politicoOpt.get().getNombreCompleto();
                contextoId = String.valueOf(politicoOpt.get().getId());
            }
        }

        return ComentarioModeracionDTO.builder()
                .id(c.getId())
                .texto(c.getTexto())
                .usuario(c.getUsuario() != null ? c.getUsuario().getNombre() : "Anónimo")
                .fecha(c.getFecha() != null ? c.getFecha().format(FORMATO_FECHA) : "")
                .estado(estadoEfectivo(c).name())
                .notaModeracion(c.getNotaModeracion())
                .contextoTipo(contextoTipo)
                .contextoTitulo(contextoTitulo)
                .contextoId(contextoId)
                .build();
    }
}
