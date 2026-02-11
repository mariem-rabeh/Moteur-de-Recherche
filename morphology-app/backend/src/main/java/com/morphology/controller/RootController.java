package com.morphology.controller;

import com.morphology.dto.request.AddRootRequest;
import com.morphology.dto.response.ApiResponse;
import com.morphology.dto.response.RootsPageResponse;
import com.morphology.model.NoeudAVL;
import com.morphology.service.RootService;
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
@RequestMapping("/roots")
@RequiredArgsConstructor
public class RootController {
    
    private final RootService rootService;
    
    /**
     * GET /api/roots?search=...&page=1&limit=10
     * Obtenir la liste des racines (paginée)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<RootsPageResponse>> getRoots(
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int limit) {
        
        log.info("GET /roots - search={}, page={}, limit={}", search, page, limit);
        
        List<String> roots = rootService.getRoots(search, page, limit);
        int total = rootService.getTotalRoots(search);
        int totalPages = (int) Math.ceil((double) total / limit);
        
        RootsPageResponse response = new RootsPageResponse(
            roots, page, totalPages, total
        );
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * POST /api/roots
     * Ajouter une racine
     */
    @PostMapping
    public ResponseEntity<ApiResponse<String>> addRoot(@Valid @RequestBody AddRootRequest request) {
        log.info("POST /roots - root={}", request.getRoot());
        
        try {
            boolean added = rootService.addRoot(request.getRoot());
            if (added) {
                return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Racine ajoutée avec succès", request.getRoot()));
            } else {
                return ResponseEntity.ok(
                    ApiResponse.error("La racine existe déjà")
                );
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * DELETE /api/roots/{root}
     * Supprimer une racine
     */
    @DeleteMapping("/{root}")
    public ResponseEntity<ApiResponse<String>> deleteRoot(@PathVariable String root) {
        log.info("DELETE /roots/{}", root);
        
        boolean deleted = rootService.deleteRoot(root);
        if (deleted) {
            return ResponseEntity.ok(
                ApiResponse.success("Racine supprimée avec succès", root)
            );
        } else {
            return ResponseEntity.ok(
                ApiResponse.error("Racine non trouvée")
            );
        }
    }
    
    /**
     * GET /api/roots/{root}
     * Rechercher une racine
     */
    @GetMapping("/{root}")
    public ResponseEntity<ApiResponse<NoeudAVL>> searchRoot(@PathVariable String root) {
        log.info("GET /roots/{}", root);
        
        NoeudAVL node = rootService.searchRoot(root);
        if (node != null) {
            return ResponseEntity.ok(ApiResponse.success(node));
        } else {
            return ResponseEntity.ok(
                ApiResponse.error("Racine non trouvée")
            );
        }
    }
    
    /**
     * POST /api/roots/upload
     * Charger des racines depuis un fichier
     */
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<Integer>> uploadRoots(
            @RequestParam("file") MultipartFile file) {
        
        log.info("POST /roots/upload - file={}", file.getOriginalFilename());
        
        try {
            int count = rootService.loadRootsFromFile(file);
            return ResponseEntity.ok(
                ApiResponse.success(count + " racines chargées avec succès", count)
            );
        } catch (Exception e) {
            log.error("Erreur lors du chargement du fichier", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Erreur: " + e.getMessage()));
        }
    }
}