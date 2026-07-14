package com.controlf.controller;

import com.controlf.auth.AuthenticatedUser;
import com.controlf.dto.AlertaDTO;
import com.controlf.dto.SuscripcionDTO;
import com.controlf.dto.SuscripcionRequestDTO;
import com.controlf.service.AlertaService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Alertas y suscripciones de nuevas leyes / votaciones (CF-015). Requiere usuario autenticado.
 * Expone rutas nuevas bajo /api/alertas sin tocar ningún endpoint existente.
 */
@RestController
@CrossOrigin("*")
@RequestMapping("/api/alertas")
@RequiredArgsConstructor
public class AlertaController {

    private final AlertaService alertaService;

    @GetMapping
    public List<AlertaDTO> getAlertas(Authentication authentication) {
        return alertaService.obtenerAlertas(currentUserId(authentication));
    }

    @GetMapping("/suscripciones")
    public List<SuscripcionDTO> getSuscripciones(Authentication authentication) {
        return alertaService.listarSuscripciones(currentUserId(authentication));
    }

    @PostMapping("/suscripciones")
    public SuscripcionDTO crearSuscripcion(@RequestBody(required = false) SuscripcionRequestDTO request,
                                           Authentication authentication) {
        String categoria = request != null ? request.getCategoria() : null;
        return alertaService.crearSuscripcion(currentUserId(authentication), categoria);
    }

    @DeleteMapping("/suscripciones/{id}")
    public void eliminarSuscripcion(@PathVariable Integer id, Authentication authentication) {
        alertaService.eliminarSuscripcion(currentUserId(authentication), id);
    }

    private Integer currentUserId(Authentication authentication) {
        return ((AuthenticatedUser) authentication.getPrincipal()).getId();
    }
}
