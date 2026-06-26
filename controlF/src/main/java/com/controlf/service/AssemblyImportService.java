package com.controlf.service;

import com.controlf.db.repository.LeyRepository;
import com.controlf.db.repository.PoliticoRepository;
import com.controlf.db.repository.VotoRepository;
import com.controlf.db.schema.Ley;
import com.controlf.db.schema.Politico;
import com.controlf.db.schema.Voto;
import com.controlf.db.schema.enums.EstadoLey;
import com.controlf.db.schema.enums.TipoVoto;
import com.controlf.dto.AssemblyMemberDTO;
import com.controlf.dto.ImportResultDTO;
import com.controlf.dto.VotingDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeFormatterBuilder;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssemblyImportService {

    private static final List<String> IRRELEVANT = List.of(
            "himno", "instalación", "quorum", "receso",
            "lectura", "acta", "homenaje", "minuto de silencio"
    );

    private static final String BASE_URL = "https://datos.asambleanacional.gob.ec/ecurul/assemblyman";

    private final LeyRepository leyRepository;
    private final VotoRepository votoRepository;
    private final PoliticoRepository politicoRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<AssemblyMemberDTO> getAssemblyMembers() {
        String url = BASE_URL + "/assembly?idType=1&onlyActive=false&idPeriod=8&idTerritorial=";
        ResponseEntity<List<AssemblyMemberDTO>> response = restTemplate.exchange(
                url, HttpMethod.GET, null, new ParameterizedTypeReference<>() {}
        );
        return response.getBody();
    }

    public List<VotingDTO> getVotings(Long memberId) throws Exception {
        String url = String.format(
                BASE_URL + "/findVotings?periodId=&assemblyMemberId=%d&dateIn=&dateOut=&sessionNumber=&theme=&proposal=&offset=0&limit=",
                memberId
        );

        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36")
                .header("Accept-Language", "en-US,en;q=0.8")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

if (response.body().trim().startsWith("<")) {
    log.info("Sin votaciones para memberId={}", memberId);
    return List.of();
}

log.debug("findVotings status={} bodyPreview={}", response.statusCode(),
        response.body().substring(0, Math.min(100, response.body().length())));

return objectMapper.readValue(response.body(),
        objectMapper.getTypeFactory().constructCollectionType(List.class, VotingDTO.class));
    }

    public ImportResultDTO importVotings(Long assemblyMemberId) {
        log.info("Iniciando importación para assemblyMemberId={}", assemblyMemberId);

        List<VotingDTO> votings;
        try {
            votings = getVotings(assemblyMemberId);
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener votaciones: " + e.getMessage(), e);
        }

        int found = votings == null ? 0 : votings.size();
        int imported = 0;
        int ignored = 0;
        int duplicates = 0;

        AssemblyMemberDTO member = getAssemblyMembers().stream()
                .filter(m -> assemblyMemberId.equals(m.getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Asambleísta no encontrado: " + assemblyMemberId));

        String nombre = (member.getFirstName() + " " + member.getLastname()).trim().toUpperCase();
        Politico politico = politicoRepository.findByNombreCompletoContainingIgnoreCase(nombre)
                .orElseThrow(() -> new RuntimeException("No se encontró político local para: " + nombre));

        if (votings != null) {
           DateTimeFormatter fmt = new DateTimeFormatterBuilder()
    .appendPattern("yyyy-MM-dd HH:mm:ss")
    .appendFraction(java.time.temporal.ChronoField.NANO_OF_SECOND, 0, 9, true)
    .toFormatter();
            for (VotingDTO v : votings) {
                if (!isRelevant(v)) {
                    ignored++;
                    log.debug("Votación ignorada (irrelevante): {}", v.getProposalDescription());
                    continue;
                }
                if (leyRepository.existsByExternalId(v.getId())) {
                    duplicates++;
                    log.debug("Duplicado omitido external_id={}", v.getId());
                    continue;
                }

                LocalDateTime ldt = LocalDateTime.parse(v.getVotingDate(), fmt);

                Ley ley = new Ley();
                ley.setTitulo(truncate(v.getProposalDescription(), 255));
                ley.setCodigo("AN-" + v.getId());
                ley.setTipoExpediente("VOTACION_ASAMBLEA");
                ley.setProponente(nombre);
                ley.setDescripcionOriginal(v.getThemeDescription());
                ley.setEstado(EstadoLey.DEBATE);
                ley.setFechaIngreso(ldt.toLocalDate());
                ley.setExternalId(v.getId());
                ley = leyRepository.save(ley);

                Voto voto = new Voto();
                voto.setPolitico(politico);
                voto.setLey(ley);
                voto.setTipoVoto(mapVote(v.getDescription()));
                voto.setAsistencia(true);
                voto.setFechaVoto(ldt);
                votoRepository.save(voto);

                imported++;
            }
        }

        log.info("Importación finalizada: encontradas={} importadas={} ignoradas={} duplicadas={}",
                found, imported, ignored, duplicates);
        return new ImportResultDTO(found, imported, ignored, duplicates);
    }

    private TipoVoto mapVote(String description) {
        if (description == null) {
            log.warn("Voto nulo recibido, asignando ABSTENCION");
            return TipoVoto.ABSTENCION;
        }
        return switch (description.trim().toUpperCase()) {
            case "SI" -> TipoVoto.FAVOR;
            case "NO" -> TipoVoto.CONTRA;
            case "ABSTENCION" -> TipoVoto.ABSTENCION;
            default -> {
                log.warn("Voto desconocido: '{}', asignando ABSTENCION", description);
                yield TipoVoto.ABSTENCION;
            }
        };
    }

    private boolean isRelevant(VotingDTO v) {
        String combined = ((v.getProposalDescription() == null ? "" : v.getProposalDescription()) + " " +
                (v.getThemeDescription() == null ? "" : v.getThemeDescription())).toLowerCase();
        return IRRELEVANT.stream().noneMatch(combined::contains);
    }

    private String truncate(String value, int maxLength) {
        if (value == null) return null;
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}