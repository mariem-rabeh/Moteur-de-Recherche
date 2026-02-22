package com.morphology.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.morphology.model.Root;
import com.morphology.model.RootType;
import com.morphology.model.TransformationRule;
import com.morphology.model.TransformationRule.TransformationType;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MorphoAnalyzer {

    // ا retiré : l'alef simple n'est jamais une consonne de racine
    private static final List<String> LETTRES_FAIBLES = Arrays.asList("و", "ي");
    private static final List<String> HAMZA_VARIANTS  =
            Arrays.asList("أ", "إ", "آ", "ء", "ؤ", "ئ");

    private final Map<String, TransformationRule> reglesCache;

    public MorphoAnalyzer() {
        this.reglesCache = new HashMap<>();
        initialiserRegles();
    }

    private void initialiserRegles() {
        reglesCache.put("AJWAF_FAIL", new TransformationRule(
            RootType.AJWAF, "1ا2ِ3",
            "Lettre faible au milieu → Hamza",
            TransformationType.HAMZA_CONVERSION, 2, "و", "ئ"));

        reglesCache.put("NAQIS_MADI", new TransformationRule(
            RootType.NAQIS, "1َ2َ3",
            "Lettre faible finale → ى",
            TransformationType.YAA_MAQSURA, 3, "ي", "ى"));

        reglesCache.put("MITHAL_AMR", new TransformationRule(
            RootType.MITHAL, "23",
            "Lettre faible initiale disparaît",
            TransformationType.DELETION, 1, "و", ""));

        reglesCache.put("MOUDAAF_SHADDA", new TransformationRule(
            RootType.MOUDAAF, "1َ2ّ",
            "Lettres identiques → Shadda",
            TransformationType.SHADDA_ADDITION, 2, "", "ّ"));

        log.info("✅ {} règles chargées", reglesCache.size());
    }

    // ================================================================
    // Point d'entrée principal
    // ================================================================
    public Root analyserRacine(String racine) {
        log.debug("🔍 Analyse: {}", racine);
        Root root = new Root(racine);
        if (!root.isValid()) {
            log.warn("❌ Invalide: {}", root.getErrorMessage());
            return root;
        }

        // Vérification complémentaire : alef simple détecté après construction
        if (root.getL1().equals("ا") || root.getL2().equals("ا") || root.getL3().equals("ا")) {
            root.setErrorMessage("Alef simple (ا) non autorisé en position consonantique.");
            root.setValid(false);
            return root;
        }

        RootType type = detecterType(root);
        root.setType(type);
        log.info("✅ '{}' → {} ({})", racine, type.getNomFrancais(), type.getNomArabe());
        return root;
    }

    // ================================================================
    // Détection du type — ordre critique
    // ================================================================
    private RootType detecterType(Root root) {
        String l1 = root.getL1(), l2 = root.getL2(), l3 = root.getL3();

        boolean faibleL1 = estLettreFaible(l1);
        boolean faibleL2 = estLettreFaible(l2);
        boolean faibleL3 = estLettreFaible(l3);

        // --- Hamza : flag orthographique, pas un type exclusif ---
        boolean hamza = contientHamza(l1) || contientHamza(l2) || contientHamza(l3);
        if (hamza) {
            root.setContientHamza(true);
            log.debug("⚠️ Hamza détectée — post-traitement activé");
        }

        // MOUDAAF avant LAFEEF : L2=L3 identiques même s'ils sont faibles
        if (l2.equals(l3)) return RootType.MOUDAAF;

        // LAFEEF : 2 lettres faibles ou plus
        int nb = (faibleL1?1:0) + (faibleL2?1:0) + (faibleL3?1:0);
        if (nb >= 2) return RootType.LAFEEF;

        if (faibleL1) return RootType.MITHAL;
        if (faibleL2) return RootType.AJWAF;
        if (faibleL3) return RootType.NAQIS;

        // Hamza seule, aucune lettre faible structurelle
        if (hamza) return RootType.MAHMOUZ;

        return RootType.SALIM;
    }

    private boolean estLettreFaible(String l) { return LETTRES_FAIBLES.contains(l); }
    private boolean contientHamza(String l)   {
        return HAMZA_VARIANTS.stream().anyMatch(l::contains);
    }

    // ================================================================
    // Explication lisible
    // ================================================================
    public String genererExplication(Root root) {
        if (!root.isValid()) return root.getErrorMessage();
        String h = root.isContientHamza() ? " + hamza (post-traitement requis)." : ".";
        switch (root.getType()) {
            case SALIM:   return "✅ Racine saine : substitution directe" + h;
            case AJWAF:   return "⚠️ Concave : '" + root.getL2() + "' au milieu → ا/ء selon schème" + h;
            case MITHAL:  return "⚠️ Assimilée : '" + root.getL1() + "' initial disparaît dans certaines formes" + h;
            case NAQIS:   return "⚠️ Défectueuse : '" + root.getL3() + "' final → ى ou disparaît" + h;
            case MOUDAAF: return "⚠️ Doublée : '" + root.getL2() + "'='" + root.getL3() + "' → shadda" + h;
            case MAHMOUZ: return "⚠️ Hamzée : variations orthographiques de la hamza requises.";
            case LAFEEF:  return "⚠️⚠️ Double faiblesse : transformations complexes combinées" + h;
            default:      return "";
        }
    }

    public TransformationRule getRegleTransformation(Root root, String pattern) {
        String cle = root.getType().name() + "_"
            + (pattern.contains("ا") && pattern.contains("ِ") ? "FAIL" : "GENERIC");
        return reglesCache.get(cle);
    }

    public Map<String, TransformationRule> getRegles() { return new HashMap<>(reglesCache); }
}