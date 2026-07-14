package com.controlf.controller;

import com.controlf.dto.ComentarioModeracionDTO;
import com.controlf.dto.ModeracionRequestDTO;
import com.controlf.service.ValidacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Panel de validación de contenido ciudadano (CF-029). Accesible solo para VALIDADOR y ADMIN
 * (ver SecurityConfig). No modifica ningún endpoint existente; expone rutas nuevas bajo /api/validacion.
 */
@RestController
@CrossOrigin("*")
@RequestMapping("/api/validacion")
@RequiredArgsConstructor
public class ValidacionController {

    private final ValidacionService validacionService;

    @GetMapping("/comentarios")
    public List<ComentarioModeracionDTO> listarComentarios(@RequestParam(required = false) String estado) {
        return validacionService.listarComentarios(estado);
    }

    @GetMapping("/resumen")
    public Map<String, Long> resumen() {
        return validacionService.contarPorEstado();
    }

    @PatchMapping("/comentarios/{id}/estado")
    public ComentarioModeracionDTO moderar(@PathVariable Integer id, @Valid @RequestBody ModeracionRequestDTO request) {
        return validacionService.moderar(id, request.getEstado(), request.getNota());
    }
}
