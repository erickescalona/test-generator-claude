package com.testgenerator.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClaudeIntegrationService {

    private static final String CLAUDE_API_URL = "https://api.anthropic.com/v1/messages";

    @Value("${claude.api.key}")
    private String apiKey;

    @Value("${claude.api.model}")
    private String model;

    @Value("${claude.api.max-tokens}")
    private int maxTokens;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Genera código de prueba unitaria usando Claude AI
     */
    public String generateUnitTest(String javaSourceCode, String className) {
        try {
            String prompt = buildPrompt(javaSourceCode, className);
            
            log.debug("Enviando solicitud a Claude para clase: {}", className);

            String response = callClaudeAPI(prompt);

            log.info("Respuesta recibida de Claude para clase: {}", className);
            return response;

        } catch (Exception e) {
            log.error("Error llamando a Claude API para clase: {}", className, e);
            return generateDefaultTest(className);
        }
    }

    private String callClaudeAPI(String prompt) throws Exception {
        HttpClient httpClient = HttpClients.createDefault();

        String requestBody = objectMapper.writeValueAsString(Map.of(
            "model", model,
            "max_tokens", maxTokens,
            "messages", new Object[]{
                Map.of(
                    "role", "user",
                    "content", prompt
                )
            }
        ));

        ClassicHttpRequest request = ClassicRequestBuilder.post(CLAUDE_API_URL)
            .setHeader("Content-Type", "application/json")
            .setHeader("x-api-key", apiKey)
            .setHeader("anthropic-version", "2023-06-01")
            .setEntity(new StringEntity(requestBody))
            .build();

        return httpClient.execute(request, response -> {
            HttpEntity entity = response.getEntity();
            String responseBody = new String(entity.getContent().readAllBytes(), StandardCharsets.UTF_8);
            
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            return jsonNode.get("content")
                .get(0)
                .get("text")
                .asText();
        });
    }

    private String buildPrompt(String javaSourceCode, String className) {
        return String.format(
            """
            Eres un experto en pruebas unitarias Java. Analiza la siguiente clase Java y genera 
            pruebas unitarias COMPLETAS y EXHAUSTIVAS usando JUnit 5 y Mockito.
            
            REQUISITOS:
            1. Usar JUnit 5 (org.junit.jupiter.api.Test)
            2. Usar Mockito para mockear dependencias
            3. Cubrir casos normales, casos límite y excepciones
            4. Usar métodos descriptivos (ej: testAddWhenPositiveNumbers)
            5. Incluir @BeforeEach para setup
            6. Incluir assertions significativas
            7. No incluir System.out.println
            8. Cobertura mínima del 80%% de métodos
            
            CLASE A PROBAR:
            ```java
            %s
            ```
            
            Genera SOLO el código del test, sin explicaciones adicionales. El nombre de la clase de test 
            debe ser %sTest. Asegúrate de que sea compilable y ejecutable.
            """,
            javaSourceCode,
            className
        );
    }

    private String generateDefaultTest(String className) {
        return String.format(
            """
            import org.junit.jupiter.api.BeforeEach;
            import org.junit.jupiter.api.Test;
            import static org.junit.jupiter.api.Assertions.*;
            
            class %sTest {
            
                private %s instance;
            
                @BeforeEach
                void setUp() {
                    instance = new %s();
                }
            
                @Test
                void testBasicFunctionality() {
                    assertNotNull(instance);
                }
            }
            """,
            className,
            className,
            className
        );
    }
}
