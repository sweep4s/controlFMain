package com.controlf.backend;

import com.controlf.db.repository.ComentarioRepository;
import com.controlf.db.repository.LeyRepository;
import com.controlf.db.repository.PoliticoRepository;
import com.controlf.db.repository.UsuarioRepository;
import com.controlf.db.repository.VotoRepository;
import com.controlf.db.schema.Ley;
import com.controlf.db.schema.Politico;
import com.controlf.db.schema.Usuario;
import com.controlf.db.schema.Voto;
import com.controlf.db.schema.enums.EstadoLey;
import com.controlf.db.schema.enums.TipoVoto;
import com.controlf.dto.ActualizarCampoPoliticoRequestDTO;
import com.controlf.dto.AgendaLegislativaDTO;
import com.controlf.dto.AlertaDTO;
import com.controlf.dto.CalificacionRequestDTO;
import com.controlf.dto.ComentarioRequestDTO;
import com.controlf.dto.ComparacionVotosDTO;
import com.controlf.dto.CrearLeyRequestDTO;
import com.controlf.dto.DebateCiudadanoDTO;
import com.controlf.dto.DebateLegislativoDTO;
import com.controlf.dto.MetricasInteractivasDTO;
import com.controlf.dto.PerfilPoliticoDTO;
import com.controlf.service.AdminService;
import com.controlf.service.AlertaService;
import com.controlf.service.DashboardService;
import com.controlf.service.LeyService;
import com.controlf.service.PoliticoService;
import com.controlf.service.ValidacionService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Verificación de regresión de las funcionalidades añadidas por el reporte QA
 * (CF-013, CF-014, CF-015, CF-016, CF-021, CF-024/025 y CF-029). Ejercita cada flujo
 * de negocio de extremo a extremo contra la base H2 de pruebas.
 */
@SpringBootTest
@Transactional
class NuevasFuncionalidadesTests {

    @Autowired private PoliticoService politicoService;
    @Autowired private LeyService leyService;
    @Autowired private DashboardService dashboardService;
    @Autowired private AlertaService alertaService;
    @Autowired private ValidacionService validacionService;
    @Autowired private AdminService adminService;

    @Autowired private PoliticoRepository politicoRepository;
    @Autowired private LeyRepository leyRepository;
    @Autowired private VotoRepository votoRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private ComentarioRepository comentarioRepository;

    // ---------- CF-024 / CF-025: índice de reputación ----------
    @Test
    void reputationIndexIsComputedFromRatings() {
        Politico p = crearPolitico("Reputa Test");
        Usuario u = crearUsuario("reputa@test.dev");

        politicoService.addCalificacion(p.getId(), calificacion(4), u.getId());
        politicoService.addCalificacion(p.getId(), calificacion(2), u.getId());

        PerfilPoliticoDTO perfil = politicoService.getPoliticoProfile(p.getId());
        assertThat(perfil.getTotalCalificaciones()).isEqualTo(2);
        assertThat(perfil.getIndiceReputacion()).isEqualTo(3.0);
        assertThat(perfil.getEtiquetaReputacion()).isEqualTo("BUENA");
    }

    @Test
    void reputationIsZeroWithoutRatings() {
        Politico p = crearPolitico("Sin Calificaciones");
        PerfilPoliticoDTO perfil = politicoService.getPoliticoProfile(p.getId());
        assertThat(perfil.getTotalCalificaciones()).isZero();
        assertThat(perfil.getEtiquetaReputacion()).isEqualTo("SIN CALIFICACIONES");
    }

    // ---------- CF-016: comparación de patrones de voto ----------
    @Test
    void compareVotingPatterns() {
        Politico p1 = crearPolitico("Comparado Uno");
        Politico p2 = crearPolitico("Comparado Dos");
        Ley ley1 = crearLey("Ley Comun", "LEY-CMP-1", EstadoLey.APROBADA, "SALUD");
        Ley ley2 = crearLey("Ley Solo", "LEY-CMP-2", EstadoLey.DEBATE, "EDUCACION");

        crearVoto(p1, ley1, TipoVoto.FAVOR, true);
        crearVoto(p2, ley1, TipoVoto.CONTRA, true);
        crearVoto(p1, ley2, TipoVoto.FAVOR, true);

        ComparacionVotosDTO cmp = politicoService.compararPatronesVoto(List.of(p1.getId(), p2.getId()));
        assertThat(cmp.getPoliticos()).hasSize(2);
        assertThat(cmp.getLeyesEnComun()).isEqualTo(1);      // solo ley1 votada por ambos
        assertThat(cmp.getCoincidencias()).isEqualTo(0);     // FAVOR vs CONTRA
        assertThat(cmp.getIndiceCoincidencia()).isEqualTo(0.0);
    }

    @Test
    void compareRequiresAtLeastTwoPoliticians() {
        Politico p1 = crearPolitico("Uno Solo");
        assertThatThrownBy(() -> politicoService.compararPatronesVoto(List.of(p1.getId())))
                .isInstanceOf(ResponseStatusException.class);
    }

    // ---------- CF-013 / CF-014: agenda y debates ----------
    @Test
    void agendaAndDebatesExposeLegislativeActivity() {
        Ley ley = crearLey("Ley En Debate", "LEY-AGD-1", EstadoLey.DEBATE, "SEGURIDAD");
        ley.setDescripcionOriginal("Exposición de motivos oficial del expediente.");
        ley.setDescripcionSimplificada("Resumen simple.");
        ley.setFechaIngreso(LocalDate.now());
        leyRepository.save(ley);

        Politico p = crearPolitico("Votante Agenda");
        crearVoto(p, ley, TipoVoto.FAVOR, true);

        AgendaLegislativaDTO agenda = leyService.getAgendaLegislativa();
        assertThat(agenda.getTotalEventos()).isGreaterThanOrEqualTo(2); // ingreso + votación
        assertThat(agenda.getTotalIngresos()).isGreaterThanOrEqualTo(1);
        assertThat(agenda.getTotalVotaciones()).isGreaterThanOrEqualTo(1);

        List<DebateLegislativoDTO> debates = leyService.getDebatesLegislativos("EN_DEBATE");
        assertThat(debates).anyMatch(d -> "LEY-AGD-1".equals(d.getCodigo())
                && d.getResumenOficial() != null
                && d.getVotosFavor() == 1);
    }

    // ---------- CF-021: métricas interactivas ----------
    @Test
    void interactiveMetricsAggregateData() {
        Ley ley = crearLey("Ley Metricas", "LEY-MTR-1", EstadoLey.APROBADA, "SALUD");
        Politico p = crearPolitico("Votante Metricas");
        crearVoto(p, ley, TipoVoto.FAVOR, true);

        MetricasInteractivasDTO m = dashboardService.getMetricasInteractivas(null, null, null, null);
        assertThat(m.getVotosPorTipo()).hasSize(3);
        assertThat(m.getLeyesPorEstado()).isNotEmpty();
        assertThat(m.getTotalLeyes()).isGreaterThanOrEqualTo(1);

        // El filtro por categoría restringe el conjunto.
        MetricasInteractivasDTO filtrada = dashboardService.getMetricasInteractivas("SALUD", null, null, null);
        assertThat(filtrada.getCategoriaFiltro()).isEqualTo("SALUD");
        assertThat(filtrada.getLeyesPorCategoria()).allMatch(item -> item.getEtiqueta().equalsIgnoreCase("SALUD"));
    }

    // ---------- CF-015: alertas y suscripciones ----------
    @Test
    void subscriptionsAndAlertsFlow() {
        Usuario u = crearUsuario("alertas@test.dev");
        Ley ley = crearLey("Ley Alerta", "LEY-ALR-1", EstadoLey.DEBATE, "AMBIENTE");
        ley.setFechaIngreso(LocalDate.now());
        leyRepository.save(ley);

        alertaService.crearSuscripcion(u.getId(), null); // todas
        alertaService.crearSuscripcion(u.getId(), null); // duplicada -> ignorada
        assertThat(alertaService.listarSuscripciones(u.getId())).hasSize(1);

        List<AlertaDTO> alertas = alertaService.obtenerAlertas(u.getId());
        assertThat(alertas).anyMatch(a -> "LEY".equals(a.getTipo()) && a.getLeyId().equals(ley.getId().toString()));
    }

    // ---------- CF-029: validador (solo se publica lo aprobado) ----------
    @Test
    void validatorHidesRejectedComments() {
        Ley ley = crearLey("Ley Moderacion", "LEY-MOD-1", EstadoLey.DEBATE, "SALUD");
        Usuario u = crearUsuario("moderacion@test.dev");

        ComentarioRequestDTO req = new ComentarioRequestDTO();
        req.setTexto("Comentario a moderar");
        leyService.addComentario(ley.getId(), req, u.getId());

        DebateCiudadanoDTO antes = leyService.getDebateCiudadano(ley.getId());
        assertThat(antes.getComentarios()).hasSize(1); // por defecto APROBADO -> visible

        Integer comentarioId = comentarioRepository.findAll().get(0).getId();
        validacionService.moderar(comentarioId, "RECHAZADO", "contenido inapropiado");

        DebateCiudadanoDTO despues = leyService.getDebateCiudadano(ley.getId());
        assertThat(despues.getComentarios()).isEmpty(); // rechazado -> no público

        assertThat(validacionService.listarComentarios("RECHAZADO"))
                .anyMatch(c -> c.getId().equals(comentarioId));
        assertThat(validacionService.contarPorEstado().get("RECHAZADO")).isEqualTo(1L);
    }

    // ---------- CF-005: historial persistente de cambios ----------
    @Test
    void profileExposesPersistedChangeHistory() {
        Politico p = crearPolitico("Historial Persistente");
        p.setPatrimonioDeclarado(new BigDecimal("100"));
        politicoRepository.save(p);

        politicoService.actualizarCampoPolitico(p.getId(),
                ActualizarCampoPoliticoRequestDTO.builder().campo("patrimonio").valor("500").build());

        PerfilPoliticoDTO perfil = politicoService.getPoliticoProfile(p.getId());
        assertThat(perfil.getHistorialCambios()).isNotEmpty();
        assertThat(perfil.getHistorialCambios().get(0).getCampo()).isEqualTo("patrimonio");
        assertThat(perfil.getHistorialCambios().get(0).getValorAnterior()).isEqualTo("100");
        assertThat(perfil.getHistorialCambios().get(0).getValorNuevo()).isEqualTo("500");
    }

    // ---------- CF-007: registro manual de propuesta de ley ----------
    @Test
    void adminCanCreateLawProposal() {
        CrearLeyRequestDTO req = new CrearLeyRequestDTO();
        req.setTitulo("Ley de Prueba Manual");
        req.setCodigo("LEY-NEW-1");
        req.setEstado("APROBADA");
        req.setCategoria("SALUD");
        adminService.crearLey(req);

        Ley creada = leyRepository.findByCodigo("LEY-NEW-1").orElseThrow();
        assertThat(creada.getEstado()).isEqualTo(EstadoLey.APROBADA);
        assertThat(creada.getCategoria()).isEqualTo("SALUD");

        CrearLeyRequestDTO duplicada = new CrearLeyRequestDTO();
        duplicada.setTitulo("Otra");
        duplicada.setCodigo("LEY-NEW-1");
        assertThatThrownBy(() -> adminService.crearLey(duplicada))
                .isInstanceOf(ResponseStatusException.class);
    }

    // ---------- CF-011: trazabilidad del estado en el perfil de ley ----------
    @Test
    void lawProfileExposesRealState() {
        Ley ley = crearLey("Ley Estado", "LEY-EST-1", EstadoLey.APROBADA, "EDUCACION");
        var perfil = leyService.getFullPerfilLey(ley.getId());
        assertThat(perfil.getContenido().getEstado()).isEqualTo("APROBADA");
        assertThat(perfil.getContenido().getCategoria()).isEqualTo("EDUCACION");
    }

    // ---------- CF-023: la calificación 1-5 se publica junto al comentario ----------
    @Test
    void commentCarriesCitizenRating() {
        Ley ley = crearLey("Ley Rating", "LEY-RAT-1", EstadoLey.DEBATE, "SALUD");
        Usuario u = crearUsuario("rating@test.dev");

        ComentarioRequestDTO req = new ComentarioRequestDTO();
        req.setTexto("Buen proyecto");
        req.setPuntaje(4);
        leyService.addComentario(ley.getId(), req, u.getId());

        DebateCiudadanoDTO debate = leyService.getDebateCiudadano(ley.getId());
        assertThat(debate.getComentarios()).hasSize(1);
        assertThat(debate.getComentarios().get(0).getPuntaje()).isEqualTo(4);
    }

    // ---------- CF-022: exportación detallada ----------
    @Test
    void detailedExportsIncludeRows() {
        Politico p = crearPolitico("Export Politico");
        p.setPartidoPolitico("Partido X");
        politicoRepository.save(p);
        Ley ley = crearLey("Export Ley", "LEY-EXP-1", EstadoLey.APROBADA, "SALUD");
        crearVoto(p, ley, TipoVoto.FAVOR, true);

        String csvPoliticos = dashboardService.exportPoliticosCsv();
        assertThat(csvPoliticos).contains("Nombre").contains("Export Politico").contains("Partido X");

        String csvLeyes = dashboardService.exportLeyesCsv();
        assertThat(csvLeyes).contains("Codigo").contains("LEY-EXP-1").contains("APROBADA");
    }

    // ---------- helpers ----------
    private Politico crearPolitico(String nombre) {
        Politico p = new Politico();
        p.setNombreCompleto(nombre);
        p.setEstaActivo(true);
        p.setPromesas(new ArrayList<>());
        p.setVotos(new ArrayList<>());
        p.setComentarios(new ArrayList<>());
        p.setCalificaciones(new ArrayList<>());
        return politicoRepository.save(p);
    }

    private Ley crearLey(String titulo, String codigo, EstadoLey estado, String categoria) {
        Ley ley = new Ley();
        ley.setTitulo(titulo);
        ley.setCodigo(codigo);
        ley.setEstado(estado);
        ley.setCategoria(categoria);
        ley.setVotos(new ArrayList<>());
        ley.setComentarios(new ArrayList<>());
        ley.setCalificaciones(new ArrayList<>());
        ley.setVinculos(new ArrayList<>());
        return leyRepository.save(ley);
    }

    private Voto crearVoto(Politico p, Ley ley, TipoVoto tipo, boolean asistencia) {
        Voto voto = new Voto();
        voto.setPolitico(p);
        voto.setLey(ley);
        voto.setTipoVoto(tipo);
        voto.setAsistencia(asistencia);
        voto.setFechaVoto(LocalDateTime.now());
        return votoRepository.save(voto);
    }

    private Usuario crearUsuario(String email) {
        Usuario u = new Usuario();
        u.setNombre(email);
        u.setEmail(email);
        u.setPasswordHash("x");
        u.setRol(Usuario.Rol.CIUDADANO);
        u.setActivo(true);
        u.setFechaRegistro(LocalDateTime.now());
        return usuarioRepository.save(u);
    }

    private CalificacionRequestDTO calificacion(int puntaje) {
        CalificacionRequestDTO req = new CalificacionRequestDTO();
        req.setPuntaje(puntaje);
        return req;
    }
}
