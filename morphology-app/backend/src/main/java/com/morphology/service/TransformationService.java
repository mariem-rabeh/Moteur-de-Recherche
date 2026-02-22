package com.morphology.service;

import org.springframework.stereotype.Service;

import com.morphology.model.Root;
import com.morphology.model.RootType;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TransformationService {

    // Diacritiques en constantes Unicode explicites
    private static final char FATHA  = '\u064E'; // َ
    private static final char KASRA  = '\u0650'; // ِ
    private static final char DAMMA  = '\u064F'; // ُ
    private static final char SUKUN  = '\u0652'; // ْ
    private static final char SHADDA = '\u0651'; // ّ

    // Voyelles longues
    private static final char ALEF   = '\u0627'; // ا
    private static final char WAW    = '\u0648'; // و
    private static final char YAA    = '\u064A'; // ي

    // Hamzas
    private static final char HAMZA_ISOLE  = '\u0621'; // ء
    private static final char HAMZA_ALEF   = '\u0623'; // أ
    private static final char HAMZA_ALEF_B = '\u0625'; // إ
    private static final char HAMZA_WAW    = '\u0624'; // ؤ
    private static final char HAMZA_YAA    = '\u0626'; // ئ
    private static final char MADDA        = '\u0622'; // آ

    // ================================================================
    // Point d'entrée
    // ================================================================
    public String appliquerTransformations(String mot, RootType type,
                                           Root root, String schemeId) {
        if (mot == null || mot.isBlank()) return mot;

        // FIX : normaliser alef maqsura ى (\u0649) → ي (\u064A) en entrée.
        // Le ى légitime en finale sera reposé par transformerNaqis (CAS 1/1b).
        mot = mot.replace('\u0649', '\u064A');

        if (type == null || type == RootType.SALIM) {
            return root.isContientHamza() ? postTraitementHamza(mot) : mot;
        }

        log.info("🔧 TRANSFORMATION — Type: {}, Mot: {}, Schème: {}",
            type.getNomArabe(), mot, schemeId);

        String resultat = mot;

        switch (type) {
            case MAHMOUZ: resultat = postTraitementHamza(resultat);               break;
            case MOUDAAF: resultat = transformerMoudaaf(resultat, root);          break;
            case MITHAL:  resultat = transformerMithal(resultat, root, schemeId); break;
            case AJWAF:   resultat = transformerAjwaf(resultat, root, schemeId);  break;
            case NAQIS:   resultat = transformerNaqis(resultat, root, schemeId);  break;
            case LAFEEF:  resultat = transformerLafeef(resultat, root, schemeId); break;
            default: break;
        }

        if (root.isContientHamza() && type != RootType.MAHMOUZ)
            resultat = postTraitementHamza(resultat);

        // FIX : supprimer la damma finale résiduelle du schème
        // Ex: يَرْوِيُ (Ajwaf + يَفْعِلُ) → يَرْوِي
        // Ce nettoyage s'applique à tous les types car la damma vient du schème,
        // pas de la transformation morphologique.
        resultat = supprimerDammaFinale(resultat);

        if (!resultat.equals(mot))
            log.info("✅ {} → {}", mot, resultat);
        else
            log.warn("⚠️ Aucune transformation: {} ({})", mot, type.getNomArabe());

        return resultat;
    }

    // ================================================================
    // MAHMOUZ — Post-traitement orthographique de la Hamza
    // ================================================================
    private String normaliserHamzas(String s) {
        StringBuilder sb = new StringBuilder(s);
        for (int i = 0; i < sb.length(); i++) {
            char c = sb.charAt(i);
            if (c == HAMZA_ALEF   ||
                c == HAMZA_ALEF_B ||
                c == HAMZA_WAW    ||
                c == HAMZA_YAA) {
                sb.setCharAt(i, HAMZA_ISOLE);
            }
        }
        return sb.toString();
    }

    private String postTraitementHamza(String mot) {
        if (mot == null || mot.isEmpty()) return mot;

        String res = mot;
        res = res.replace("" + HAMZA_ALEF + FATHA  + HAMZA_ALEF + SUKUN, "" + MADDA);
        res = res.replace("" + HAMZA_ALEF + FATHA  + HAMZA_ALEF + FATHA, "" + MADDA);
        res = res.replace("" + HAMZA_ALEF + FATHA  + ALEF,               "" + MADDA);
        res = res.replace("" + HAMZA_ALEF + SUKUN  + ALEF,               "" + MADDA);
        res = res.replace("" + HAMZA_ALEF + ALEF,                        "" + MADDA);

        res = normaliserHamzas(res);

        StringBuilder sb = new StringBuilder(res);

        for (int i = 0; i < sb.length(); i++) {
            if (sb.charAt(i) != HAMZA_ISOLE) continue;

            char avant = i > 0             ? sb.charAt(i - 1) : '\0';
            char apres = i < sb.length()-1 ? sb.charAt(i + 1) : '\0';

            // R4 : ء finale après voyelle longue → reste ء isolée
            if (i == sb.length() - 1 &&
                    (avant == ALEF || avant == WAW || avant == YAA)) {
                continue;
            }

            // R1 : kasra (priorité maximale)
            if (avant == KASRA || apres == KASRA) {
                sb.setCharAt(i, HAMZA_YAA);
                continue;
            }

            // R2 : damma
            if (avant == DAMMA || apres == DAMMA) {
                sb.setCharAt(i, HAMZA_WAW);
                continue;
            }

            // R3 : début de mot
            if (i == 0) {
                sb.setCharAt(i, apres == KASRA ? HAMZA_ALEF_B : HAMZA_ALEF);
                continue;
            }

            // Défaut : fatha → أ
            if (apres == FATHA || avant == FATHA) {
                sb.setCharAt(i, HAMZA_ALEF);
            }
        }

        res = sb.toString();
        if (!res.equals(mot)) log.info("   ✅ Hamza: {} → {}", mot, res);
        return res;
    }

    // ================================================================
    // MOUDAAF — Fusion L2+L3 adjacents → L2 + Shadda
    // ================================================================
    private String transformerMoudaaf(String mot, Root root) {
        String l2 = root.getL2(), l3 = root.getL3();
        if (!l2.equals(l3)) { log.warn("⚠️ MOUDAAF L2≠L3"); return mot; }

        char cible = l2.charAt(0);
        StringBuilder sb = new StringBuilder(mot);

        for (int i = 0; i < sb.length() - 1; i++) {
            if (sb.charAt(i) != cible) continue;

            // CAS A : adjacents directs
            if (sb.charAt(i + 1) == cible) {
                sb.replace(i, i + 2, "" + cible + SHADDA);
                if (i + 2 < sb.length() && estVoyelle(sb.charAt(i + 2)))
                    sb.deleteCharAt(i + 2);
                log.info("   ✅ MOUDAAF A — {} → {}", mot, sb);
                return sb.toString();
            }

            // CAS B : L2 + voyelle + L3
            if (i + 2 < sb.length()) {
                char m = sb.charAt(i + 1);
                if (estVoyelle(m) && sb.charAt(i + 2) == cible) {
                    sb.replace(i, i + 3, "" + cible + m + SHADDA);
                    if (i + 3 < sb.length() && estVoyelle(sb.charAt(i + 3)))
                        sb.deleteCharAt(i + 3);
                    log.info("   ✅ MOUDAAF B — {} → {}", mot, sb);
                    return sb.toString();
                }
            }
        }

        log.debug("   ⚪ MOUDAAF — L2/L3 non adjacents, pas de fusion");
        return mot;
    }

    // ================================================================
    // MITHAL — L1 ∈ {و, ي}
    // ================================================================
    private String transformerMithal(String mot, Root root, String schemeId) {
        String l1 = root.getL1();
        if (!l1.equals("و") && !l1.equals("ي")) return mot;

        char c1 = l1.charAt(0);

        // Cas spécial مِفعال : و → ي
        if (estSchemeMifaal(schemeId) && c1 == WAW) {
            String marque = "" + '\u0645' + '\u0650' + WAW;
            if (mot.contains(marque)) {
                String t = mot.replaceFirst(marque, "" + '\u0645' + '\u0650' + YAA);
                log.info("   ✅ MITHAL مِفعال و→ي: {} → {}", mot, t);
                return t;
            }
        }

        // Schèmes nominaux ET passés → L1 maintenu
        // FIX : وفى + فَعَلَ — sans ce garde-fou, Mithal tente de supprimer و
        if (estSchemeNominal(schemeId) || estSchemePasse(schemeId)) {
            log.debug("   ℹ️ MITHAL — {} maintenu (nominal ou passé)", l1);
            return mot;
        }

        // Présent : يَوْ... → supprimer L1 + sukun
        if (mot.charAt(0) == YAA) {
            for (int i = 1; i < mot.length() - 1; i++) {
                if (mot.charAt(i) == c1 && mot.charAt(i + 1) == SUKUN) {
                    String t = mot.substring(0, i) + mot.substring(i + 2);
                    log.info("   ✅ MITHAL PRÉSENT — {} → {}", mot, t);
                    return t;
                }
            }
        }

        // Impératif اوْ / ايْ → supprimer ا + L1 + sukun
        if (mot.length() >= 3
                && mot.charAt(0) == ALEF
                && mot.charAt(1) == c1
                && mot.charAt(2) == SUKUN) {
            String t = mot.substring(3);
            log.info("   ✅ MITHAL IMPÉRATIF (اوْ) — {} → {}", mot, t);
            return t;
        }

        // Impératif simple وْ / يْ en tête
        if (mot.length() >= 2 && mot.charAt(0) == c1 && mot.charAt(1) == SUKUN) {
            String t = mot.substring(2);
            log.info("   ✅ MITHAL IMPÉRATIF — {} → {}", mot, t);
            return t;
        }

        return mot;
    }

    // ================================================================
    // AJWAF — L2 ∈ {و, ي}
    //
    // CAS 1  : damma+و ou kasra+و ou kasra+ي → voyelle longue maintenue
    // CAS 2  : ا + L2 + kasra → ائِ  (hamza)
    // CAS 3  : fatha + L2 → ا  (allongement passé)
    //          SAUF si L3 est lettre faible (Lafeef مقرون) → protéger L2
    // CAS 4b : مَفْعُول + L2=ي : ْيُو → ِي
    // CAS 4  : sukun + L2 → suppression (مَفعول + L2=و uniquement)
    // ================================================================
    private String transformerAjwaf(String mot, Root root, String schemeId) {
        return transformerAjwafInterne(mot, root, schemeId, false);
    }

    private String transformerAjwafInterne(String mot, Root root,
                                            String schemeId, boolean estMaqroun) {
        String l2 = root.getL2();
        if (!l2.equals("و") && !l2.equals("ي")) return mot;

        char cible = l2.charAt(0);
        int pos = trouverPosition(mot, cible);
        if (pos < 0) return mot;

        char avant = pos > 0             ? mot.charAt(pos - 1) : '\0';
        char apres = pos < mot.length()-1 ? mot.charAt(pos + 1) : '\0';

        StringBuilder sb = new StringBuilder(mot);

        // CAS 1 : voyelle longue maintenue
        // kasra+و couvre يَفْعِلُ (ex: روى → يَرْوِي)
        if ((avant == DAMMA && cible == WAW)  ||
            (avant == KASRA && cible == WAW)  ||
            (avant == KASRA && cible == YAA)) {
            log.debug("   ℹ️ CAS 1 — voyelle longue maintenue");
            return mot;
        }

        // Schème présent → L2 maintenu
        if (estSchemePresent(schemeId)) {
            log.debug("   ℹ️ Schème présent — {} maintenu", l2);
            return mot;
        }

                // CAS 2 : ا + L2 + kasra → ائِ
        // SAUF Lafeef مقرون : و maintenu (رَاوِي → رَاوٍ)
        if (pos >= 1 && mot.charAt(pos - 1) == ALEF && apres == KASRA) {
            if (estMaqroun) {
                log.debug("   ℹ️ CAS 2 — LAFEEF MAQROUN, {} protégé", l2);
                return mot;
            }
            sb.replace(pos - 1, pos + 2, "" + ALEF + HAMZA_YAA + KASRA);
            log.info("   ✅ CAS 2 — ا{}ِ → ائِ: {} → {}", l2, mot, sb);
            return sb.toString();
        }


        // CAS 3 : fatha + L2 → ا
        // FIX Bug3/4 : si L3 est lettre faible (Lafeef مقرون), protéger L2
        // car Naqis doit recevoir L2 intact pour traiter L3 correctement
        if (avant == FATHA) {
            boolean l3Faible = estLettreFaible(root.getL3());
            if (estMaqroun || l3Faible) {
                log.debug("   ℹ️ LAFEEF MAQROUN — L2={} protégé (L3 faible)", l2);
                return mot;
            }
            sb.replace(pos - 1, pos + 1, "" + ALEF);
            log.info("   ✅ CAS 3 — َ{} → ا: {} → {}", l2, mot, sb);
            return sb.toString();
        }

        // CAS 4b : مَفْعُول + L2=ي : ْيُو → ِي
        if (avant == SUKUN && cible == YAA && apres == DAMMA
                && estSchemeNominalAjwaf(schemeId)) {
            int debut = pos - 1;
            int fin = pos + 2;
            if (fin < sb.length() && sb.charAt(fin) == WAW) fin++;
            sb.replace(debut, fin, "" + KASRA + YAA);
            log.info("   ✅ CAS 4b مَفْعُول+ي — ْيُو → ِي: {} → {}", mot, sb);
            return sb.toString();
        }

        // CAS 4 : sukun + L2 → suppression (مَفعول + L2=و uniquement)
        if (avant == SUKUN && estSchemeNominalAjwaf(schemeId)) {
            if (estVoyelle(apres)) {
                sb.replace(pos - 1, pos + 2, "" + apres);
            } else {
                sb.replace(pos - 1, pos + 1, "");
            }
            log.info("   ✅ CAS 4 — ْ{} supprimé: {} → {}", l2, mot, sb);
            return sb.toString();
        }

        return mot;
    }

    // ================================================================
    // NAQIS — L3 ∈ {و, ي} en position finale
    //
    // CAS 0  : يي → يّ  /  وو → وّ  (voyelle longue schème + L3 identique)
    // CAS 3  : L3 + sukun → ٍ
    // CAS 3b : فاعِل, L3 final → ٍ   (priorité sur CAS 1b)
    // CAS 1  : fatha + L3 → ى (ي) ou ا (و)
    // CAS 1b : ي final sans voyelle → ى  (seulement si pas فاعِل)
    // CAS 2  : kasra + و → ي
    //
    // FIX Bug1 : supprimerDammaFinale() après chaque transformation
    // FIX Bug2 : CAS 3b testé AVANT CAS 1b
    // ================================================================
    private String transformerNaqis(String mot, Root root, String schemeId) {
        String l3 = root.getL3();
        if (!l3.equals("و") && !l3.equals("ي")) return mot;

        char cible = l3.charAt(0);

        // CAS 0 : يي → يّ  ou  وو → وّ
        if (cible == YAA && mot.endsWith("" + YAA + YAA)) {
            String t = supprimerDammaFinale(
                mot.substring(0, mot.length() - 2) + YAA + SHADDA);
            log.info("   ✅ CAS 0 — يي → يّ: {} → {}", mot, t);
            return t;
        }
        if (cible == WAW && mot.endsWith("" + WAW + WAW)) {
            String t = supprimerDammaFinale(
                mot.substring(0, mot.length() - 2) + WAW + SHADDA);
            log.info("   ✅ CAS 0 — وو → وّ: {} → {}", mot, t);
            return t;
        }

        // Garde-fou : L3 fait partie d'une voyelle longue FIXE du schème
        if (estSchemeAvecVoyelleLongue(schemeId, l3)) {
            log.debug("   ℹ️ NAQIS — L3 voyelle longue du schème, maintenu");
            return mot;
        }

        // Chercher L3 par la DERNIÈRE occurrence (évite confusion avec L2 en Lafeef)
        int posL3 = trouverDernierePosition(mot, cible);
        if (posL3 < 0) return mot;

        char avantL3 = posL3 > 0             ? mot.charAt(posL3 - 1) : '\0';
        char apresL3 = posL3 < mot.length()-1 ? mot.charAt(posL3 + 1) : '\0';

        // CAS 3 : L3 + sukun → ٍ
        if (apresL3 == SUKUN) {
            String t = supprimerDammaFinale(mot.substring(0, posL3) + '\u064D');
            log.info("   ✅ CAS 3 — {}ْ → ٍ: {} → {}", l3, mot, t);
            return t;
        }

        // CAS 3b : فاعِل → L3 final faible → ٍ
    if (estSchemeFaail(schemeId) && estEnPositionFinale(mot, posL3)) {
        log.debug("   🔍 CAS 3b check — estSchemeFaail={}, estEnPositionFinale={}, avantL3='{}'({})",
            estSchemeFaail(schemeId),
            estEnPositionFinale(mot, posL3),
            avantL3, (int) avantL3);
        int debut = posL3;
        // Si la lettre juste avant L3 est une kasra, on la retire
        // car kasratân ٍ inclut déjà la voyelle kasra
        if (debut > 0 && mot.charAt(debut - 1) == KASRA) debut--;
        String t = supprimerDammaFinale(mot.substring(0, debut) + '\u064D');
        log.info("   ✅ CAS 3b — {} final (فاعِل) → ٍ: {} → {}", l3, mot, t);
        return t;
    }

        // CAS 1 : fatha + L3 → ى/ي ou ا
        // FIX : présent → ي (voyelle longue), passé/nominal → ى
        if (avantL3 == FATHA) {
            String suf;
            if (cible == WAW) {
                suf = "" + ALEF; // و → ا toujours
            } else {
                suf = estSchemePresent(schemeId) ? "" + YAA : "\u0649";
            }
            String t = supprimerDammaFinale(
                supprimerVoyelleFinale(mot.substring(0, posL3 - 1) + suf));
            log.info("   ✅ CAS 1 — fatha+{} → {}: {} → {}", l3, suf, mot, t);
            return t;
        }

        // CAS 1b : ي final sans voyelle → ى (passé/nominal) ou ي (présent)
        // FIX : يَرْوِي doit garder ي et non ى
        if (cible == YAA && posL3 == mot.length() - 1 && avantL3 != KASRA) {
            char finaleChar = estSchemePresent(schemeId) ? YAA : '\u0649';
            String t = supprimerDammaFinale(
                supprimerVoyelleFinale(mot.substring(0, posL3) + finaleChar));
            log.info("   ✅ CAS 1b — ي → {}: {} → {}",
                     (finaleChar == YAA ? "ي" : "ى"), mot, t);
            return t;
        }

        // CAS 2 : kasra + و → ي
        if (cible == WAW && avantL3 == KASRA) {
            String t = supprimerDammaFinale(mot.substring(0, posL3) + YAA);
            log.info("   ✅ CAS 2 — ِو → ِي: {} → {}", mot, t);
            return t;
        }

        return mot;
    }

    /** Supprime une voyelle courte parasite en fin de chaîne. */
    private String supprimerVoyelleFinale(String s) {
        if (s.isEmpty()) return s;
        char dernier = s.charAt(s.length() - 1);
        if (estVoyelle(dernier)) return s.substring(0, s.length() - 1);
        return s;
    }

    /**
     * Supprime la damma finale résiduelle du schème après transformation Naqis.
     * FIX Bug1 : يَرْوِيُ → يَرْوِي
     */
    private String supprimerDammaFinale(String s) {
        if (s == null || s.isEmpty()) return s;
        if (s.charAt(s.length() - 1) == DAMMA)
            return s.substring(0, s.length() - 1);
        return s;
    }

    // ================================================================
    // LAFEEF — Séquentiel L1 → L2 → L3
    //
    // Pour Lafeef مقرون (L2=و ET L3=ي) :
    //   → L2 est protégé si fatha+و car L3 faible suit (transformerAjwafInterne)
    //   → L3 est traité par transformerNaqis (trouverDernierePosition)
    //
    // روى + فاعِل :
    //   1. Ajwaf CAS 2 : اوِ → ائِ  → رَائِيٌ
    //   2. Naqis CAS 3b (فاعِل) : ي final → ٍ  → رَاوٍ ✅
    // ================================================================
    private String transformerLafeef(String mot, Root root, String schemeId) {
        log.debug("🔍 LAFEEF — L1={}, L2={}, L3={}",
            root.getL1(), root.getL2(), root.getL3());

        // Détecter Lafeef مقرون : L2 ET L3 sont des lettres faibles
        boolean estMaqroun = estLettreFaible(root.getL2())
                          && estLettreFaible(root.getL3());

        String res = mot;

        if (estLettreFaible(root.getL1()))
            res = transformerMithal(res, root, schemeId);

        if (estLettreFaible(root.getL2()))
            res = transformerAjwafInterne(res, root, schemeId, estMaqroun);

        if (estLettreFaible(root.getL3()))
            res = transformerNaqis(res, root, schemeId);

        log.info("   ✅ LAFEEF — {} → {}", mot, res);
        return res;
    }

    // ================================================================
    // Détection des types de schèmes
    // ================================================================

    // ================================================================
    // Détection des schèmes — comparaison SANS diacritiques
    // pour éviter les bugs d'encodage Unicode (ordre composition)
    // ================================================================

    /** Supprime tous les diacritiques arabes U+064B–U+065F */
    private String supprimerDiacritiques(String s) {
        if (s == null) return "";
        return s.replaceAll("[\\u064B-\\u065F]", "");
    }

    private boolean estSchemeNominal(String s) {
        if (s == null) return false;
        String d = supprimerDiacritiques(s);
        return d.contains("فاعل")  || d.contains("فاعلة") ||
               d.contains("مفعل")  || d.contains("مفعال") ||
               d.contains("مفعول") || d.contains("مفاعل") ||
               d.contains("مفعّل") ||
               s.toUpperCase().contains("FAIL")  ||
               s.toUpperCase().contains("MAFAL") ||
               s.toUpperCase().contains("MAFOUL");
    }

    /** Restreint CAS 4 AJWAF au seul schème مَفعول */
    private boolean estSchemeNominalAjwaf(String s) {
        if (s == null) return false;
        String d = supprimerDiacritiques(s);
        return d.contains("مفعول") ||
               s.toUpperCase().contains("MAFOUL");
    }

    private boolean estSchemePresent(String s) {
        if (s == null) return false;
        String d = supprimerDiacritiques(s);
        return d.startsWith("يفع")  || d.startsWith("يفاع") ||
               d.startsWith("يتفع") || d.startsWith("ينفع") ||
               d.startsWith("يفتع") || d.startsWith("يستف") ||
               d.startsWith("يفعّ") ||
               s.toUpperCase().contains("PRESENT");
    }

    private boolean estSchemePasse(String s) {
        if (s == null) return false;
        String d = supprimerDiacritiques(s);
        return d.startsWith("فعل")   || // فَعَلَ / فَعِلَ / فَعُلَ
               d.startsWith("فعّل")  || // فَعَّلَ
               d.startsWith("فاعل")  || // فاعَلَ
               d.startsWith("أفعل")  || // أفْعَلَ
               d.startsWith("تفعّل") || // تَفَعَّلَ
               d.startsWith("تفاعل") || // تَفاعَلَ
               d.startsWith("انفعل") || // انْفَعَلَ
               d.startsWith("افتعل") || // افْتَعَلَ
               d.startsWith("استفعل")||  // اسْتَفْعَلَ
               s.toUpperCase().contains("PASSE") ||
               s.toUpperCase().contains("MADI");
    }

    private boolean estSchemeFaail(String s) {
        if (s == null) return false;
        String d = supprimerDiacritiques(s);
        return d.contains("فاعل") || d.contains("فاعلة") ||
               s.toUpperCase().contains("FAIL");
    }

    private boolean estSchemeMifaal(String s) {
        if (s == null) return false;
        String d = supprimerDiacritiques(s);
        return d.contains("مفعال") ||
               s.toUpperCase().contains("MIFAAL");
    }

    /**
     * Protège ي/و quand ils font partie d'une voyelle longue FIXE du schème.
     * فعيل est EXCLU : traité par CAS 0 (يي → يّ) dans transformerNaqis.
     */
    private boolean estSchemeAvecVoyelleLongue(String s, String l3) {
        if (s == null) return false;
        String d = supprimerDiacritiques(s);
        if (l3.equals("ي"))
            return d.contains("تفعيل");
        if (l3.equals("و"))
            return d.contains("فعول") || d.contains("مفعول");
        return false;
    }

    // ================================================================
    // Utilitaires
    // ================================================================

    /**
     * Trouve la PREMIÈRE occurrence de cible qui est une consonne (pas un diacritique).
     * Utilisé pour L2 (Ajwaf).
     */
    private int trouverPosition(String mot, char cible) {
        for (int i = 0; i < mot.length(); i++)
            if (mot.charAt(i) == cible && !estDiacritique(mot.charAt(i)))
                return i;
        return -1;
    }

    /**
     * Trouve la DERNIÈRE occurrence de cible qui est une consonne.
     * Utilisé pour L3 (Naqis / Lafeef) afin d'éviter de confondre L2 et L3.
     */
    private int trouverDernierePosition(String mot, char cible) {
        for (int i = mot.length() - 1; i >= 0; i--)
            if (mot.charAt(i) == cible && !estDiacritique(mot.charAt(i)))
                return i;
        return -1;
    }

    private boolean estVoyelle(char c) {
        return c == FATHA || c == KASRA || c == DAMMA;
    }

    private boolean estDiacritique(char c) {
        return c >= '\u064B' && c <= '\u065F';
    }

    private boolean estLettreFaible(String l) {
        return l.equals("و") || l.equals("ي");
    }
    private boolean estEnPositionFinale(String mot, int pos) {
        for (int i = pos + 1; i < mot.length(); i++)
            if (!estDiacritique(mot.charAt(i))) return false;
        return true;
    }

}