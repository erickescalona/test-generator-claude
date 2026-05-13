// TestGeneratorService.java

package com.testgenerator.service;

import com.testgenerator.model.ClassInfo;
import com.testgenerator.model.GenerationResult;
import com.testgenerator.model.MethodInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor

public class TestGeneratorService {
    
    private final ZipHandlerService zipHandlerService;
    private final JavaAnalysisService javaAnalysisService;
    private final GeminiIntegrationService geminiIntegrationService;

    @Value("${app.enable-code-analysis}")
    private boolean enableCodeAnalysis;

    @Value("${app.enable-coverage-estimation}")
    private boolean enableCoverageEstimation;

    /**
     * Genera tests unitarios a partir de un archivo ZIP
     */
    public GenerationResult generateTests(MultipartFile zipFile, boolean includeAnalysis) 
            throws Exception {

        // Descomprimir y extraer archivos Java
        Map<String, String> javaFiles = zipHandlerService.unzipFile(zipFile);

        Map<String, String> generatedTests = new HashMap<>();
        Map<String, List<ClassInfo>> classAnalysis = new HashMap<>();
        Map<String, Integer> coverageEstimates = new HashMap<>();

        int totalFiles = javaFiles.size();
        int processed = 0;

        for (Map.Entry<String, String> entry : javaFiles.entrySet()) {
            String filePath = entry.getKey();
            String sourceCode = entry.getValue();

            try {
                log.info("Procesando archivo {}/{}: {}", ++processed, totalFiles, filePath);

                // Analizar código
                List<ClassInfo> classes = javaAnalysisService.analyzeJavaFile(filePath, sourceCode);
                classAnalysis.put(filePath, classes);

                // Generar tests
                String testCode = geminiIntegrationService.generateUnitTest(sourceCode, 
                    extractClassName(classes));

                String testFileName = filePath.replace(".java", "Test.java");
                generatedTests.put(testFileName, testCode);

                // Calcular cobertura estimada
                if (enableCoverageEstimation && !classes.isEmpty()) {
                    List<MethodInfo> allMethods = classes.stream()
                        .flatMap(c -> c.getMethods().stream())
                        .collect(Collectors.toList());
                    
                    int coverage = javaAnalysisService.estimateCoverage(allMethods);
                    coverageEstimates.put(filePath, coverage);
                }

            } catch (Exception e) {
                log.error("Error procesando archivo: {}", filePath, e);
                generatedTests.put(filePath + ".error", "Error: " + e.getMessage());
            }
        }

        GenerationResult result = new GenerationResult();
        result.setGeneratedTests(generatedTests);
        result.setTotalFilesProcessed(totalFiles);
        result.setTotalTestsGenerated(generatedTests.size());
        result.setTimestamp(new Date());

        if (includeAnalysis && enableCodeAnalysis) {
            result.setClassAnalysis(classAnalysis);
            result.setCoverageEstimates(coverageEstimates);
        }

        return result;
    }

    /**
     * Genera tests y retorna un ZIP descargable
     */
    public byte[] generateTestsZip(MultipartFile zipFile) throws Exception {
        GenerationResult result = generateTests(zipFile, false);
        return zipHandlerService.createZip(result.getGeneratedTests());
    }

    private String extractClassName(List<ClassInfo> classes) {
        return classes.isEmpty() ? "Generated" : classes.get(0).getName();
    }
}
