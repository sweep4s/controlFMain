package com.controlf.service;

import com.controlf.db.repository.*;
import com.controlf.db.schema.Ley;
import com.controlf.db.schema.Voto;
import com.controlf.db.schema.VinculoPromesaLey;
import com.controlf.db.schema.enums.EstadoLey;
import com.controlf.db.schema.enums.TipoVoto;
import com.controlf.dto.DashboardStatsDTO;
import com.controlf.dto.MetricaItemDTO;
import com.controlf.dto.MetricasInteractivasDTO;
import com.controlf.dto.RecentActivityDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final PoliticoRepository politicoRepository;
    private final LeyRepository leyRepository;
    private final ComentarioRepository comentarioRepository;
    private final VinculoPromesaLeyRepository vinculoRepository;
    private final VotoRepository votoRepository;
    private final CalificacionRepository calificacionRepository;

    public DashboardStatsDTO getStats() {
        // Misma escala de coherencia que el resto del sistema (CUMPLE=100, AMBIGUO=50,
        // INCUMPLE=0), para que "Coherencia global" coincida con la sección de Métricas,
        // el repositorio y el CSV. scoreCoherencia además tolera nivelCoherencia null.
        Double avgCoherencia = vinculoRepository.findAll().stream()
                .mapToDouble(this::scoreCoherencia)
                .average()
                .orElse(0.0);

        var actividad = comentarioRepository.findAll().stream()
                .sorted((a, b) -> b.getFecha().compareTo(a.getFecha()))
                .limit(5)
                .map(c -> RecentActivityDTO.builder()
                        .tipo("COMENTARIO")
                        .usuario(c.getUsuario().getNombre())
                        .detalle("Comentó en: " + c.getTexto())
                        .fecha(c.getFecha().format(DateTimeFormatter.ofPattern("dd/MM HH:mm")))
                        .build())
                .collect(Collectors.toList());

        return DashboardStatsDTO.builder()
                .totalPoliticos(politicoRepository.count())
                .totalLeyes(leyRepository.count())
                .promedioCoherenciaGlobal(Math.round(avgCoherencia * 10.0) / 10.0)
                .totalComentarios(comentarioRepository.count())
                .actividadReciente(actividad)
                .build();
    }

    /**
     * Métricas de cumplimiento interactivas (CF-021). Aplica filtros de categoría, estado y rango
     * de fechas sobre datos reales y devuelve agregaciones listas para graficar y comparar.
     */
    public MetricasInteractivasDTO getMetricasInteractivas(String categoria, String estado, String desde, String hasta) {
        String categoriaFiltro = (categoria != null && !categoria.isBlank()) ? categoria.trim() : null;
        EstadoLey estadoFiltro = parseEstado(estado);
        LocalDate desdeFiltro = parseFecha(desde);
        LocalDate hastaFiltro = parseFecha(hasta);

        // Leyes filtradas por categoría/estado
        List<Ley> leyes = leyRepository.findAll().stream()
                .filter(l -> categoriaFiltro == null || categoriaFiltro.equalsIgnoreCase(l.getCategoria()))
                .filter(l -> estadoFiltro == null || l.getEstado() == estadoFiltro)
                .collect(Collectors.toList());
        Set<Integer> leyIds = leyes.stream().map(Ley::getId).collect(Collectors.toSet());

        // Leyes por estado
        Map<String, Long> porEstado = new LinkedHashMap<>();
        for (EstadoLey e : EstadoLey.values()) {
            porEstado.put(e.name(), 0L);
        }
        for (Ley l : leyes) {
            if (l.getEstado() != null) {
                porEstado.merge(l.getEstado().name(), 1L, Long::sum);
            }
        }

        // Leyes por categoría
        Map<String, Long> porCategoria = new LinkedHashMap<>();
        for (Ley l : leyes) {
            String c = (l.getCategoria() == null || l.getCategoria().isBlank()) ? "SIN CATEGORÍA" : l.getCategoria();
            porCategoria.merge(c, 1L, Long::sum);
        }

        // Votos de las leyes filtradas dentro del rango de fechas
        List<Voto> votos = votoRepository.findAll().stream()
                .filter(v -> v.getLey() != null && leyIds.contains(v.getLey().getId()))
                .filter(v -> enRango(v.getFechaVoto(), desdeFiltro, hastaFiltro))
                .collect(Collectors.toList());

        long favor = votos.stream().filter(v -> v.getTipoVoto() == TipoVoto.FAVOR).count();
        long contra = votos.stream().filter(v -> v.getTipoVoto() == TipoVoto.CONTRA).count();
        long abstencion = votos.stream().filter(v -> v.getTipoVoto() == TipoVoto.ABSTENCION).count();

        // Serie temporal de votos por mes (yyyy-MM), ordenada
        Map<String, Long> serie = new java.util.TreeMap<>();
        for (Voto v : votos) {
            if (v.getFechaVoto() != null) {
                String periodo = v.getFechaVoto().toLocalDate().toString().substring(0, 7);
                serie.merge(periodo, 1L, Long::sum);
            }
        }
        // Rellena los meses intermedios sin votos con 0, para que el eje temporal
        // sea continuo y no "salte" meses (un mes con muchos votos no debe quedar
        // pegado visualmente a otro mes lejano).
        serie = rellenarMesesContinuos(serie);

        // Coherencia (cumplimiento) por categoría a partir de los vínculos de las leyes filtradas
        Map<String, long[]> acumCoherencia = new LinkedHashMap<>(); // [sumaPonderada, conteo]
        double sumaGlobal = 0.0;
        long conteoGlobal = 0;
        for (VinculoPromesaLey v : vinculoRepository.findAll()) {
            if (v.getLey() == null || !leyIds.contains(v.getLey().getId())) {
                continue;
            }
            long score = scoreCoherencia(v);
            String c = (v.getLey().getCategoria() == null || v.getLey().getCategoria().isBlank())
                    ? "SIN CATEGORÍA" : v.getLey().getCategoria();
            long[] slot = acumCoherencia.computeIfAbsent(c, k -> new long[2]);
            slot[0] += score;
            slot[1] += 1;
            sumaGlobal += score;
            conteoGlobal += 1;
        }

        List<MetricaItemDTO> coherenciaPorCategoria = new ArrayList<>();
        for (Map.Entry<String, long[]> e : acumCoherencia.entrySet()) {
            double promedio = e.getValue()[1] > 0 ? Math.round((double) e.getValue()[0] / e.getValue()[1] * 10.0) / 10.0 : 0.0;
            coherenciaPorCategoria.add(new MetricaItemDTO(e.getKey(), promedio));
        }

        double promedioGlobal = conteoGlobal > 0 ? Math.round(sumaGlobal / conteoGlobal * 10.0) / 10.0 : 0.0;

        return MetricasInteractivasDTO.builder()
                .categoriaFiltro(categoriaFiltro)
                .estadoFiltro(estadoFiltro != null ? estadoFiltro.name() : null)
                .desde(desdeFiltro != null ? desdeFiltro.toString() : null)
                .hasta(hastaFiltro != null ? hastaFiltro.toString() : null)
                .totalLeyes(leyes.size())
                .totalVotos(votos.size())
                .promedioCoherenciaGlobal(promedioGlobal)
                .leyesPorEstado(toItems(porEstado))
                .leyesPorCategoria(toItems(porCategoria))
                .votosPorTipo(List.of(
                        new MetricaItemDTO("FAVOR", favor),
                        new MetricaItemDTO("CONTRA", contra),
                        new MetricaItemDTO("ABSTENCION", abstencion)))
                .coherenciaPorCategoria(coherenciaPorCategoria)
                .serieVotosPorMes(toItems(serie))
                .build();
    }

    private long scoreCoherencia(VinculoPromesaLey v) {
        if (v.getNivelCoherencia() == null) return 0;
        return switch (v.getNivelCoherencia()) {
            case CUMPLE -> 100;
            case AMBIGUO -> 50;
            case INCUMPLE -> 0;
        };
    }

    /**
     * Devuelve una serie mensual continua entre el primer y el último periodo presentes,
     * insertando en 0 los meses intermedios que no tengan votos. Preserva el orden
     * cronológico. Si la serie está vacía o tiene un solo mes, se devuelve tal cual.
     */
    private Map<String, Long> rellenarMesesContinuos(Map<String, Long> serie) {
        if (serie.size() < 2) {
            return serie;
        }
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM");
        YearMonth primero = YearMonth.parse(((java.util.TreeMap<String, Long>) serie).firstKey(), fmt);
        YearMonth ultimo = YearMonth.parse(((java.util.TreeMap<String, Long>) serie).lastKey(), fmt);

        Map<String, Long> continua = new LinkedHashMap<>();
        for (YearMonth ym = primero; !ym.isAfter(ultimo); ym = ym.plusMonths(1)) {
            String clave = ym.format(fmt);
            continua.put(clave, serie.getOrDefault(clave, 0L));
        }
        return continua;
    }

    private List<MetricaItemDTO> toItems(Map<String, Long> mapa) {
        List<MetricaItemDTO> items = new ArrayList<>();
        for (Map.Entry<String, Long> e : mapa.entrySet()) {
            items.add(new MetricaItemDTO(e.getKey(), e.getValue()));
        }
        return items;
    }

    private EstadoLey parseEstado(String estado) {
        if (estado == null || estado.isBlank() || estado.equalsIgnoreCase("TODOS")) {
            return null;
        }
        try {
            return EstadoLey.valueOf(estado.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private LocalDate parseFecha(String fecha) {
        if (fecha == null || fecha.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(fecha.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private boolean enRango(LocalDateTime fechaVoto, LocalDate desde, LocalDate hasta) {
        if (fechaVoto == null) {
            // Sin fecha solo se incluye cuando no se filtra por rango.
            return desde == null && hasta == null;
        }
        LocalDate fecha = fechaVoto.toLocalDate();
        if (desde != null && fecha.isBefore(desde)) {
            return false;
        }
        return hasta == null || !fecha.isAfter(hasta);
    }

    /**
     * Exportación detallada por actor político (CF-022): coherencia, reputación y desglose de votos.
     */
    public String exportPoliticosCsv() {
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Nombre,Partido,Cargo,Region,Coherencia %,Reputacion (1-5),Total Calificaciones,Total Votos,Favor,Contra,Abstencion\n");
        for (com.controlf.db.schema.Politico p : politicoRepository.findAll()) {
            Double coherencia = vinculoRepository.findAverageCoherenciaByPoliticoId(p.getId());
            Double reputacion = calificacionRepository.findAveragePuntajeByPoliticoId(p.getId());
            long totalCalificaciones = calificacionRepository.countByPoliticoId(p.getId());
            List<Voto> votos = votoRepository.findByPoliticoId(p.getId());
            long favor = votos.stream().filter(v -> v.getTipoVoto() == TipoVoto.FAVOR).count();
            long contra = votos.stream().filter(v -> v.getTipoVoto() == TipoVoto.CONTRA).count();
            long abstencion = votos.stream().filter(v -> v.getTipoVoto() == TipoVoto.ABSTENCION).count();

            csv.append(p.getId()).append(',')
                    .append(escape(p.getNombreCompleto())).append(',')
                    .append(escape(p.getPartidoPolitico())).append(',')
                    .append(escape(p.getCargoActual())).append(',')
                    .append(escape(p.getRegion())).append(',')
                    .append(coherencia != null ? Math.round(coherencia * 10.0) / 10.0 : 0.0).append(',')
                    .append(reputacion != null ? Math.round(reputacion * 10.0) / 10.0 : 0.0).append(',')
                    .append(totalCalificaciones).append(',')
                    .append(votos.size()).append(',')
                    .append(favor).append(',')
                    .append(contra).append(',')
                    .append(abstencion).append('\n');
        }
        return csv.toString();
    }

    /**
     * Exportación detallada por ley (CF-022): estado, categoría y resultado de la votación.
     */
    public String exportLeyesCsv() {
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Codigo,Titulo,Categoria,Estado,Proponente,Favor,Contra,Abstencion,Total Votos\n");
        for (Ley ley : leyRepository.findAll()) {
            long favor = votoRepository.countByLeyIdAndTipoVoto(ley.getId(), TipoVoto.FAVOR);
            long contra = votoRepository.countByLeyIdAndTipoVoto(ley.getId(), TipoVoto.CONTRA);
            long abstencion = votoRepository.countByLeyIdAndTipoVoto(ley.getId(), TipoVoto.ABSTENCION);

            csv.append(ley.getId()).append(',')
                    .append(escape(ley.getCodigo())).append(',')
                    .append(escape(ley.getTitulo())).append(',')
                    .append(escape(ley.getCategoria())).append(',')
                    .append(escape(ley.getEstado() != null ? ley.getEstado().name() : "SIN ESTADO")).append(',')
                    .append(escape(ley.getProponente())).append(',')
                    .append(favor).append(',')
                    .append(contra).append(',')
                    .append(abstencion).append(',')
                    .append(favor + contra + abstencion).append('\n');
        }
        return csv.toString();
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return '"' + value.replace("\"", "\"\"") + '"';
        }
        return value;
    }

    public String exportStatsCsv() {
        DashboardStatsDTO stats = getStats();
        StringBuilder csv = new StringBuilder();
        csv.append("Métrica,Valor\n");
        csv.append("Total Políticos,").append(stats.getTotalPoliticos()).append("\n");
        csv.append("Total Leyes,").append(stats.getTotalLeyes()).append("\n");
        csv.append("Promedio Coherencia Global,").append(stats.getPromedioCoherenciaGlobal()).append("\n");
        csv.append("Total Comentarios,").append(stats.getTotalComentarios()).append("\n");
        csv.append("Actividad Reciente,\n");
        for (RecentActivityDTO activity : stats.getActividadReciente()) {
            csv.append('"').append(activity.getTipo()).append(" - ")
                    .append(activity.getUsuario()).append(" - ")
                    .append(activity.getDetalle()).append('"')
                    .append(',').append(activity.getFecha()).append("\n");
        }
        return csv.toString();
    }
}
