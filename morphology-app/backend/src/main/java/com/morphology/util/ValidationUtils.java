package com.morphology.util;

/**
 * Utilitaires de validation pour les données arabes
 */
public class ValidationUtils {

    /**
     * Vérifier si une racine est valide
     * Une racine valide contient exactement 3 caractères arabes
     */
    public static boolean estRacineValide(String racine) {
        if (racine == null || racine.isEmpty()) {
            return false;
        }
        
        racine = racine.trim();
        
        if (racine.length() != 3) {
            return false;
        }
        
        // Vérifier que tous les caractères sont arabes
        return racine.matches("[\\u0600-\\u06FF]{3}");
    }

    /**
     * Vérifier si une règle de schème est valide
     * Une règle valide doit contenir les positions 1, 2 et 3
     */
    public static boolean estRegleValide(String regle) {
        if (regle == null || regle.isEmpty()) {
            return false;
        }
        
        return regle.contains("1") && regle.contains("2") && regle.contains("3");
    }

    /**
     * Vérifier si une chaîne n'est pas vide
     */
    public static boolean estNonVide(String texte) {
        return texte != null && !texte.trim().isEmpty();
    }

    /**
     * Vérifier si un texte contient uniquement des caractères arabes
     */
    public static boolean estTexteArabe(String texte) {
        if (texte == null || texte.isEmpty()) {
            return false;
        }
        
        // Autoriser les espaces et les caractères arabes
        return texte.matches("[\\u0600-\\u06FF\\s]+");
    }

    /**
     * Vérifier si une chaîne est un nombre entier
     */
    public static boolean estNombreEntier(String texte) {
        if (texte == null || texte.isEmpty()) {
            return false;
        }
        
        try {
            Integer.parseInt(texte.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Vérifier si un nombre est dans une plage
     */
    public static boolean estDansPlage(int nombre, int min, int max) {
        return nombre >= min && nombre <= max;
    }

    /**
     * Nettoyer une chaîne (trim)
     */
    public static String nettoyer(String texte) {
        return texte == null ? "" : texte.trim();
    }

    /**
     * Vérifier si un nom de schème est valide
     */
    public static boolean estNomSchemeValide(String nom) {
        if (!estNonVide(nom)) {
            return false;
        }
        
        // Maximum 50 caractères
        return nom.length() <= 50;
    }

    /**
     * Vérifier si un choix de menu est valide
     */
    public static boolean estChoixMenuValide(String choix, int max) {
        if (!estNombreEntier(choix)) {
            return false;
        }
        
        int valeur = Integer.parseInt(choix.trim());
        return estDansPlage(valeur, 1, max);
    }

    /**
     * Vérifier si un chemin de fichier est valide
     */
    public static boolean estCheminValide(String chemin) {
        if (!estNonVide(chemin)) {
            return false;
        }
        
        // Caractères interdits dans les chemins Windows
        String caracteresInterdits = "<>:\"|?*";
        for (char c : caracteresInterdits.toCharArray()) {
            if (chemin.contains(String.valueOf(c))) {
                return false;
            }
        }
        
        return true;
    }

    /**
     * Extraire les consonnes (enlever les diacritiques)
     */
    public static String extraireConsonnes(String texte) {
        if (texte == null || texte.isEmpty()) {
            return "";
        }
        
        // Enlever les diacritiques arabes (U+064B à U+065F)
        return texte.replaceAll("[\\u064B-\\u065F]", "");
    }

    /**
     * Vérifier le format d'une ligne (nombre de champs séparés)
     */
    public static boolean estFormatLigneValide(String ligne, String separateur, int nombreChamps) {
        if (!estNonVide(ligne)) {
            return false;
        }
        
        String[] parties = ligne.split(separateur);
        return parties.length == nombreChamps;
    }

    /**
     * Créer un message d'erreur formaté
     */
    public static String messageErreur(String champ, String valeur, String raison) {
        return String.format("⚠ Erreur de validation pour '%s': valeur '%s' - %s", 
                             champ, valeur, raison);
    }

    /**
     * Vérifier si un texte contient des diacritiques
     */
    public static boolean contientDiacritiques(String texte) {
        if (texte == null || texte.isEmpty()) {
            return false;
        }
        
        return texte.matches(".*[\\u064B-\\u065F].*");
    }

    /**
     * Valider une adresse email
     */
    public static boolean estEmailValide(String email) {
        if (!estNonVide(email)) {
            return false;
        }
        
        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(regex);
    }

    /**
     * Valider un numéro de téléphone
     */
    public static boolean estTelephoneValide(String telephone) {
        if (!estNonVide(telephone)) {
            return false;
        }
        
        // Format: +XXX XXXXXXXXX ou XXXXXXXXXX
        String regex = "^(\\+\\d{1,3}\\s?)?\\d{8,15}$";
        return telephone.matches(regex);
    }
}
