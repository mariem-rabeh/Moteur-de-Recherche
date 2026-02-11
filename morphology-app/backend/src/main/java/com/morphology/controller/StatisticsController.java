package com.morphology.controller;

import com.morphology.dto.response.ApiResponse;
import com.morphology.dto.response.StatisticsResponse;
import com.morphology.model.NoeudAVL;
import com.morphology.service.RootService;
import com.morphology.service.SchemeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
public class StatisticsController {
    
    private final RootService rootService;
    private final SchemeService schemeService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<StatisticsResponse>> getStatistics() {
        log.info("GET /statistics");
        
        int totalRoots = rootService.getRootCount();
        int totalSchemes = schemeService.getSchemeCount();
        
        // Calculer les statistiques des dérivés
        List<NoeudAVL> nodes = rootService.getAllNodes();
        int totalDerivatives = 0;
        int totalFrequency = 0;
        
        for (NoeudAVL node : nodes) {
            totalDerivatives += node.getNombreDerives();
            totalFrequency += node.getFrequenceTotaleDerives();
        }
        
        double avgDerivatives = totalRoots > 0 ? (double) totalDerivatives / totalRoots : 0;
        
        StatisticsResponse stats = new StatisticsResponse(
            totalRoots,
            totalSchemes,
            totalDerivatives,
            totalFrequency,
            avgDerivatives,
            totalFrequency
        );
        
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}