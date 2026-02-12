package com.morphology.controller;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.springframework.http.MediaType;
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
    
    @PostMapping(value = "/check",
                 produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8",
                 consumes = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public ResponseEntity<ApiResponse<ValidationResponse>> validateWord(
            @Valid @RequestBody ValidateWordRequest request) {
        
        // ===== LOGS DE DÉBOGAGE DÉTAILLÉS =====
        log.info("========== DÉBUT VALIDATION ==========");
        
        // 1. Log basique
        log.info("POST /validate/check - word={}, root={}", request.getWord(), request.getRoot());
        
        // 2. Log de la longueur des chaînes
        log.debug("Word length: {}, Root length: {}", 
            request.getWord().length(), 
            request.getRoot().length());
        
        // 3. Log des bytes (pour voir l'encodage réel)
        log.debug("Word bytes (UTF-8): {}", 
            Arrays.toString(request.getWord().getBytes(StandardCharsets.UTF_8)));
        log.debug("Root bytes (UTF-8): {}", 
            Arrays.toString(request.getRoot().getBytes(StandardCharsets.UTF_8)));
        
        // 4. Log des codepoints (pour voir les caractères Unicode)
        log.debug("Word codepoints: {}", 
            request.getWord().codePoints().boxed().toArray());
        log.debug("Root codepoints: {}", 
            request.getRoot().codePoints().boxed().toArray());
        
        // 5. Vérification si c'est de l'arabe
        boolean isArabicWord = request.getWord().matches(".*[\\u0600-\\u06FF].*");
        boolean isArabicRoot = request.getRoot().matches(".*[\\u0600-\\u06FF].*");
        log.debug("Is Arabic? word={}, root={}", isArabicWord, isArabicRoot);
        
        // 6. Test de conversion
        try {
            String wordUtf8 = new String(request.getWord().getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
            String rootUtf8 = new String(request.getRoot().getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
            log.debug("After UTF-8 conversion - word={}, root={}", wordUtf8, rootUtf8);
        } catch (Exception e) {
            log.error("UTF-8 conversion error", e);
        }
        
        // 7. Afficher chaque caractère séparément
        StringBuilder wordChars = new StringBuilder();
        for (int i = 0; i < request.getWord().length(); i++) {
            char c = request.getWord().charAt(i);
            wordChars.append(String.format("[%c:U+%04X] ", c, (int)c));
        }
        log.debug("Word characters: {}", wordChars.toString());
        
        StringBuilder rootChars = new StringBuilder();
        for (int i = 0; i < request.getRoot().length(); i++) {
            char c = request.getRoot().charAt(i);
            rootChars.append(String.format("[%c:U+%04X] ", c, (int)c));
        }
        log.debug("Root characters: {}", rootChars.toString());
        
        log.info("========================================");
        
        // Appel du service
        ValidationResponse response = validationService.validateWord(
            request.getWord(),
            request.getRoot()
        );
        
        log.info("Validation result: valid={}", response.isValid());
        log.info("========== FIN VALIDATION ==========");
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PostMapping(value = "/decompose",
                 produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8",
                 consumes = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public ResponseEntity<ApiResponse<DecompositionResponse>> decomposeWord(
            @RequestBody ValidateWordRequest request) {
        
        // ===== LOGS DE DÉBOGAGE DÉTAILLÉS =====
        log.info("========== DÉBUT DÉCOMPOSITION ==========");
        
        log.info("POST /validate/decompose - word={}", request.getWord());
        
        log.debug("Word length: {}", request.getWord().length());
        
        log.debug("Word bytes (UTF-8): {}", 
            Arrays.toString(request.getWord().getBytes(StandardCharsets.UTF_8)));
        
        log.debug("Word codepoints: {}", 
            request.getWord().codePoints().boxed().toArray());
        
        boolean isArabic = request.getWord().matches(".*[\\u0600-\\u06FF].*");
        log.debug("Is Arabic? {}", isArabic);
        
        StringBuilder wordChars = new StringBuilder();
        for (int i = 0; i < request.getWord().length(); i++) {
            char c = request.getWord().charAt(i);
            wordChars.append(String.format("[%c:U+%04X] ", c, (int)c));
        }
        log.debug("Word characters: {}", wordChars.toString());
        
        log.info("========================================");
        
        DecompositionResponse response = validationService.decomposeWord(request.getWord());
        
        log.info("Decomposition result: root={}, scheme={}", 
            response.getRoot(), response.getScheme());
        log.info("========== FIN DÉCOMPOSITION ==========");
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PostMapping(value = "/find-roots",
                 produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8",
                 consumes = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public ResponseEntity<ApiResponse<List<DecompositionResponse>>> findAllRoots(
            @RequestBody ValidateWordRequest request) {
        
        // ===== LOGS DE DÉBOGAGE DÉTAILLÉS =====
        log.info("========== DÉBUT RECHERCHE RACINES ==========");
        
        log.info("POST /validate/find-roots - word={}", request.getWord());
        
        log.debug("Word length: {}", request.getWord().length());
        
        log.debug("Word bytes (UTF-8): {}", 
            Arrays.toString(request.getWord().getBytes(StandardCharsets.UTF_8)));
        
        boolean isArabic = request.getWord().matches(".*[\\u0600-\\u06FF].*");
        log.debug("Is Arabic? {}", isArabic);
        
        log.info("========================================");
        
        List<DecompositionResponse> results = validationService.findAllPossibleRoots(
            request.getWord()
        );
        
        log.info("Found {} possible roots", results.size());
        log.info("========== FIN RECHERCHE RACINES ==========");
        
        return ResponseEntity.ok(ApiResponse.success(results));
    }
}