package com.morphology.model;

public class Scheme {
    private String nom;
    private String regle;
    private String id;

    // FIX : stocker le dernier message d'erreur
    private String lastError;

    public Scheme(String nom, String regle) {
        this.nom = nom;
        this.regle = regle;
        this.id = nom; // id = nom par défaut
    }

    public Scheme(String id, String nom, String regle) {
        this.id = id;
        this.nom = nom;
        this.regle = regle;
    }

    public String getNom()      { return nom; }
    public String getRegle()    { return regle; }
    public String getId()       { return id; }

    // FIX : getter requis par GenerationService
    public String getLastError() { return lastError; }

    /**
     * Applique le schème à une racine trilitère.
     * FIX : retourne null en cas d'échec (plus de String "Erreur : ...")
     * Le message d'erreur est accessible via getLastError().
     */
    public String appliquer(String racine) {
        lastError = null; // réinitialiser à chaque appel

        // FIX : vérifier null ET longueur consonantique réelle
        if (racine == null || racine.isBlank()) {
            lastError = "La racine ne peut pas être vide.";
            return null;
        }

        // Extraire les consonnes uniquement (ignorer les harakat si présents)
        String consonnes = extraireConsonnes(racine);
        if (consonnes.length() != 3) {
            lastError = String.format(
                "La racine doit contenir exactement 3 consonnes — %d trouvée(s) : « %s ».",
                consonnes.length(), consonnes);
            return null;
        }

        if (regle == null || regle.isBlank()) {
            lastError = "La règle du schème '" + nom + "' est vide.";
            return null;
        }

        char l1 = consonnes.charAt(0);
        char l2 = consonnes.charAt(1);
        char l3 = consonnes.charAt(2);

        StringBuilder resultat = new StringBuilder();
        for (int i = 0; i < regle.length(); i++) {
            char c = regle.charAt(i);
            switch (c) {
                case '1': resultat.append(l1); break;
                case '2': resultat.append(l2); break;
                case '3': resultat.append(l3); break;
                default:  resultat.append(c);  break;
            }
        }

        String mot = resultat.toString();

        // Vérification finale : résultat ne doit pas être vide
        if (mot.isBlank()) {
            lastError = "La règle '" + regle + "' a produit un résultat vide.";
            return null;
        }

        return mot;
    }

    /**
     * Extrait uniquement les consonnes arabes (hors diacritiques).
     */
    private String extraireConsonnes(String texte) {
        StringBuilder sb = new StringBuilder();
        for (char c : texte.toCharArray()) {
            // Consonnes arabes : 0x0621–0x064A, hors diacritiques 0x064B–0x065F
            if (c >= '\u0621' && c <= '\u064A' && !(c >= '\u064B' && c <= '\u065F')) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "Schème: " + nom + " | Structure: " + regle;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Scheme other = (Scheme) obj;
        return nom != null && nom.equals(other.nom);
    }

    @Override
    public int hashCode() {
        return nom != null ? nom.hashCode() : 0;
    }
}