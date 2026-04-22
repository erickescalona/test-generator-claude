// GenerationResult.java
package com.testgenerator.model;

public class GenerationResult {
    private String status;
    private String message;
    
    public GenerationResult(String status, String message) {
        this.status = status;
        this.message = message;
    }
    
    public String getStatus() {
        return status;
    }
    
    public String getMessage() {
        return message;
    }
}