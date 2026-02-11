package com.morphology.service;

import com.morphology.dto.response.DecompositionResponse;
import com.morphology.dto.response.ValidationResponse;
import com.morphology.model.NoeudAVL;
import com.morphology.model.Scheme;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ValidationService {
    
    private final RootService rootService;
    private final SchemeService schemeService;
    
    /**
     * Vérifier si un mot appartient à une racine
     */
    public ValidationResponse validateWord(String word, String root) {
        log.debug("Validation: mot={}, racine={}", word, root);
        
        ValidationResponse response = new ValidationResponse();
        response.setWord(word);
        response.setRoot(root);
        
        // Vérifier que la racine existe
        if (!rootService.rootExists(root)) {
            response.setValid(false);
            response.setMessage("La racine '" + root + "' n'existe pas dans la base.");
            return response;
        }
        
        // Tester avec tous les schèmes
        List<String> schemeNames = schemeService.getSchemeNames();
        for (String schemeName : schemeNames) {
            Scheme scheme = schemeService.searchScheme(schemeName);
            if (scheme != null) {
                String generated = scheme.appliquer(root);
                if (generated.equals(word)) {
                    response.setValid(true);
                    response.setSchemeIdentified(schemeName);
                    response.setMessage(
                        "Le mot '" + word + "' dérive bien de la racine '" + 
                        root + "' avec le schème '" + schemeName + "'."
                    );
                    rootService.addDerivativeToRoot(root, word);
                    return response;
                }
            }
        }
        
        response.setValid(false);
        response.setMessage("Le mot '" + word + "' ne dérive pas de la racine '" + root + "'.");
        return response;
    }
    
    /**
     * Identifier le schème d'un mot
     */
    public String identifyScheme(String word, String root) {
        log.debug("Identification du schème: mot={}, racine={}", word, root);
        
        if (!rootService.rootExists(root)) {
            return null;
        }
        
        List<String> schemeNames = schemeService.getSchemeNames();
        for (String schemeName : schemeNames) {
            Scheme scheme = schemeService.searchScheme(schemeName);
            if (scheme != null) {
                String generated = scheme.appliquer(root);
                if (generated.equals(word)) {
                    return schemeName;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Décomposer un mot en racine et schème
     */
    public DecompositionResponse decomposeWord(String word) {
        log.debug("Décomposition du mot: {}", word);
        
        DecompositionResponse response = new DecompositionResponse();
        response.setWord(word);
        
        // Tester toutes les racines
        List<NoeudAVL> allNodes = rootService.getAllNodes();
        for (NoeudAVL node : allNodes) {
            String root = node.getRacine();
            
            // Tester tous les schèmes
            List<String> schemeNames = schemeService.getSchemeNames();
            for (String schemeName : schemeNames) {
                Scheme scheme = schemeService.searchScheme(schemeName);
                if (scheme != null) {
                    String generated = scheme.appliquer(root);
                    if (generated.equals(word)) {
                        response.setSuccess(true);
                        response.setRoot(root);
                        response.setScheme(schemeName);
                        response.setMessage("Mot décomposé avec succès.");
                        response.setAddedElements(extractAddedElements(word, root, scheme));
                        rootService.addDerivativeToRoot(root, word);
                        return response;
                    }
                }
            }
        }
        
        response.setSuccess(false);
        response.setMessage(
            "Impossible de décomposer le mot '" + word + 
            "'. Aucune racine et schème correspondants trouvés."
        );
        return response;
    }
    
    /**
     * Trouver toutes les racines possibles d'un mot
     */
    public List<DecompositionResponse> findAllPossibleRoots(String word) {
        log.debug("Recherche de toutes les racines pour: {}", word);
        
        List<DecompositionResponse> results = new ArrayList<>();
        
        // Tester toutes les racines
        List<NoeudAVL> allNodes = rootService.getAllNodes();
        for (NoeudAVL node : allNodes) {
            String root = node.getRacine();
            
            // Tester tous les schèmes
            List<String> schemeNames = schemeService.getSchemeNames();
            for (String schemeName : schemeNames) {
                Scheme scheme = schemeService.searchScheme(schemeName);
                if (scheme != null) {
                    String generated = scheme.appliquer(root);
                    if (generated.equals(word)) {
                        DecompositionResponse result = new DecompositionResponse();
                        result.setSuccess(true);
                        result.setWord(word);
                        result.setRoot(root);
                        result.setScheme(schemeName);
                        result.setAddedElements(extractAddedElements(word, root, scheme));
                        results.add(result);
                    }
                }
            }
        }
        
        log.info("Trouvé {} racine(s) possible(s) pour: {}", results.size(), word);
        return results;
    }
    
    /**
     * Extraire les éléments ajoutés par le schème
     */
    private List<String> extractAddedElements(String word, String root, Scheme scheme) {
        List<String> elements = new ArrayList<>();
        String rule = scheme.getRegle();
        
        for (int i = 0; i < rule.length(); i++) {
            char c = rule.charAt(i);
            if (c != '1' && c != '2' && c != '3') {
                elements.add(String.valueOf(c));
            }
        }
        
        return elements;
    }
}