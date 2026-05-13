package com.testgenerator.service;

import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiTestGenerator {

//    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent";
    private OkHttpClient httpClient;

    @Value("${gemini.api.key}")
    private String apiKey;

    private GoogleAiGeminiChatModel model;

//    public GeminiTestGenerator() {
//        // Configuramos el modelo Gemini 1.5 Flash (rápido y económico para código)
//        this.model = GoogleAiGeminiChatModel.builder()
//                .apiKey(apiKey)
//                .modelName("gemini-1.5-flash")
//                .temperature(0.2) // Baja temperatura para mayor precisión técnica
//                .build();
//    }

    @PostConstruct
    public void init() {
        this.httpClient = new OkHttpClient.Builder()
                .callTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
                .build();

        // Aquí apiKey ya NO es nulo
        this.model = GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gemini-pro")
                .logRequestsAndResponses(true)
                .temperature(0.2)
                .build();
    }

    /**
     * Genera el código de un Unit Test a partir de un String que contiene la clase Java.
     */
    public String generateUnitTest(String classSourceCode, String prompt) {
//        String prompt = """
//            Eres un experto en Java y Testing.
//            A continuación te proporciono una clase de Java.
//            Por favor, genera una clase de prueba unitaria usando JUnit 5 y Mockito si es necesario.
//            Asegúrate de cubrir casos de éxito y casos de borde.
//            Devuelve ÚNICAMENTE el código de la clase de prueba, sin explicaciones adicionales ni bloques markdown.
//
//            Clase a testear:
//            ---
//            %s
//            ---
//            """.formatted(classSourceCode);

        return model.chat(prompt);
    }

    public String buildPrompt(String javaSourceCode, String className) {
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

    public String enviarSolicitudAGemini(String prompt) throws IOException, JSONException {
        JSONObject payload = new JSONObject()
                .put("contents", new JSONArray()
                        .put(new JSONObject()
                                .put("parts", new JSONArray()
                                        .put(new JSONObject().put("text", prompt))
                                )
                        )
                );

        // Crear la solicitud HTTP
        RequestBody body = RequestBody.create(
                payload.toString(),
                MediaType.get("application/json")
        );

        Request request = new Request.Builder()
                .url(API_URL + "?key=" + apiKey)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        // Ejecutar la solicitud
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Error en la API: Código " + response.code() + " - " + response.message());
            }

            String jsonResponse = response.body().string();
            JSONObject json = new JSONObject(jsonResponse);

            // Extraer el texto de candidates[0].content.parts[0].text
            JSONArray candidates = json.getJSONArray("candidates");
            JSONObject candidate = candidates.getJSONObject(0);
            JSONObject content = candidate.getJSONObject("content");
            JSONArray parts = content.getJSONArray("parts");
            JSONObject part = parts.getJSONObject(0);
            String text = part.getString("text");

            // Limpiar bloques markdown ```java ... ``` o ``` si existen
            text = text.replaceAll("(?s)^```\\w*\\n?", "").replaceAll("(?s)```$", "").trim();

            return text;
        }
    }

}
