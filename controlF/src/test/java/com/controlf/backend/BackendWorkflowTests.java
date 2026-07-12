package com.controlf.backend;

import com.controlf.db.repository.LeyRepository;
import com.controlf.db.repository.PoliticoRepository;
import com.controlf.db.repository.VotoRepository;
import com.controlf.db.schema.Ley;
import com.controlf.db.schema.Politico;
import com.controlf.db.schema.Voto;
import com.controlf.db.schema.enums.EstadoLey;
import com.controlf.db.schema.enums.TipoVoto;
import com.controlf.dto.ReporteHistoricoDTO;
import com.controlf.service.AdminService;
import com.controlf.service.LeyService;
import com.controlf.service.PoliticoService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class BackendWorkflowTests {

    @Autowired
    private PoliticoService politicoService;

    @Autowired
    private PoliticoRepository politicoRepository;

    @Autowired
    private LeyService leyService;

    @Autowired
    private LeyRepository leyRepository;

    @Autowired
    private VotoRepository votoRepository;

    @Autowired
    private AdminService adminService;

    @Test
    void shouldUpdatePatrimonioAndKeepHistory() {
        Politico politico = new Politico();
        politico.setNombreCompleto("Test Politico");
        politico.setPatrimonioDeclarado(new BigDecimal("100"));
        politico.setAntecedentes("Antecedentes iniciales");
        politicoRepository.save(politico);

        politicoService.actualizarCampoPolitico(politico.getId(), com.controlf.dto.ActualizarCampoPoliticoRequestDTO.builder().campo("patrimonio").valor("200").build());

        Politico refreshed = politicoRepository.findById(politico.getId()).orElseThrow();
        assertThat(refreshed.getPatrimonioDeclarado()).isEqualByComparingTo("200");
        assertThat(refreshed.getHistorialActualizaciones()).contains("patrimonio");
        assertThat(refreshed.getHistorialActualizaciones()).contains("100");
    }

    @Test
    void shouldClassifyStatusAndReportAggregates() {
        Ley ley = new Ley();
        ley.setTitulo("Ley de Educación Superior");
        ley.setCodigo("LEY-TEST-1");
        ley.setDescripcionOriginal("Descripción de prueba");
        ley.setEstado(EstadoLey.DEBATE);
        ley.setFechaIngreso(LocalDate.now());
        ley = leyRepository.save(ley);

        Politico politico = new Politico();
        politico.setNombreCompleto("Politico Test");
        politicoRepository.save(politico);

        Voto voto = new Voto();
        voto.setLey(ley);
        voto.setPolitico(politico);
        voto.setTipoVoto(TipoVoto.FAVOR);
        voto.setAsistencia(true);
        voto.setFechaVoto(LocalDateTime.now());
        votoRepository.save(voto);

        leyService.actualizarCategoriaLey(ley.getId(), com.controlf.dto.CategoriaLeyRequestDTO.builder().categoria("EDUCACION").build());
        leyService.actualizarEstadoLey(ley.getId(), com.controlf.dto.EstadoLeyRequestDTO.builder().estado("APROBADA").build());
        leyService.actualizarAsistenciaVoto(ley.getId(), voto.getId(), com.controlf.dto.AsistenciaVotoRequestDTO.builder().asistencia(false).build());

        Ley refreshedLey = leyRepository.findById(ley.getId()).orElseThrow();
        assertThat(refreshedLey.getCategoria()).isEqualTo("EDUCACION");
        assertThat(refreshedLey.getEstado()).isEqualTo(EstadoLey.APROBADA);

        Voto refreshedVoto = votoRepository.findById(voto.getId()).orElseThrow();
        assertThat(refreshedVoto.getAsistencia()).isFalse();

        ReporteHistoricoDTO reporte = adminService.getHistoricoResumen();
        assertThat(reporte.getTotalLeyes()).isEqualTo(1);
        assertThat(reporte.getTotalVotos()).isEqualTo(1);
        assertThat(reporte.getVotosContra()).isEqualTo(0);
    }

    @Test
    void shouldReturnCachedExplanationIfPresent() {
        Ley ley = new Ley();
        ley.setTitulo("Ley con resumen");
        ley.setCodigo("LEY-CACHED-1");
        ley.setDescripcionOriginal("Texto original largo");
        ley.setDescripcionSimplificada("Esta es la explicacion simplificada ya guardada");
        ley.setEstado(EstadoLey.DEBATE);
        ley.setFechaIngreso(LocalDate.now());
        ley = leyRepository.save(ley);

        com.controlf.dto.ContenidoLeyDTO response = leyService.explicarLey(ley.getId());
        assertThat(response.getResumenEjecutivo()).isEqualTo("Esta es la explicacion simplificada ya guardada");
    }

    @Test
    void shouldThrowExceptionWhenApiKeyIsMissingAndNoCache() {
        Ley ley = new Ley();
        ley.setTitulo("Ley sin resumen");
        ley.setCodigo("LEY-NOCACHE-1");
        ley.setDescripcionOriginal("Texto original largo");
        ley.setDescripcionSimplificada(null);
        ley.setEstado(EstadoLey.DEBATE);
        ley.setFechaIngreso(LocalDate.now());
        ley = leyRepository.save(ley);

        final Integer leyId = ley.getId();
        org.junit.jupiter.api.Assertions.assertThrows(
                org.springframework.web.server.ResponseStatusException.class,
                () -> leyService.explicarLey(leyId)
        );
    }
}

