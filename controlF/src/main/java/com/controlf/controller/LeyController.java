package com.controlf.controller;

import com.controlf.auth.AuthenticatedUser;
import com.controlf.db.repository.ComentarioRepository;
import com.controlf.db.schema.Comentario;
import com.controlf.dto.*;
import com.controlf.service.LeyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/leyes")
@RequiredArgsConstructor
public class LeyController {

    private final LeyService leyService;
    private final ComentarioRepository comentarioRepository;

    @GetMapping("/{id}/perfil")
    public PerfilLeyDTO getPerfil(@PathVariable Integer id) {
        return leyService.getFullPerfilLey(id);
    }

    @PostMapping("/{id}/explicar")
    public ContenidoLeyDTO explicar(@PathVariable Integer id) {
        return leyService.explicarLey(id);
    }


    @GetMapping("/filtros")
    public FiltrosLeyDTO getFiltros() {
        return leyService.getFiltros();
    }

    @GetMapping
    public GrillaLeyesDTO getLeyes(
            @RequestParam(defaultValue = "1") int pagina,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String termino,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String estado) {
        return leyService.getLeyesFiltradas(pagina, size, termino, categoria, estado);
    }

    @PatchMapping("/{id}/categoria")
    public void actualizarCategoria(@PathVariable Integer id, @Valid @RequestBody CategoriaLeyRequestDTO request) {
        leyService.actualizarCategoriaLey(id, request);
    }

    @PatchMapping("/{id}/estado")
    public void actualizarEstado(@PathVariable Integer id, @Valid @RequestBody EstadoLeyRequestDTO request) {
        leyService.actualizarEstadoLey(id, request);
    }

    @PatchMapping("/{id}/votos/{votoId}/asistencia")
    public void actualizarAsistencia(@PathVariable Integer id, @PathVariable Integer votoId, @Valid @RequestBody AsistenciaVotoRequestDTO request) {
        leyService.actualizarAsistenciaVoto(id, votoId, request);
    }

    @PostMapping("/{id}/comentarios")
    public void postComentario(@PathVariable Integer id, @Valid @RequestBody ComentarioRequestDTO request, Authentication authentication) {
        Integer currentUserId = ((AuthenticatedUser) authentication.getPrincipal()).getId();
        leyService.addComentario(id, request, currentUserId);
    }

    @PostMapping("/{id}/calificaciones")
    public void postCalificacion(@PathVariable Integer id, @Valid @RequestBody CalificacionRequestDTO request, Authentication authentication) {
        Integer currentUserId = ((AuthenticatedUser) authentication.getPrincipal()).getId();
        leyService.addCalificacion(id, request, currentUserId);
    }

    @PostMapping("/{id}/import-voting-detail")
    public ImportResultDTO importVotingDetail(@PathVariable Integer id) {
        return leyService.importVotingDetailVotes(id);
    }

    @PatchMapping("/comentarios/{comentarioId}")
    public ResponseEntity<Void> actualizarComentario(@PathVariable Integer comentarioId, @Valid @RequestBody ComentarioRequestDTO request, Authentication authentication) {
        Comentario comentario = comentarioRepository.findById(comentarioId).orElseThrow();
        AuthenticatedUser principal = (AuthenticatedUser) authentication.getPrincipal();
        if (!principal.getRole().equals("ADMIN") && !comentario.getUsuario().getId().equals(principal.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        comentario.setTexto(request.getTexto());
        comentarioRepository.save(comentario);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/comentarios/{comentarioId}")
    public ResponseEntity<Void> eliminarComentario(@PathVariable Integer comentarioId, Authentication authentication) {
        Comentario comentario = comentarioRepository.findById(comentarioId).orElseThrow();
        AuthenticatedUser principal = (AuthenticatedUser) authentication.getPrincipal();
        if (!principal.getRole().equals("ADMIN") && !comentario.getUsuario().getId().equals(principal.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        comentarioRepository.delete(comentario);
        return ResponseEntity.ok().build();
    }
}
