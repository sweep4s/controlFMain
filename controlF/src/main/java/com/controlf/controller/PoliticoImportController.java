package com.controlf.controller;

import com.controlf.dto.PoliticoImportResultDTO;
import com.controlf.service.PoliticoImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin") // <--- Escucha la base de la ruta admin
@RequiredArgsConstructor
@CrossOrigin("*")
public class PoliticoImportController {

    private final PoliticoImportService politicoImportService;

    // POST /api/admin/import-politicos
    @PostMapping("/import-politicos") // <--- Escucha la acción concreta
    public ResponseEntity<PoliticoImportResultDTO> importAll() {
        return ResponseEntity.ok(politicoImportService.importAll());
    }

    // POST /api/admin/import-politicos/selected
    @PostMapping("/import-politicos/selected")
    public ResponseEntity<PoliticoImportResultDTO> importSelected(@RequestBody List<Long> selectedIds) {
        return ResponseEntity.ok(politicoImportService.importSelected(selectedIds));
    }
}