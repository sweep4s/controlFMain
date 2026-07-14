package com.controlf.service;

import com.controlf.db.repository.LeyRepository;
import com.controlf.db.repository.SuscripcionRepository;
import com.controlf.db.repository.UsuarioRepository;
import com.controlf.db.repository.VotoRepository;
import com.controlf.db.schema.Ley;
import com.controlf.db.schema.Suscripcion;
import com.controlf.db.schema.Usuario;
import com.controlf.db.schema.Voto;
import com.controlf.dto.AlertaDTO;
import com.controlf.dto.SuscripcionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Gestión de suscripciones y generación de alertas de nuevas leyes/votaciones (CF-015).
 * Un usuario se suscribe a una categoría (o a todas) y consulta las leyes y votaciones relevantes,
 * resaltando las que son nuevas desde su fecha de suscripción.
 */
@Service
@RequiredArgsConstructor
public class AlertaService {

    private final SuscripcionRepository suscripcionRepository;
    private final UsuarioRepository usuarioRepository;
    private final LeyRepository leyRepository;
    private final VotoRepository votoRepository;

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final int MAX_ALERTAS = 50;

    public List<SuscripcionDTO> listarSuscripciones(Integer userId) {
        return suscripcionRepository.findByUsuarioId(userId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public SuscripcionDTO crearSuscripcion(Integer userId, String categoria) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        String cat = (categoria != null && !categoria.isBlank()) ? categoria.trim() : null;

        // Evita suscripciones duplicadas a la misma categoría (o a "todas").
        for (Suscripcion existente : suscripcionRepository.findByUsuarioId(userId)) {
            if (mismaCategoria(existente.getCategoria(), cat)) {
                return mapToDTO(existente);
            }
        }

        Suscripcion suscripcion = new Suscripcion();
        suscripcion.setUsuario(usuario);
        suscripcion.setCategoria(cat);
        suscripcion.setFechaCreacion(LocalDateTime.now());
        return mapToDTO(suscripcionRepository.save(suscripcion));
    }

    @Transactional
    public void eliminarSuscripcion(Integer userId, Integer suscripcionId) {
        Suscripcion suscripcion = suscripcionRepository.findById(suscripcionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Suscripción no encontrada"));
        if (suscripcion.getUsuario() == null || !suscripcion.getUsuario().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes eliminar suscripciones de otro usuario");
        }
        suscripcionRepository.delete(suscripcion);
    }

    public List<AlertaDTO> obtenerAlertas(Integer userId) {
        List<Suscripcion> subs = suscripcionRepository.findByUsuarioId(userId);
        if (subs.isEmpty()) {
            return List.of();
        }

        boolean todas = subs.stream().anyMatch(s -> s.getCategoria() == null || s.getCategoria().isBlank());
        Set<String> categorias = subs.stream()
                .map(Suscripcion::getCategoria)
                .filter(c -> c != null && !c.isBlank())
                .map(c -> c.toUpperCase(Locale.ROOT))
                .collect(Collectors.toSet());
        LocalDateTime desdeSuscripcion = subs.stream()
                .map(Suscripcion::getFechaCreacion)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);

        List<AlertaDTO> alertas = new ArrayList<>();
        for (Ley ley : leyRepository.findAll()) {
            boolean coincide = todas
                    || (ley.getCategoria() != null && categorias.contains(ley.getCategoria().toUpperCase(Locale.ROOT)));
            if (!coincide) {
                continue;
            }

            if (ley.getFechaIngreso() != null) {
                boolean nuevo = desdeSuscripcion != null
                        && !ley.getFechaIngreso().isBefore(desdeSuscripcion.toLocalDate());
                alertas.add(AlertaDTO.builder()
                        .tipo("LEY")
                        .titulo(ley.getTitulo())
                        .categoria(ley.getCategoria())
                        .fecha(ley.getFechaIngreso().toString())
                        .detalle("Nueva ley ingresada")
                        .leyId(ley.getId().toString())
                        .nuevo(nuevo)
                        .build());
            }

            List<Voto> votos = votoRepository.findByLeyId(ley.getId());
            LocalDateTime ultimaVotacion = votos.stream()
                    .map(Voto::getFechaVoto)
                    .filter(Objects::nonNull)
                    .max(LocalDateTime::compareTo)
                    .orElse(null);
            if (ultimaVotacion != null) {
                boolean nuevo = desdeSuscripcion != null && !ultimaVotacion.isBefore(desdeSuscripcion);
                alertas.add(AlertaDTO.builder()
                        .tipo("VOTACION")
                        .titulo(ley.getTitulo())
                        .categoria(ley.getCategoria())
                        .fecha(ultimaVotacion.toLocalDate().toString())
                        .detalle("Votación registrada (" + votos.size() + " votos)")
                        .leyId(ley.getId().toString())
                        .nuevo(nuevo)
                        .build());
            }
        }

        alertas.sort((a, b) -> b.getFecha().compareTo(a.getFecha()));
        return alertas.size() > MAX_ALERTAS ? new ArrayList<>(alertas.subList(0, MAX_ALERTAS)) : alertas;
    }

    private boolean mismaCategoria(String a, String b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equalsIgnoreCase(b);
    }

    private SuscripcionDTO mapToDTO(Suscripcion s) {
        return SuscripcionDTO.builder()
                .id(s.getId())
                .categoria(s.getCategoria())
                .fechaCreacion(s.getFechaCreacion() != null ? s.getFechaCreacion().format(FORMATO_FECHA) : null)
                .build();
    }
}
