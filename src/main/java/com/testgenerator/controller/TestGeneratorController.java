// TestGeneratorController.java

package com.testgenerator.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/test-generator")
public class TestGeneratorController {

    @GetMapping("/generate")
    public ResponseEntity<String> generateTest() {
        // Implementation for generating a test
        return new ResponseEntity<>("Test generated successfully!", HttpStatus.OK);
    }

}