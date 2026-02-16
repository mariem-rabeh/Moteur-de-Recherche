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
            log.debug("✅ Racine SALIM ou type null - Aucune transformation");
            return motGenere;
        }
        
        log.info("🔧 DÉBUT TRANSFORMATION - Type: {}, Mot: {}", type.getNomArabe(), motGenere);
        log.debug("   Racine: {} - Lettres: L1={}, L2={}, L3={}", 
            root.getRacine(), root.getL1(), root.getL2(), root.getL3());
        
        String resultat = motGenere;
        
        switch (type) {
            case AJWAF:
                resultat = transformerAjwaf(resultat, root);
                break;
            
            case MITHAL:
                resultat = transformerMithal(resultat, root);
                break;
            
            case NAQIS:
                resultat = transformerNaqis(resultat, root);
                break;
            
            case MOUDAAF:
                resultat = transformerMoudaaf(resultat, root);
                break;
                
            case MAHMOUZ:
                resultat = transformerMahmouz(resultat, root);
                break;
                
            case LAFEEF:
                // Double faiblesse - appliquer plusieurs transformations
                resultat = transformerLafeef(resultat, root);
                break;
            
            default:
                break;
        }
        
        if (!resultat.equals(motGenere)) {
            log.info("✅ TRANSFORMATION RÉUSSIE: {} → {} (Type: {})", 
                motGenere, resultat, type.getNomArabe());
        } else {
            log.warn("⚠️ AUCUNE TRANSFORMATION: {} (Type: {})", motGenere, type.getNomArabe());
        }
        
        return resultat;
    }
    
    private String transformerAjwaf(String mot, Root root) {
        String l2 = root.getL2();
        
        log.debug("🔍 AJWAF - Analyse du mot: {}", mot);
        log.debug("   Lettre faible L2: {}", l2);
        
        // Vérifier que L2 est bien une lettre faible
        if (!l2.equals("و") && !l2.equals("ي") && !l2.equals("ا")) {
            log.warn("⚠️ AJWAF - L2 '{}' n'est pas une lettre faible (و/ي/ا)", l2);
            return mot;
        }

        if (mot.startsWith("يَ") &&  (mot.contains("ْيُ") || mot.contains("ْوُ"))) {
            String motTransforme = mot
                    .replace("ْيُ", "ِي")
                    .replace("ْوُ", "ُو");

            log.info("   ✅ AJWAF (présent) - Transformation en voyelle longue: {} → {}", 
                    mot, motTransforme);

            return motTransforme;
        }


        
        // ========================================
        // CAS 1: و/ي avec FATHA → ا (sans fatha avant)
        // Exemples: قَوَل → قَال, بَيَع → بَاع
        // ========================================
        if (mot.contains("َ" + l2)) {
            String motTransforme = mot.replace("َ" + l2, "ا");
            log.info("   ✅ AJWAF - Transformation {}َ → ا: {} → {}", l2, mot, motTransforme);
            return motTransforme;
        }
        
        // ========================================
        // CAS 1bis: Schème فاعِل (ا + و/ي + kasra)
        // Exemples: قاوِل → قائِل, بايِع → بائِع
        // ========================================
        if (mot.contains("ا" + l2 + "ِ")) {
            String motTransforme = mot.replace("ا" + l2 + "ِ", "ائِ");
            log.info("   ✅ AJWAF - Transformation ا{}ِ → ائِ: {} → {}", l2, mot, motTransforme);
            return motTransforme;
        }
        
        // ========================================
        // CAS 2: و/ي avec KASRA
        // و → ي, mais ي reste ي
        // ========================================
        if (mot.contains("ِ" + l2)) {
            if (l2.equals("و")) {
                String motTransforme = mot.replace("ِو", "ِي");
                log.info("   ✅ AJWAF - Transformation ِو → ِي: {} → {}", mot, motTransforme);
                return motTransforme;
            } else {
                log.debug("   ℹ️ AJWAF - ِي reste inchangé");
                return mot;
            }
        }
        
        // ========================================
        // CAS 4: SUKUN + lettre faible dans schème مَفْعُول
        // Exemple: مَقْوُول → مَقُول
        // NE PAS appliquer pour يَبْيُعُ (doit rester tel quel)
        // ========================================
        if (mot.matches(".*ْ[وي]ُ.*")) {
            String motTransforme = mot.replaceAll("ْ[وي]ُ", "ُ");
            log.info("   ✅ AJWAF - Suppression après sukun: {} → {}", mot, motTransforme);
            return motTransforme;
        }

        
        
        // ========================================
        // CAS 3: و/ي avec DAMMA
        // Généralement و/ي reste (exemples: يَقُول, يَبِيع)
        // ========================================
        if (mot.contains("ُ" + l2)) {
            log.debug("   ℹ️ AJWAF - {}ُ reste inchangé dans ce contexte", l2);
            return mot;
        }
        
   
        log.debug("   ⚪ AJWAF - Aucune règle applicable pour: {}", mot);
        return mot;
    }
    
    private String transformerMithal(String mot, Root root) {
        String l1 = root.getL1();
        
        log.debug("🔍 MITHAL - Mot: {}, L1: {}", mot, l1);
        
        // La lettre initiale و disparaît dans certains schèmes
        if (l1.equals("و")) {
            // Pattern 1: يَوْعِل → يَعِل
            if (mot.startsWith("ي" + l1)) {
                String motTransforme = mot.replace("ي" + l1, "ي");
                log.info("   ✅ MITHAL - Suppression و après ي: {} → {}", mot, motTransforme);
                return motTransforme;
            }
            // Pattern 2: وْعِل → عِل (impératif)
            else if (mot.startsWith(l1 + "ْ")) {
                String motTransforme = mot.substring(1);
                log.info("   ✅ MITHAL - Suppression و initial: {} → {}", mot, motTransforme);
                return motTransforme;
            }
        }
        
        log.debug("   ⚪ MITHAL - Aucune transformation");
        return mot;
    }
    
    private String transformerNaqis(String mot, Root root) {
        String l3 = root.getL3();
        
        log.debug("🔍 NAQIS - Mot: {}, L3: {}", mot, l3);
        
        if (!l3.equals("ي") && !l3.equals("و")) {
            return mot;
        }
        
        // Cas 1: ي/و + FATHA → ى (alif maqsura)
        if (mot.endsWith("َ" + l3) || mot.endsWith(l3 + "َ")) {
            String motTransforme = mot.replaceAll("[يو]َ$", "ى");
            motTransforme = motTransforme.replaceAll("َ[يو]$", "َى");
            log.info("   ✅ NAQIS - {}َ → ى: {} → {}", l3, mot, motTransforme);
            return motTransforme;
        }
        
        // Cas 2: و + KASRA → ي
        if (mot.endsWith("ِ" + l3) && l3.equals("و")) {
            String motTransforme = mot.replace("ِو", "ِي");
            log.info("   ✅ NAQIS - ِو → ِي: {} → {}", mot, motTransforme);
            return motTransforme;
        }
        
        // Cas 3: SUKUN → tanwin kasra
        if (mot.endsWith("ْ" + l3)) {
            String motTransforme = mot.substring(0, mot.length() - 2) + "ٍ";
            log.info("   ✅ NAQIS - Suppression avec sukun: {} → {}", mot, motTransforme);
            return motTransforme;
        }
        
        log.debug("   ⚪ NAQIS - Aucune transformation");
        return mot;
    }
    
    private String transformerMoudaaf(String mot, Root root) {
        String l2 = root.getL2();
        String l3 = root.getL3();
        
        log.debug("🔍 MOUDAAF - Mot: {}, L2={}, L3={}", mot, l2, l3);
        
        if (!l2.equals(l3)) {
            log.warn("⚠️ MOUDAAF - L2 et L3 ne sont pas identiques!");
            return mot;
        }
        
        // Chercher les patterns de lettres doublées
        String[] patterns = {
            l2 + "َ" + l3 + "َ",  // Pattern avec fatha (ex: دَدَ)
            l2 + "ِ" + l3 + "َ",  // Pattern avec kasra puis fatha
            l2 + "ُ" + l3 + "َ",  // Pattern avec damma puis fatha
            l2 + "َ" + l3,        // Pattern avec fatha simple
            l2 + "ِ" + l3,        // Pattern avec kasra
            l2 + "ُ" + l3,        // Pattern avec damma
            l2 + l3               // Pattern sans voyelle
        };
        
        String[] replacements = {
            l2 + "َّ",   // دَدَ → دَّ
            l2 + "ِّ",
            l2 + "ُّ",
            l2 + "َّ",
            l2 + "ِّ",
            l2 + "ُّ",
            l2 + "ّ"
        };
        
        for (int i = 0; i < patterns.length; i++) {
            if (mot.contains(patterns[i])) {
                String motTransforme = mot.replace(patterns[i], replacements[i]);
                log.info("   ✅ MOUDAAF - Fusion avec Shadda: {} → {}", mot, motTransforme);
                return motTransforme;
            }
        }
        
        log.debug("   ⚪ MOUDAAF - Aucune transformation");
        return mot;
    }
    
    private String transformerMahmouz(String mot, Root root) {
        log.debug("🔍 MAHMOUZ - Mot: {}", mot);
        
        String resultat = mot;
        
        // Règles de support de la Hamza
        resultat = resultat.replaceAll("^اء", "أ");      // أ au début
        resultat = resultat.replaceAll("ُء", "ؤ");      // ؤ après damma
        resultat = resultat.replaceAll("ِء", "ئ");      // ئ après kasra
        resultat = resultat.replaceAll("أا", "آ");      // آ (madda)
        
        if (!resultat.equals(mot)) {
            log.info("   ✅ MAHMOUZ - Transformation appliquée: {} → {}", mot, resultat);
        }
        
        return resultat;
    }
    
    private String transformerLafeef(String mot, Root root) {
        log.debug("🔍 LAFEEF - Double faiblesse détectée");
        
        String l1 = root.getL1();
        String l2 = root.getL2();
        String l3 = root.getL3();
        
        boolean faibleL1 = estLettreFaible(l1);
        boolean faibleL2 = estLettreFaible(l2);
        boolean faibleL3 = estLettreFaible(l3);
        
        String resultat = mot;
        
        // Appliquer les transformations dans l'ordre
        if (faibleL1 && faibleL2) {
            log.debug("   Type: MITHAL + AJWAF");
            resultat = transformerMithal(resultat, root);
            resultat = transformerAjwaf(resultat, root);
        } else if (faibleL1 && faibleL3) {
            log.debug("   Type: MITHAL + NAQIS");
            resultat = transformerMithal(resultat, root);
            resultat = transformerNaqis(resultat, root);
        } else if (faibleL2 && faibleL3) {
            log.debug("   Type: AJWAF + NAQIS");
            resultat = transformerAjwaf(resultat, root);
            resultat = transformerNaqis(resultat, root);
        }
        
        log.info("   ✅ LAFEEF - Transformation combinée appliquée");
        return resultat;
    }
    
    private boolean estLettreFaible(String lettre) {
        return lettre.equals("و") || lettre.equals("ي") || lettre.equals("ا");
    }
}