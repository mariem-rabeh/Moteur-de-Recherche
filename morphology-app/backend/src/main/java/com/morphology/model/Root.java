package com.morphology.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Root {

    private String racine;
    private RootType type;
    private String[] lettres;
    private boolean isValid;
    private String errorMessage;
    private boolean contientHamza = false;

    // Plage consonnes arabes : ا (0x0621) → ي (0x064A)
    private static final char CONSONNE_DEBUT = '\u0621';
    private static final char CONSONNE_FIN   = '\u064A';

    // Harakat (diacritiques) : à exclure des consonnes
    private static final char HARAKA_DEBUT   = '\u064B';
    private static final char HARAKA_FIN     = '\u065F';

    private static final String[] HAMZA_VARIANTS = {"أ","إ","آ","ء","ؤ","ئ"};

    // ================================================================
    // Constructeur
    // ================================================================
    public Root(String racine) {
        this.racine  = racine != null ? racine.trim().replace('\u0649', '\u064A') : "";
        this.lettres = extraireLettres(this.racine);
        this.isValid = validerRacine();
    }

    // ================================================================
    // Extraction — ne garde que les consonnes (hors harakat)
    // ================================================================
    private String[] extraireLettres(String racine) {
        if (racine == null || racine.isEmpty()) {
            this.errorMessage = "La racine ne peut pas être vide.";
            return new String[0];
        }

        StringBuilder consonnes = new StringBuilder();
        for (char c : racine.toCharArray()) {
            // Consonne arabe pure (hors diacritiques)
            if (c >= CONSONNE_DEBUT && c <= CONSONNE_FIN
                    && !(c >= HARAKA_DEBUT && c <= HARAKA_FIN)) {
                consonnes.append(c);
            }
        }

        // Rejeter l'alef simple en position consonantique
        for (int i = 0; i < consonnes.length(); i++) {
            if (consonnes.charAt(i) == '\u0627') { // ا
                this.errorMessage =
                    "La racine contient un alef simple (ا) en position consonantique. "
                  + "Une hamza (أ / إ) est peut-être attendue.";
                return new String[0];
            }
        }

        String clean = consonnes.toString();
        if (clean.length() != 3) {
            this.errorMessage = String.format(
                "La racine doit contenir exactement 3 consonnes — %d trouvée(s) : « %s ».",
                clean.length(), clean);
            return new String[0];
        }

        return new String[]{
            String.valueOf(clean.charAt(0)),
            String.valueOf(clean.charAt(1)),
            String.valueOf(clean.charAt(2))
        };
    }

    // ================================================================
    // Validation
    // ================================================================
    private boolean validerRacine() {
        if (lettres == null || lettres.length != 3) return false;

        for (String lettre : lettres) {
            if (lettre == null || lettre.isEmpty()) {
                errorMessage = "Lettre vide détectée.";
                return false;
            }
            char c = lettre.charAt(0);
            if (c < CONSONNE_DEBUT || c > CONSONNE_FIN) {
                errorMessage = String.format(
                    "Caractère non consonantique : « %s » (U+%04X).", lettre, (int) c);
                return false;
            }
            if (estHamza(lettre)) this.contientHamza = true;
        }
        return true;
    }

    private boolean estHamza(String l) {
        for (String h : HAMZA_VARIANTS) if (h.equals(l)) return true;
        return false;
    }

    public String getL1() { return lettres != null && lettres.length > 0 ? lettres[0] : ""; }
    public String getL2() { return lettres != null && lettres.length > 1 ? lettres[1] : ""; }
    public String getL3() { return lettres != null && lettres.length > 2 ? lettres[2] : ""; }

    @Override
    public String toString() {
        if (!isValid) return "Root[INVALIDE: " + errorMessage + "]";
        return String.format("Root[%s | %s-%s-%s | %s | hamza=%b]",
            racine, getL1(), getL2(), getL3(),
            type != null ? type.getNomArabe() : "?", contientHamza);
    }
}