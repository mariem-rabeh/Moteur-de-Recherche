package com.morphology.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.morphology.dto.response.GeneratedWordResponse;
import com.morphology.model.Root;
import com.morphology.model.RootType;
import com.morphology.model.Scheme;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenerationService {
    
    private final RootService rootService;
    private final SchemeService schemeService;
    private final TransformationService transformationService;
    private final MorphoAnalyzer morphoAnalyzer;
    
    /**
     * Générer un mot à partir d'une racine et d'un schème
     * AVEC application des transformations morphologiques
     */
    public GeneratedWordResponse generateWord(String rootText, String schemeName) {
        log.debug("🔧 Génération: racine={}, schème={}", rootText, schemeName);
        
        // 1. Vérifier que la racine existe
        if (!rootService.rootExists(rootText)) {
            log.error("❌ Racine introuvable: {}", rootText);
            return new GeneratedWordResponse(
                null, rootText, schemeName, false,
                "Erreur : La racine '" + rootText + "' n'existe pas dans la base."
            );
        }
        
        // 2. Vérifier que le schème existe
        Scheme scheme = schemeService.searchScheme(schemeName);
        if (scheme == null) {
            log.error("❌ Schème introuvable: {}", schemeName);
            return new GeneratedWordResponse(
                null, rootText, schemeName, false,
                "Erreur : Le schème '" + schemeName + "' n'existe pas."
            );
        }
        
        // 3. Récupérer ou calculer le type morphologique
        RootType type = rootService.getRootType(rootText);
        
        // Si le type n'est pas dans le cache, l'analyser
        if (type == null) {
            Root analysis = morphoAnalyzer.analyserRacine(rootText);
            if (analysis.isValid()) {
                type = analysis.getType();
                log.info("Type morphologique calculé: {} pour {}", type.getNomArabe(), rootText);
            } else {
                log.error("❌ Analyse échouée: {}", analysis.getErrorMessage());
                return new GeneratedWordResponse(
                    null, rootText, schemeName, false, analysis.getErrorMessage()
                );
            }
        }
        
        // 4. Générer le mot de base (application mécanique du schème)
        String generatedWord = scheme.appliquer(rootText);
        if (generatedWord.startsWith("Erreur")) {
            log.error("❌ Application du schème échouée: {}", generatedWord);
            return new GeneratedWordResponse(
                null, rootText, schemeName, false, generatedWord
            );
        }
        
        log.debug("📝 Mot de base généré: {}", generatedWord);
        
        // 5. Créer l'objet Root pour les transformations
        Root root = new Root(rootText);
        root.setType(type);
        
        // 6. Appliquer les transformations morphologiques si nécessaire
        String finalWord = generatedWord;
        if (type != RootType.SALIM) {
            finalWord = transformationService.appliquerTransformations(
                generatedWord, type, root
            );
            
            if (!finalWord.equals(generatedWord)) {
                log.info("✨ Transformation appliquée: {} → {} (Type: {})",
                    generatedWord, finalWord, type.getNomArabe());
            } else {
                log.debug("⚪ Aucune transformation nécessaire pour ce schème");
            }
        } else {
            log.debug("✅ Racine SALIM - pas de transformation");
        }
        
        // 7. Ajouter le dérivé à la racine
        rootService.addDerivativeToRoot(rootText, finalWord);
        
        // 8. Préparer le message de succès
        String message = String.format("✅ Mot généré : %s", finalWord);
        if (type != RootType.SALIM) {
            message += String.format(" (Racine %s: %s)", 
                type.getNomFrancais(), type.getNomArabe());
        }
        
        log.info(message);
        return new GeneratedWordResponse(
            finalWord, rootText, schemeName, true, message
        );
    }
    
    /**
     * Générer toute la famille morphologique d'une racine
     */
    public List<GeneratedWordResponse> generateFamily(String root) {
        log.debug("👨‍👩‍👧‍👦 Génération de la famille pour: {}", root);
        
        List<GeneratedWordResponse> family = new ArrayList<>();
        
        if (!rootService.rootExists(root)) {
            GeneratedWordResponse error = new GeneratedWordResponse(
                null, root, null, false,
                "Erreur : La racine '" + root + "' n'existe pas."
            );
            family.add(error);
            return family;
        }
        
        List<String> schemeNames = schemeService.getSchemeNames();
        log.info("Génération de {} mots pour la racine {}", schemeNames.size(), root);
        
        for (String schemeName : schemeNames) {
            GeneratedWordResponse result = generateWord(root, schemeName);
            family.add(result);
        }
        
        log.info("✅ Famille générée: {} mots pour la racine {}", family.size(), root);
        return family;
    }
}