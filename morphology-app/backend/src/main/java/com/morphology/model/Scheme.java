package com.morphology.model;

/**
 * Représente un schème morphologique arabe
 * Un schème définit un motif de transformation d'une racine
 */
public class Scheme {
    private String nom;
    private String regle;

    public Scheme(String nom, String regle) {
        this.nom = nom;
        this.regle = regle;
    }

    // Getters
    public String getNom() {
        return nom;
    }

    public String getRegle() {
        return regle;
    }

    /**
     * Appliquer le schème à une racine trilitère
     * 
     * @param racine La racine de 3 lettres
     * @return Le mot généré
     */
    public String appliquer(String racine) {
        if (racine == null || racine.length() != 3) {
            return "Erreur : La racine doit contenir exactement 3 lettres.";
        }

        StringBuilder resultat = new StringBuilder();
        
        char lettre1 = racine.charAt(0);
        char lettre2 = racine.charAt(1);
        char lettre3 = racine.charAt(2);

        // Parcourir la règle et remplacer 1, 2, 3 par les lettres de la racine
        for (int i = 0; i < regle.length(); i++) {
            char c = regle.charAt(i);
            switch (c) {
                case '1':
                    resultat.append(lettre1);
                    break;
                case '2':
                    resultat.append(lettre2);
                    break;
                case '3':
                    resultat.append(lettre3);
                    break;
                default:
                    resultat.append(c);
                    break;
            }
        }

        return resultat.toString();
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