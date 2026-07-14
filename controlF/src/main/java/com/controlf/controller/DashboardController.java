package com.controlf.controller;

import com.controlf.dto.DashboardStatsDTO;
import com.controlf.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    public DashboardStatsDTO getStats() {
        return dashboardService.getStats();
    }

    @GetMapping("/metricas")
    public com.controlf.dto.MetricasInteractivasDTO getMetricas(
            @org.springframework.web.bind.annotation.RequestParam(required = false) String categoria,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String estado,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String desde,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String hasta) {
        return dashboardService.getMetricasInteractivas(categoria, estado, desde, hasta);
    }

    @GetMapping("/export")
    public ResponseEntity<String> exportStats() {
        String csv = dashboardService.exportStatsCsv();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=dashboard-report.csv")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv);
    }

    @GetMapping("/export/politicos")
    public ResponseEntity<String> exportPoliticos() {
        String csv = dashboardService.exportPoliticosCsv();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte-politicos-detallado.csv")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv);
    }

    @GetMapping("/export/leyes")
    public ResponseEntity<String> exportLeyes() {
        String csv = dashboardService.exportLeyesCsv();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte-leyes-detallado.csv")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv);
    }
}
