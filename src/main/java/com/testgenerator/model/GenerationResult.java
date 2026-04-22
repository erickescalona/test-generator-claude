package com.testgenerator.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GenerationResult {
    private Map<String, String> generatedTests;
    private int totalFilesProcessed;
    private int totalTestsGenerated;
    private Date timestamp;
    private Map<String, List<ClassInfo>> classAnalysis;
    private Map<String, Integer> coverageEstimates;
}
