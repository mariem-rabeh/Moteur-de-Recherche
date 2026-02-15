package com.morphology.service;

import org.springframework.stereotype.Service;

import com.morphology.model.Root;
import com.morphology.model.RootType;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TransformationService {
    
    /**
     * Applique les transformations morphologiques selon le type de racine
     */
    public String appliquerTransformations(String motGenere, RootType type, Root root) {
        if (type == null || type == RootType.SALIM) {
            return motGenere;
        }
        
        log.debug("Application des transformations pour type: {}", type);
        
        switch (type) {
            case AJWAF:
                return transformerAjwaf(motGenere, root);
            
            case MITHAL:
                return transformerMithal(motGenere, root);
            
            case NAQIS:
                return transformerNaqis(motGenere, root);
            
            case MOUDAAF:
                return transformerMoudaaf(motGenere, root);
            
            default:
                return motGenere;
        }
    }
    
    private String transformerAjwaf(String mot, Root root) {
        // Exemple: قاول → قائل (و → ئ quand précédé de ا)
        String l2 = root.getL2();
        
        if (mot.contains("ا" + l2)) {
            if (l2.equals("و")) {
                mot = mot.replace("او", "ائ");
                log.debug("Transformation AJWAF: او → ائ");
            } else if (l2.equals("ي")) {
                mot = mot.replace("اي", "ائ");
                log.debug("Transformation AJWAF: اي → ائ");
            }
        }
        
        return mot;
    }
    
    private String transformerMithal(String mot, Root root) {
        // La lettre initiale و ou ي peut disparaître
        // Cette logique dépend du schème utilisé
        return mot;
    }
    
    private String transformerNaqis(String mot, Root root) {
        // Exemple: رمي → رمى (ي final → ى)
        String l3 = root.getL3();
        
        if (mot.endsWith(l3) && l3.equals("ي")) {
            mot = mot.substring(0, mot.length() - 1) + "ى";
            log.debug("Transformation NAQIS: ي → ى");
        }
        
        return mot;
    }
    
    private String transformerMoudaaf(String mot, Root root) {
        // Exemple: مدد → مدّ (fusion avec Shadda)
        String l2 = root.getL2();
        String l3 = root.getL3();
        
        if (l2.equals(l3) && mot.contains(l2 + l3)) {
            mot = mot.replace(l2 + l3, l2 + "ّ");
            log.debug("Transformation MOUDAAF: {} + {} → {}ّ", l2, l3, l2);
        }
        
        return mot;
    }
}