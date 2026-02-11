package com.morphology.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.morphology.dto.request.GenerateWordRequest;
import com.morphology.dto.response.ApiResponse;
import com.morphology.dto.response.GeneratedWordResponse;
import com.morphology.service.GenerationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/generate")
@RequiredArgsConstructor
public class GenerationController {
    
    private final GenerationService generationService;
    
    @PostMapping("/word")
    public ResponseEntity<ApiResponse<GeneratedWordResponse>> generateWord(
            @Valid @RequestBody GenerateWordRequest request) {
        
        log.info("POST /generate/word - root={}, scheme={}", request.getRoot(), request.getScheme());
        
        GeneratedWordResponse response = generationService.generateWord(
            request.getRoot(),
            request.getScheme()
        );
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PostMapping("/family")
    public ResponseEntity<ApiResponse<List<GeneratedWordResponse>>> generateFamily(
            @RequestBody GenerateWordRequest request) {
        
        log.info("POST /generate/family - root={}", request.getRoot());
        
        List<GeneratedWordResponse> family = generationService.generateFamily(request.getRoot());
        
        return ResponseEntity.ok(ApiResponse.success(family));
    }
}