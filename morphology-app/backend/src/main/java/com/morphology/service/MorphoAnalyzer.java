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
    
    private static final List<String> LETTRES_FAIBLES = Arrays.asList("و", "ي", "ا");
    private static final List<String> HAMZA_VARIANTS = Arrays.asList("أ", "إ", "آ", "ء", "ؤ", "ئ");
    
    private final Map<String, TransformationRule> reglesCache;
    
    public MorphoAnalyzer() {
        this.reglesCache = new HashMap<>();
        initialiserRegles();
    }
    
    private void initialiserRegles() {
        reglesCache.put("AJWAF_FAIL", new TransformationRule(
            RootType.AJWAF,
            "1ا2ِ3",
            "La lettre faible (و/ي) au milieu se transforme en Hamza (ء)",
            TransformationType.HAMZA_CONVERSION,
            2, "و", "ئ"
        ));
        
        reglesCache.put("NAQIS_MADI", new TransformationRule(
            RootType.NAQIS,
            "1َ2َ3",
            "La lettre faible finale devient Alif Maqsura (ى)",
            TransformationType.YAA_MAQSURA,
            3, "ي", "ى"
        ));
        
        reglesCache.put("MITHAL_AMR", new TransformationRule(
            RootType.MITHAL,
            "23",
            "La lettre faible initiale (و/ي) disparaît",
            TransformationType.DELETION,
            1, "و", ""
        ));
        
        reglesCache.put("MOUDAAF_SHADDA", new TransformationRule(
            RootType.MOUDAAF,
            "1َ2ّ",
            "Les deux lettres identiques fusionnent avec Shadda",
            TransformationType.SHADDA_ADDITION,
            2, "", "ّ"
        ));
        
        log.info("✅ {} règles de transformation chargées", reglesCache.size());
    }
    
    public Root analyserRacine(String racine) {
        log.debug("🔍 Analyse de la racine : {}", racine);
        
        Root root = new Root(racine);
        
        if (!root.isValid()) {
            log.warn("❌ Racine invalide : {}", root.getErrorMessage());
            return root;
        }
        
        RootType type = detecterType(root);
        root.setType(type);
        
        log.info("✅ Racine '{}' classée comme : {} ({})", 
                 racine, type.getNomFrancais(), type.getNomArabe());
        
        return root;
    }
    
    private RootType detecterType(Root root) {
        String l1 = root.getL1();
        String l2 = root.getL2();
        String l3 = root.getL3();
        
        boolean faibleL1 = estLettreFaible(l1);
        boolean faibleL2 = estLettreFaible(l2);
        boolean faibleL3 = estLettreFaible(l3);
        
        int nombreFaiblesses = (faibleL1 ? 1 : 0) + (faibleL2 ? 1 : 0) + (faibleL3 ? 1 : 0);
        if (nombreFaiblesses >= 2) {
            return RootType.LAFEEF;
        }
        
        if (l2.equals(l3)) {
            return RootType.MOUDAAF;
        }
        
        if (contientHamza(l1) || contientHamza(l2) || contientHamza(l3)) {
            return RootType.MAHMOUZ;
        }
        
        if (faibleL1 && (l1.equals("و") || l1.equals("ي"))) {
            return RootType.MITHAL;
        }
        
        if (faibleL2) {
            return RootType.AJWAF;
        }
        
        if (faibleL3) {
            return RootType.NAQIS;
        }
        
        return RootType.SALIM;
    }
    
    private boolean estLettreFaible(String lettre) {
        return LETTRES_FAIBLES.contains(lettre);
    }
    
    private boolean contientHamza(String lettre) {
        return HAMZA_VARIANTS.stream().anyMatch(lettre::contains);
    }
    
    public String genererExplication(Root root) {
        if (!root.isValid()) {
            return root.getErrorMessage();
        }
        
        switch (root.getType()) {
            case SALIM:
                return "✅ Racine saine : aucune transformation nécessaire.";
            
            case AJWAF:
                return String.format(
                    "⚠️ Racine concave : '%s' au milieu se transforme en 'ا' ou 'ء' selon le schème.",
                    root.getL2()
                );
            
            case MITHAL:
                return String.format(
                    "⚠️ Racine assimilée : '%s' initial disparaît dans certaines formes.",
                    root.getL1()
                );
            
            case NAQIS:
                return String.format(
                    "⚠️ Racine défectueuse : '%s' final devient 'ى' ou disparaît.",
                    root.getL3()
                );
            
            case MOUDAAF:
                return String.format(
                    "⚠️ Racine doublée : '%s' et '%s' fusionnent avec Shadda.",
                    root.getL2(), root.getL3()
                );
            
            case MAHMOUZ:
                return "⚠️ Racine hamzée : attention aux variations de la Hamza.";
            
            case LAFEEF:
                return "⚠️⚠️ Racine avec double faiblesse : transformations complexes.";
            
            default:
                return "";
        }
    }
    
    public TransformationRule getRegleTransformation(Root root, String patternScheme) {
        String cle = root.getType().name() + "_" + simplifierPattern(patternScheme);
        return reglesCache.get(cle);
    }
    
    private String simplifierPattern(String pattern) {
        if (pattern.contains("ا") && pattern.contains("ِ")) {
            return "FAIL";
        }
        return "GENERIC";
    }
    
    public Map<String, TransformationRule> getRegles() {
        return new HashMap<>(reglesCache);
    }
}