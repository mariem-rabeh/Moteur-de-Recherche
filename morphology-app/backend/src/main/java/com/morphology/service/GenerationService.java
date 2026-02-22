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

    private final RootService          rootService;
    private final SchemeService        schemeService;
    private final TransformationService transformationService;
    private final MorphoAnalyzer       morphoAnalyzer;

    // ================================================================
    // Générer un mot — racine + schème
    // ================================================================
    public GeneratedWordResponse generateWord(String rootText, String schemeName) {
        log.debug("🔧 Génération: racine={}, schème={}", rootText, schemeName);

        // Validation des entrées
        if (rootText == null || rootText.isBlank())
            return erreur(null, schemeName, "La racine ne peut pas être vide.");
        if (schemeName == null || schemeName.isBlank())
            return erreur(rootText, null, "Le nom du schème ne peut pas être vide.");

        // 1. Racine existe ?
        if (!rootService.rootExists(rootText))
            return erreur(rootText, schemeName,
                "La racine '" + rootText + "' n'existe pas dans la base.");

        // 2. Schème existe ?
        Scheme scheme = schemeService.searchScheme(schemeName);
        if (scheme == null)
            return erreur(rootText, schemeName,
                "Le schème '" + schemeName + "' n'existe pas.");

        // 3. Analyser la racine UNE SEULE FOIS — réutiliser le même objet Root
        Root root = morphoAnalyzer.analyserRacine(rootText);
        if (!root.isValid())
            return erreur(rootText, schemeName, root.getErrorMessage());

        RootType type = root.getType();

        // Mettre à jour le cache si nécessaire
        if (rootService.getRootType(rootText) == null)
            rootService.setRootType(rootText, type);

        // 4. Substitution brute du schème
        String motBrut = scheme.appliquer(rootText);
        if (motBrut == null || motBrut.isBlank()) {
            String msg = scheme.getLastError() != null ? scheme.getLastError()
                : "Le schème '" + schemeName + "' n'a pas pu être appliqué.";
            return erreur(rootText, schemeName, msg);
        }

        log.debug("📝 Mot brut (avant transformation): {}", motBrut);

        // 5. Transformations morphologiques — schemeId transmis pour MITHAL/AJWAF
        String motFinal = transformationService.appliquerTransformations(
            motBrut, type, root, scheme.getId()
        );

        // 6. Vérifier que le résultat n'est pas vide
        if (motFinal == null || motFinal.isBlank())
            return erreur(rootText, schemeName,
                "La transformation a produit un résultat vide. "
              + "Vérifier la compatibilité du schème avec ce type de racine.");

        if (!motFinal.equals(motBrut))
            log.info("✨ {} → {} ({})", motBrut, motFinal, type.getNomArabe());

        // 7. Enregistrer le dérivé
        rootService.addDerivativeToRoot(rootText, motFinal);

        String message = "✅ Mot généré : " + motFinal;
        if (type != RootType.SALIM)
            message += " (Racine " + type.getNomFrancais() + " : " + type.getNomArabe() + ")";

        return new GeneratedWordResponse(motFinal, rootText, schemeName, true, message);
    }

    // ================================================================
    // Générer la famille morphologique complète
    // ================================================================
    public List<GeneratedWordResponse> generateFamily(String rootText) {
        log.debug("👨‍👩‍👧‍👦 Famille pour: {}", rootText);
        List<GeneratedWordResponse> family = new ArrayList<>();

        if (rootText == null || rootText.isBlank()) {
            family.add(erreur(null, null, "La racine ne peut pas être vide."));
            return family;
        }
        if (!rootService.rootExists(rootText)) {
            family.add(erreur(rootText, null, "La racine '" + rootText + "' n'existe pas."));
            return family;
        }

        List<String> schemeNames = schemeService.getSchemeNames();
        if (schemeNames == null || schemeNames.isEmpty()) {
            family.add(erreur(rootText, null, "Aucun schème disponible."));
            return family;
        }

        log.info("Génération de {} mots pour {}", schemeNames.size(), rootText);
        int succes = 0;

        for (String schemeName : schemeNames) {
            try {
                GeneratedWordResponse res = generateWord(rootText, schemeName);
                family.add(res);
                if (res.isSuccess()) succes++;
            } catch (Exception e) {
                log.error("❌ Exception pour schème '{}': {}", schemeName, e.getMessage(), e);
                family.add(erreur(rootText, schemeName,
                    "Erreur inattendue : " + e.getMessage()));
            }
        }

        log.info("✅ {}/{} mots générés pour {}", succes, schemeNames.size(), rootText);
        return family;
    }

    private GeneratedWordResponse erreur(String racine, String scheme, String msg) {
        log.error("❌ {}", msg);
        return new GeneratedWordResponse(null, racine, scheme, false, "Erreur : " + msg);
    }
}