package com.morphology.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.morphology.dto.response.DecompositionResponse;
import com.morphology.dto.response.ValidationResponse;
import com.morphology.model.NoeudAVL;
import com.morphology.model.Root;
import com.morphology.model.RootType;
import com.morphology.model.Scheme;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ValidationService {

    private final RootService           rootService;
    private final SchemeService         schemeService;
    private final MorphoAnalyzer        morphoAnalyzer;
    private final TransformationService transformationService;

    // ================================================================
    // Valider qu'un mot appartient à une racine
    // ================================================================
    public ValidationResponse validateWord(String word, String root) {
        log.debug("Validation: mot={}, racine={}", word, root);

        ValidationResponse response = new ValidationResponse();
        response.setWord(word);
        response.setRoot(root);

        if (word == null || word.isBlank()) {
            response.setValid(false);
            response.setMessage("لا يمكن أن تكون الكلمة فارغة.");
            return response;
        }

        if (!rootService.rootExists(root)) {
            response.setValid(false);
            response.setMessage("الجذر '" + root + "' غير موجود في قاعدة البيانات.");
            return response;
        }

        Root rootObj = morphoAnalyzer.analyserRacine(root);
        if (!rootObj.isValid()) {
            response.setValid(false);
            response.setMessage("الجذر '" + root + "' غير صالح.");
            return response;
        }

        String wordNormalise = normaliserPourComparaison(word);

        List<String> schemeNames = schemeService.getSchemeNames();
        for (String schemeName : schemeNames) {
            Scheme scheme = schemeService.searchScheme(schemeName);
            if (scheme == null) continue;

            String motTransforme = genererMotTransforme(scheme, rootObj);
            if (motTransforme == null) continue;

            if (normaliserPourComparaison(motTransforme).equals(wordNormalise)) {
                response.setValid(true);
                response.setSchemeIdentified(schemeName);
                response.setMessage(
                    "الكلمة '" + word + "' مشتقة من الجذر '" + root +
                    "' على وزن '" + schemeName + "'."
                );
                rootService.addDerivativeToRoot(root, motTransforme);
                log.info("✅ Validation réussie: {} ← {} + {}", word, root, schemeName);
                return response;
            }
        }

        response.setValid(false);
        response.setMessage(
            "الكلمة '" + word + "' لا تنتمي إلى الجذر '" + root + "'."
        );
        return response;
    }

    // ================================================================
    // Identifier le schème d'un mot
    // ================================================================
    public String identifyScheme(String word, String root) {
        log.debug("Identification du schème: mot={}, racine={}", word, root);

        if (word == null || word.isBlank() || !rootService.rootExists(root)) return null;

        Root rootObj = morphoAnalyzer.analyserRacine(root);
        if (!rootObj.isValid()) return null;

        String wordNormalise = normaliserPourComparaison(word);

        List<String> schemeNames = schemeService.getSchemeNames();
        for (String schemeName : schemeNames) {
            Scheme scheme = schemeService.searchScheme(schemeName);
            if (scheme == null) continue;

            String motTransforme = genererMotTransforme(scheme, rootObj);
            if (motTransforme != null &&
                    normaliserPourComparaison(motTransforme).equals(wordNormalise)) {
                log.info("✅ Schème identifié: {} → {}", word, schemeName);
                return schemeName;
            }
        }

        return null;
    }

    // ================================================================
    // Décomposer un mot en racine + schème
    // ================================================================
    public DecompositionResponse decomposeWord(String word) {
        log.debug("Décomposition: {}", word);

        DecompositionResponse response = new DecompositionResponse();
        response.setWord(word);

        if (word == null || word.isBlank()) {
            response.setSuccess(false);
            response.setMessage("لا يمكن أن تكون الكلمة فارغة.");
            return response;
        }

        String wordNormalise = normaliserPourComparaison(word);
        List<NoeudAVL> allNodes = rootService.getAllNodes();

        if (allNodes == null || allNodes.isEmpty()) {
            response.setSuccess(false);
            response.setMessage("قاعدة البيانات فارغة، لا توجد جذور مسجّلة.");
            return response;
        }

        for (NoeudAVL node : allNodes) {
            String root  = node.getRacine();
            Root rootObj = obtenirRootAnalyse(root, node);
            if (rootObj == null || !rootObj.isValid()) continue;

            List<String> schemeNames = schemeService.getSchemeNames();
            for (String schemeName : schemeNames) {
                Scheme scheme = schemeService.searchScheme(schemeName);
                if (scheme == null) continue;

                String motTransforme = genererMotTransforme(scheme, rootObj);
                if (motTransforme != null &&
                        normaliserPourComparaison(motTransforme).equals(wordNormalise)) {
                    response.setSuccess(true);
                    response.setRoot(root);
                    response.setScheme(schemeName);
                    response.setMessage(
                        "تم تحليل الكلمة '" + word + "' : الجذر '" + root +
                        "' على وزن '" + schemeName + "'."
                    );
                    response.setAddedElements(extraireElementsAjoutes(scheme));
                    rootService.addDerivativeToRoot(root, motTransforme);
                    log.info("✅ Décomposition: {} ← {} + {}", word, root, schemeName);
                    return response;
                }
            }
        }

        response.setSuccess(false);
        response.setMessage(
            "تعذّر تحليل الكلمة '" + word +
            "'. لم يُعثر على جذر أو وزن مطابق."
        );
        return response;
    }

    // ================================================================
    // Trouver TOUTES les racines possibles d'un mot
    // ================================================================
    public List<DecompositionResponse> findAllPossibleRoots(String word) {
        log.debug("Recherche toutes racines pour: {}", word);

        List<DecompositionResponse> results = new ArrayList<>();
        if (word == null || word.isBlank()) return results;

        String wordNormalise = normaliserPourComparaison(word);
        List<NoeudAVL> allNodes = rootService.getAllNodes();

        if (allNodes == null || allNodes.isEmpty()) return results;

        for (NoeudAVL node : allNodes) {
            String root  = node.getRacine();
            Root rootObj = obtenirRootAnalyse(root, node);
            if (rootObj == null || !rootObj.isValid()) continue;

            List<String> schemeNames = schemeService.getSchemeNames();
            for (String schemeName : schemeNames) {
                Scheme scheme = schemeService.searchScheme(schemeName);
                if (scheme == null) continue;

                String motTransforme = genererMotTransforme(scheme, rootObj);
                if (motTransforme != null &&
                        normaliserPourComparaison(motTransforme).equals(wordNormalise)) {
                    DecompositionResponse result = new DecompositionResponse();
                    result.setSuccess(true);
                    result.setWord(word);
                    result.setRoot(root);
                    result.setScheme(schemeName);
                    result.setMessage(
                        "'" + word + "' ← جذر '" + root + "' على وزن '" + schemeName + "'"
                    );
                    result.setAddedElements(extraireElementsAjoutes(scheme));
                    results.add(result);
                }
            }
        }

        if (results.isEmpty())
            log.info("❌ لم يُعثر على جذر لـ '{}'", word);
        else
            log.info("✅ عُثر على {} نتيجة لـ '{}'", results.size(), word);

        return results;
    }

    // ================================================================
    // MÉTHODE CENTRALE — génère le mot brut PUIS applique les transformations
    // ================================================================
    private String genererMotTransforme(Scheme scheme, Root rootObj) {
        try {
            String motBrut = scheme.appliquer(rootObj.getRacine());
            if (motBrut == null || motBrut.isBlank()) return null;

            RootType type = rootObj.getType();
            if (type == null || type == RootType.SALIM) {
                return rootObj.isContientHamza()
                    ? transformationService.appliquerTransformations(
                        motBrut, type, rootObj, scheme.getId())
                    : motBrut;
            }

            return transformationService.appliquerTransformations(
                motBrut, type, rootObj, scheme.getId()
            );

        } catch (Exception e) {
            log.warn("⚠️ Erreur génération pour schème '{}': {}", scheme.getNom(), e.getMessage());
            return null;
        }
    }

    // ================================================================
    // Obtenir l'analyse d'une racine en utilisant le cache du noeud
    // ================================================================
    private Root obtenirRootAnalyse(String racine, NoeudAVL node) {
        try {
            if (node.getTypeMorphologique() != null) {
                Root rootObj = new Root(racine);
                if (!rootObj.isValid()) return null;
                rootObj.setType(node.getTypeMorphologique());
                rootObj.setContientHamza(node.isContientHamza());
                return rootObj;
            }
            return morphoAnalyzer.analyserRacine(racine);
        } catch (Exception e) {
            log.warn("⚠️ Erreur analyse racine '{}': {}", racine, e.getMessage());
            return null;
        }
    }

    // ================================================================
    // Normalisation pour comparaison — supprime les harakat
    // ================================================================
    private String normaliserPourComparaison(String mot) {
        if (mot == null) return "";
        return mot.replaceAll("[\\u064B-\\u065F]", "").trim();
    }

    // ================================================================
    // Extraire les éléments fixes ajoutés par le schème (non L1/L2/L3)
    // ================================================================
    private List<String> extraireElementsAjoutes(Scheme scheme) {
        List<String> elements = new ArrayList<>();
        String rule = scheme.getRegle();
        if (rule == null) return elements;

        for (int i = 0; i < rule.length(); i++) {
            char c = rule.charAt(i);
            if (c != '1' && c != '2' && c != '3') {
                elements.add(String.valueOf(c));
            }
        }
        return elements;
    }
}