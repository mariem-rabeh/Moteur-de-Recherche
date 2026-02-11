package com.morphology.controller;

import com.morphology.dto.request.AddSchemeRequest;
import com.morphology.dto.response.ApiResponse;
import com.morphology.dto.response.SchemeResponse;
import com.morphology.service.SchemeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/schemes")
@RequiredArgsConstructor
public class SchemeController {
    
    private final SchemeService schemeService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<SchemeResponse>>> getSchemes() {
        log.info("GET /schemes");
        List<SchemeResponse> schemes = schemeService.getAllSchemes();
        return ResponseEntity.ok(ApiResponse.success(schemes));
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<String>> addScheme(@Valid @RequestBody AddSchemeRequest request) {
        log.info("POST /schemes - name={}", request.getName());
        
        try {
            boolean added = schemeService.addScheme(request.getName(), request.getRule());
            if (added) {
                return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Schème ajouté avec succès", request.getName()));
            } else {
                return ResponseEntity.ok(ApiResponse.error("Le schème existe déjà"));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PutMapping("/{name}")
    public ResponseEntity<ApiResponse<String>> updateScheme(
            @PathVariable String name,
            @RequestBody AddSchemeRequest request) {
        
        log.info("PUT /schemes/{}", name);
        
        try {
            boolean updated = schemeService.updateScheme(name, request.getRule());
            if (updated) {
                return ResponseEntity.ok(ApiResponse.success("Schème mis à jour", name));
            } else {
                return ResponseEntity.ok(ApiResponse.error("Schème non trouvé"));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @DeleteMapping("/{name}")
    public ResponseEntity<ApiResponse<String>> deleteScheme(@PathVariable String name) {
        log.info("DELETE /schemes/{}", name);
        
        boolean deleted = schemeService.deleteScheme(name);
        if (deleted) {
            return ResponseEntity.ok(ApiResponse.success("Schème supprimé", name));
        } else {
            return ResponseEntity.ok(ApiResponse.error("Schème non trouvé"));
        }
    }
    
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<Integer>> uploadSchemes(@RequestParam("file") MultipartFile file) {
        log.info("POST /schemes/upload - file={}", file.getOriginalFilename());
        
        try {
            int count = schemeService.loadSchemesFromFile(file);
            return ResponseEntity.ok(
                ApiResponse.success(count + " schèmes chargés avec succès", count)
            );
        } catch (Exception e) {
            log.error("Erreur lors du chargement", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Erreur: " + e.getMessage()));
        }
    }
}