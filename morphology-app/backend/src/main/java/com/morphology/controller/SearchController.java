package com.morphology.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.morphology.dto.response.ApiResponse;
import com.morphology.model.MotDerive;
import com.morphology.model.NoeudAVL;
import com.morphology.model.Scheme;
import com.morphology.service.GenerationService;
import com.morphology.service.RootService;
import com.morphology.service.SchemeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {

    private final RootService rootService;
    private final SchemeService schemeService;
    private final GenerationService generationService;

    /**
     * POST /api/search/by-scheme
     * Recherche dans les dérivés stockés dans l'AVL — PAS de génération
     */
    @PostMapping("/by-scheme")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> searchByScheme(
            @RequestBody Map<String, String> body) {

        String schemeName = body.get("scheme");
        log.info("POST /search/by-scheme - scheme={}", schemeName);

        if (schemeName == null || schemeName.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Le nom du schème ne peut pas être vide."));
        }

        // Vérifier que le schème existe dans la table de hachage
        Scheme scheme = schemeService.searchScheme(schemeName);
        if (scheme == null) {
            return ResponseEntity.ok(
                ApiResponse.error("Schème '" + schemeName + "' non trouvé.")
            );
        }

        // Parcourir tous les noeuds AVL et chercher dans leurs dérivés stockés
        List<NoeudAVL> allNodes = rootService.getAllNodes();
        List<Map<String, String>> results = new ArrayList<>();

        for (NoeudAVL node : allNodes) {
            // Appliquer le schème à la racine pour savoir quel mot chercher
            String motCherche = scheme.appliquer(node.getRacine());
            if (motCherche == null) continue;

            // Chercher ce mot dans les dérivés stockés du noeud
            if (node.contientDerive(motCherche)) {
                MotDerive derive = node.rechercherDerive(motCherche);
                Map<String, String> entry = new LinkedHashMap<>();
                entry.put("root", node.getRacine());
                entry.put("word", motCherche);
                entry.put("scheme", schemeName);
                entry.put("frequence", String.valueOf(derive.getFrequence()));
                entry.put("rootType", node.getTypeMorphologique() != null
                        ? node.getTypeMorphologique().getNomArabe() : "");
                results.add(entry);
            }
        }

        log.info("✅ {} mots trouvés pour le schème '{}'", results.size(), schemeName);
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    
    /**
     * GET /api/roots/{root}/derivatives
     * Retourne tous les mots dérivés enregistrés pour une racine donnée
     */
    @GetMapping("/roots/{root}/derivatives")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDerivatives(
            @PathVariable String root) {

        log.info("GET /search/roots/{}/derivatives", root);

        NoeudAVL node = rootService.searchRoot(root);
        if (node == null) {
            return ResponseEntity.ok(ApiResponse.error("    الجذر '" + root + "' غير موجود."));
        }

        List<MotDerive> derives = node.getListeDerives();
        // Tri par fréquence décroissante
        derives.sort((a, b) -> b.getFrequence() - a.getFrequence());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("root", root);
        result.put("rootType", node.getTypeMorphologique() != null
                ? node.getTypeMorphologique().getNomArabe() : "غير محدد");
        result.put("frequenceRacine", node.getFrequenceRacine());
        result.put("nombreDerives", derives.size());
        result.put("derives", derives);

        return ResponseEntity.ok(ApiResponse.success(result));
    }
}