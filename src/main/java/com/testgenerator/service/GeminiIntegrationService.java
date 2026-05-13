package com.testgenerator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiIntegrationService {

    private final GeminiTestGenerator geminiTestGenerator;

    public String generateUnitTest(String javaSourceCode, String className) {
        try {
            String prompt = geminiTestGenerator.buildPrompt(javaSourceCode, className);
            log.debug("Enviando solicitud a Gemini para clase: {}", className);

            String response = geminiTestGenerator.enviarSolicitudAGemini(prompt);
            log.info("Respuesta recibida de Gemini para clase: {}", className);
            return response;

        } catch (Exception e) {
            log.error("Error llamando a Gemini API para clase: {}", className, e);
            return generateDefaultTest(className);
        }
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
