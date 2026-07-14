package com.controlf.controller;

import com.controlf.dto.CrearPoliticoRequestDTO;
import com.controlf.dto.CrearPromesaRequestDTO;
import com.controlf.dto.PanelControlDTO;
import com.controlf.dto.PanelMantenimientoDTO;
import com.controlf.dto.VinculoRequestDTO;
import com.controlf.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final com.controlf.service.DataSeederService dataSeederService;

    @PostMapping("/seed")
    public String seedData() {
        dataSeederService.seed();
        return "Database seeded successfully";
    }

    @GetMapping("/motor/data")
    public com.controlf.dto.MotorCoherenciaDataDTO getMotorData() {
        return adminService.getMotorData();
    }

    @GetMapping("/politicos/{id}/promesas")
    public java.util.List<com.controlf.dto.SimpleItemDTO> getPromesas(@PathVariable Integer id) {
        return adminService.getPromesasByPolitico(id);
    }

    @PostMapping("/promesas")
    public void crearPromesa(@RequestBody CrearPromesaRequestDTO request) {
        adminService.crearPromesa(request);
    }

    @PostMapping("/politicos")
    public void crearPolitico(@RequestBody CrearPoliticoRequestDTO request) {
        adminService.crearPolitico(request);
    }

    @PostMapping("/leyes")
    public void crearLey(@Valid @RequestBody com.controlf.dto.CrearLeyRequestDTO request) {
        adminService.crearLey(request);
    }

    @DeleteMapping("/politicos/{id}")
    public void eliminarPolitico(@PathVariable Integer id) {
        adminService.eliminarPolitico(id);
    }

    @GetMapping("/panel")
    public PanelControlDTO getPanel() {
        return adminService.getSecurityPanel();
    }

    @GetMapping("/mantenimiento")
    public PanelMantenimientoDTO getMantenimiento() {
        return adminService.getMantenimientoInfo();
    }

    @PostMapping("/vinculos")
    public void postVinculo(@Valid @RequestBody VinculoRequestDTO request) {
        adminService.crearVinculoCoherencia(request);
    }

    @PostMapping("/mantenimiento/respaldo")
    public void postRespaldo() {
        adminService.ejecutarRespaldo(null); // ID de admin simplificado para el ejemplo
    }

    @PostMapping("/mantenimiento/limpiar-cache")
    public void postLimpiarCache() {
        adminService.limpiarCache();
    }

    @GetMapping("/historico")
    public com.controlf.dto.ReporteHistoricoDTO getHistorico() {
        return adminService.getHistoricoResumen();
    }

    @PostMapping("/importar-leyes")
    public void postImportarLeyes() {
        adminService.importarLeyes();
    }

    @PostMapping("/normalizar-leyes")
    public com.controlf.dto.LeyNormalizacionResultDTO postNormalizarLeyes() {
        return adminService.normalizarLeyes();
    }

    @GetMapping("/leyes/syncable")
    public java.util.List<com.controlf.dto.LeySyncItemDTO> getLeyesSyncables() {
        return adminService.listarLeyesParaSync();
    }
}
