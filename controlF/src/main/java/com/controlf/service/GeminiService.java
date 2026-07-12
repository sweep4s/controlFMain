package com.controlf.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

@Service
@Slf4j
public class GeminiService {

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String model;

    public GeminiService(
            RestTemplate restTemplate,
            @Value("${app.gemini.api-key:}") String apiKey,
            @Value("${app.gemini.model:gemini-2.5-flash}") String model) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
        this.model = model;
    }

    public String generarExplicacion(String titulo, String descripcionOriginal) {
        if (apiKey == null || apiKey.isBlank()) {
            log.error("La API key de Gemini no está configurada.");
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "El servicio de traducción no está disponible en este momento (API key no configurada)."
            );
        }

        String prompt = String.format(
                "Explica esta ley ecuatoriana en lenguaje sencillo y claro para un ciudadano sin conocimientos legales, en 2-3 párrafos, sin tecnicismos. Título: %s. Texto: %s",
                titulo != null ? titulo : "",
                descripcionOriginal != null ? descripcionOriginal : ""
        );

        String url = String.format(
                "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent",
                model
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", apiKey);

        GeminiRequest requestBody = new GeminiRequest(prompt);
        HttpEntity<GeminiRequest> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<GeminiResponse> responseEntity = restTemplate.postForEntity(url, entity, GeminiResponse.class);
            GeminiResponse response = responseEntity.getBody();
            if (response != null && response.getCandidates() != null && !response.getCandidates().isEmpty()) {
                GeminiResponse.Candidate candidate = response.getCandidates().get(0);
                if (candidate.getContent() != null && candidate.getContent().getParts() != null && !candidate.getContent().getParts().isEmpty()) {
                    return candidate.getContent().getParts().get(0).getText();
                }
            }
            log.error("Respuesta de Gemini vacía o estructura inesperada: {}", response);
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo obtener una respuesta válida de la inteligencia artificial."
            );
        } catch (Exception e) {
            log.error("Error al llamar a la API de Gemini", e);
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Error al comunicarse con el servicio de traducción: " + e.getMessage()
            );
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GeminiRequest {
        private List<Content> contents;

        public GeminiRequest(String prompt) {
            this.contents = List.of(new Content(List.of(new Part(prompt))));
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Content {
            private List<Part> parts;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Part {
            private String text;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GeminiResponse {
        private List<Candidate> candidates;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Candidate {
            private Content content;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Content {
            private List<Part> parts;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Part {
            private String text;
        }
    }
}
