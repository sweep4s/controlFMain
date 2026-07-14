package com.controlf.controller;

import com.controlf.auth.AuthenticatedUser;
import com.controlf.db.repository.ComentarioRepository;
import com.controlf.db.schema.Comentario;
import com.controlf.dto.ActualizarCampoPoliticoRequestDTO;
import com.controlf.dto.CalificacionRequestDTO;
import com.controlf.dto.ComentarioRequestDTO;
import com.controlf.dto.PromesaDTO;
import com.controlf.dto.PromesaRequestDTO;
import com.controlf.dto.SimpleItemDTO;
import com.controlf.service.PoliticoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/politicos")
@RequiredArgsConstructor
public class PoliticoController {

    private final PoliticoService politicoService;
    private final ComentarioRepository comentarioRepository;

    @GetMapping("/{id}")
    public com.controlf.dto.PerfilPoliticoDTO getPolitico(@PathVariable Integer id) {
        return politicoService.getPoliticoProfile(id);
    }

    @GetMapping("/filtros")
    public com.controlf.dto.FiltrosPoliticoDTO getFiltros() {
        return politicoService.getFiltros();
    }

    @GetMapping("/importables")
    public List<SimpleItemDTO> getImportables() {
        return politicoService.getPoliticosImportables();
    }

    @GetMapping("/comparar")
    public com.controlf.dto.ComparacionVotosDTO compararPatronesVoto(@RequestParam List<Integer> ids) {
        return politicoService.compararPatronesVoto(ids);
    }

    @GetMapping
    public com.controlf.dto.GrillaPoliticosDTO getPoliticos(
            @RequestParam(defaultValue = "1") int pagina,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String partido,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String comision) {
        return politicoService.getPoliticosFiltrados(pagina, size, nombre, partido, region, comision);
    }

    @PatchMapping("/{id}")
    public void actualizarCampo(@PathVariable Integer id, @Valid @RequestBody ActualizarCampoPoliticoRequestDTO request) {
        politicoService.actualizarCampoPolitico(id, request);
    }

    @PostMapping("/{id}/comentarios")
    public void postComentario(@PathVariable Integer id, @Valid @RequestBody ComentarioRequestDTO request, Authentication authentication) {
        Integer currentUserId = ((AuthenticatedUser) authentication.getPrincipal()).getId();
        politicoService.addComentario(id, request, currentUserId);
    }

    @PostMapping("/{id}/calificaciones")
    public void postCalificacion(@PathVariable Integer id, @Valid @RequestBody CalificacionRequestDTO request, Authentication authentication) {
        Integer currentUserId = ((AuthenticatedUser) authentication.getPrincipal()).getId();
        politicoService.addCalificacion(id, request, currentUserId);
    }

    @PostMapping("/{politicoId}/promesas")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PromesaDTO> crearPromesa(@PathVariable Integer politicoId, @Valid @RequestBody PromesaRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(politicoService.crearPromesa(politicoId, request));
    }

    @GetMapping("/{politicoId}/promesas")
    public List<PromesaDTO> listarPromesas(@PathVariable Integer politicoId) {
        return politicoService.listarPromesasPorPolitico(politicoId);
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
