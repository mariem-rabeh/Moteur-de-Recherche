package com.morphology.service;

import com.morphology.dto.response.GeneratedWordResponse;
import com.morphology.model.Root;
import com.morphology.model.RootType;
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
    private final TransformationService transformationService;
    
    /**
     * Générer un mot à partir d'une racine et d'un schème
     * AVEC application des transformations morphologiques
     */
    public GeneratedWordResponse generateWord(String rootText, String schemeName) {
        log.debug("Génération: racine={}, schème={}", rootText, schemeName);
        
        // 1. Vérifier que la racine existe
        if (!rootService.rootExists(rootText)) {
            return new GeneratedWordResponse(
                null, rootText, schemeName, false,
                "Erreur : La racine '" + rootText + "' n'existe pas dans la base."
            );
        }
        
        // 2. Vérifier que le schème existe
        Scheme scheme = schemeService.searchScheme(schemeName);
        if (scheme == null) {
            return new GeneratedWordResponse(
                null, rootText, schemeName, false,
                "Erreur : Le schème '" + schemeName + "' n'existe pas."
            );
        }
        
        // 3. Récupérer le type morphologique (O(1) depuis le cache)
        RootType type = rootService.getRootType(rootText);
        
        // 4. Générer le mot de base
        String generatedWord = scheme.appliquer(rootText);
        if (generatedWord.startsWith("Erreur")) {
            return new GeneratedWordResponse(
                null, rootText, schemeName, false, generatedWord
            );
        }
        
        // 5. Appliquer les transformations morphologiques
        if (type != null && type != RootType.SALIM) {
            Root root = new Root(rootText);
            root.setType(type);
            
            String motTransforme = transformationService.appliquerTransformations(
                generatedWord, type, root
            );
            
            if (!motTransforme.equals(generatedWord)) {
                log.info("🔄 Transformation appliquée: {} → {} (Type: {})",
                         generatedWord, motTransforme, type.getNomArabe());
                generatedWord = motTransforme;
            }
        }
        
        // 6. Ajouter le dérivé à la racine
        rootService.addDerivativeToRoot(rootText, generatedWord);
        
        // 7. Préparer le message de succès
        String message = "Mot généré avec succès : " + generatedWord;
        if (type != null && type != RootType.SALIM) {
            message += " (Type racine: " + type.getNomArabe() + ")";
        }
        
        log.info("✅ {}", message);
        return new GeneratedWordResponse(
            generatedWord, rootText, schemeName, true, message
        );
    }
    
    /**
     * Générer toute la famille morphologique d'une racine
     */
    public List<GeneratedWordResponse> generateFamily(String root) {
        log.debug("Génération de la famille pour: {}", root);
        
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
        for (String schemeName : schemeNames) {
            GeneratedWordResponse result = generateWord(root, schemeName);
            family.add(result);
        }
        
        log.info("Famille générée: {} mots pour la racine {}", family.size(), root);
        return family;
    }
}