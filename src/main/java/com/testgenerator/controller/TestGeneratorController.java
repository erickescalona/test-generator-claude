// TestGeneratorController.java

package com.testgenerator.controller;

import com.testgenerator.model.GenerationResult;
import com.testgenerator.service.TestGeneratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class TestGeneratorController {

    private final TestGeneratorService testGeneratorService;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/api/generate-tests")
    @ResponseBody
    public ResponseEntity<?> generateTests(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "includeAnalysis", defaultValue = "true") boolean includeAnalysis) {

        try {
            log.info("Iniciando generación de tests para archivo: {}", file.getOriginalFilename());

            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "El archivo está vacío"));
            }

            if (!file.getOriginalFilename().endsWith(".zip")) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Solo se aceptan archivos ZIP"));
            }

            // Generar tests
            GenerationResult result = testGeneratorService.generateTests(file, includeAnalysis);

            log.info("Tests generados exitosamente. Total de archivos: {}", 
                result.getGeneratedTests().size());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error al generar tests", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Error: " + e.getMessage()));
        }
    }

    @PostMapping("/api/generate-and-download")
    public ResponseEntity<?> generateAndDownload(
            @RequestParam("file") MultipartFile file) {

        try {
            log.info("Generando tests para descarga: {}", file.getOriginalFilename());

            byte[] zipContent = testGeneratorService.generateTestsZip(file);

            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=\"tests_generated.zip\"")
                .body(zipContent);

        } catch (Exception e) {
            log.error("Error al generar ZIP de descarga", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Error: " + e.getMessage()));
        }
    }

}
