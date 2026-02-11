package com.morphology.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.morphology.dto.request.ValidateWordRequest;
import com.morphology.dto.response.ApiResponse;
import com.morphology.dto.response.DecompositionResponse;
import com.morphology.dto.response.ValidationResponse;
import com.morphology.service.ValidationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/validate")
@RequiredArgsConstructor
public class ValidationController {
    
    private final ValidationService validationService;
    
    @PostMapping("/check")
    public ResponseEntity<ApiResponse<ValidationResponse>> validateWord(
            @Valid @RequestBody ValidateWordRequest request) {
        
        log.info("POST /validate/check - word={}, root={}", request.getWord(), request.getRoot());
        
        ValidationResponse response = validationService.validateWord(
            request.getWord(),
            request.getRoot()
        );
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PostMapping("/decompose")
    public ResponseEntity<ApiResponse<DecompositionResponse>> decomposeWord(
            @RequestBody ValidateWordRequest request) {
        
        log.info("POST /validate/decompose - word={}", request.getWord());
        
        DecompositionResponse response = validationService.decomposeWord(request.getWord());
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PostMapping("/find-roots")
    public ResponseEntity<ApiResponse<List<DecompositionResponse>>> findAllRoots(
            @RequestBody ValidateWordRequest request) {
        
        log.info("POST /validate/find-roots - word={}", request.getWord());
        
        List<DecompositionResponse> results = validationService.findAllPossibleRoots(
            request.getWord()
        );
        
        return ResponseEntity.ok(ApiResponse.success(results));
    }
}