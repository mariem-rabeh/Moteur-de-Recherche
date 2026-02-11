package com.morphology.service;

import com.morphology.dto.response.GeneratedWordResponse;
import com.morphology.model.Scheme;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenerationService {
    
    private final RootService rootService;
    private final SchemeService schemeService;
    
    /**
     * Générer un mot à partir d'une racine et d'un schème
     */
    public GeneratedWordResponse generateWord(String root, String schemeName) {
        log.debug("Génération: racine={}, schème={}", root, schemeName);
        
        // Vérifier que la racine existe
        if (!rootService.rootExists(root)) {
            return new GeneratedWordResponse(
                null, root, schemeName, false,
                "Erreur : La racine '" + root + "' n'existe pas dans la base."
            );
        }
        
        // Vérifier que le schème existe
        Scheme scheme = schemeService.searchScheme(schemeName);
        if (scheme == null) {
            return new GeneratedWordResponse(
                null, root, schemeName, false,
                "Erreur : Le schème '" + schemeName + "' n'existe pas."
            );
        }
        
        // Générer le mot
        String generatedWord = scheme.appliquer(root);
        if (generatedWord.startsWith("Erreur")) {
            return new GeneratedWordResponse(
                null, root, schemeName, false, generatedWord
            );
        }
        
        // Ajouter le dérivé à la racine
        rootService.addDerivativeToRoot(root, generatedWord);
        
        log.info("Mot généré: {}", generatedWord);
        return new GeneratedWordResponse(
            generatedWord, root, schemeName, true,
            "Mot généré avec succès : " + generatedWord
        );
    }
    
    /**
     * Générer toute la famille morphologique d'une racine
     */
    public List<GeneratedWordResponse> generateFamily(String root) {
        log.debug("Génération de la famille pour: {}", root);
        
        List<GeneratedWordResponse> family = new ArrayList<>();
        
        // Vérifier que la racine existe
        if (!rootService.rootExists(root)) {
            GeneratedWordResponse error = new GeneratedWordResponse(
                null, root, null, false,
                "Erreur : La racine '" + root + "' n'existe pas."
            );
            family.add(error);
            return family;
        }
        
        // Générer avec tous les schèmes
        List<String> schemeNames = schemeService.getSchemeNames();
        for (String schemeName : schemeNames) {
            GeneratedWordResponse result = generateWord(root, schemeName);
            family.add(result);
        }
        
        log.info("Famille générée: {} mots pour la racine {}", family.size(), root);
        return family;
    }
}