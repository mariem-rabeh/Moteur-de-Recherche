package com.morphology.model;

/**
 * Table de hachage pour stocker les schèmes morphologiques
 * Utilise le chaînage pour gérer les collisions
 */
public class TableHachage {
    private static final int TAILLE = 128;
    
    private Maillon[] table;
    private int nombreElements;

    /**
     * Classe interne représentant un maillon de la chaîne
     */
    private static class Maillon {
        String cle;
        Scheme valeur;
        Maillon suivant;

        Maillon(String cle, Scheme valeur) {
            this.cle = cle;
            this.valeur = valeur;
            this.suivant = null;
        }
    }

    /**
     * Constructeur
     */
    public TableHachage() {
        this.table = new Maillon[TAILLE];
        this.nombreElements = 0;
    }

    /**
     * Fonction de hachage DJB2
     * Algorithme de hachage efficace pour les chaînes
     */
    private int djb2(String cle) {
        long hash = 5381;
        
        for (int i = 0; i < cle.length(); i++) {
            hash = ((hash << 5) + hash) + cle.charAt(i);
        }
        
        return (int) (Math.abs(hash) % TAILLE);
    }

    /**
     * Insérer ou mettre à jour un schème
     */
    public void inserer(String cle, Scheme valeur) {
        int index = djb2(cle);
        
        // Vérifier si la clé existe déjà (mise à jour)
        Maillon current = table[index];
        while (current != null) {
            if (current.cle.equals(cle)) {
                current.valeur = valeur;
                return;
            }
            current = current.suivant;
        }
        
        // Insertion d'un nouveau maillon au début de la chaîne
        Maillon nouveau = new Maillon(cle, valeur);
        nouveau.suivant = table[index];
        table[index] = nouveau;
        nombreElements++;
    }

    /**
     * Rechercher un schème par son nom
     */
    public Scheme rechercher(String cle) {
        int index = djb2(cle);
        
        Maillon current = table[index];
        while (current != null) {
            if (current.cle.equals(cle)) {
                return current.valeur;
            }
            current = current.suivant;
        }
        
        return null;
    }

    /**
     * Supprimer un schème
     */
    public boolean supprimer(String cle) {
        int index = djb2(cle);
        
        Maillon current = table[index];
        Maillon previous = null;
        
        while (current != null) {
            if (current.cle.equals(cle)) {
                if (previous == null) {
                    // Supprimer le premier élément
                    table[index] = current.suivant;
                } else {
                    // Supprimer un élément au milieu ou à la fin
                    previous.suivant = current.suivant;
                }
                nombreElements--;
                return true;
            }
            previous = current;
            current = current.suivant;
        }
        
        return false;
    }

    /**
     * Obtenir le nombre d'éléments
     */
    public int getNombreElements() {
        return nombreElements;
    }

    /**
     * Vérifier si la table est vide
     */
    public boolean estVide() {
        return nombreElements == 0;
    }

    /**
     * Obtenir le taux de remplissage
     */
    public double getTauxRemplissage() {
        return (double) nombreElements / TAILLE;
    }

    /**
     * Obtenir le nombre de collisions
     */
    public int getNombreCollisions() {
        int collisions = 0;
        for (int i = 0; i < TAILLE; i++) {
            if (table[i] != null && table[i].suivant != null) {
                Maillon current = table[i].suivant;
                while (current != null) {
                    collisions++;
                    current = current.suivant;
                }
            }
        }
        return collisions;
    }

    /**
     * Obtenir la longueur maximale d'une chaîne
     */
    public int getLongueurMaxChaine() {
        int maxLength = 0;
        for (int i = 0; i < TAILLE; i++) {
            int length = 0;
            Maillon current = table[i];
            while (current != null) {
                length++;
                current = current.suivant;
            }
            if (length > maxLength) {
                maxLength = length;
            }
        }
        return maxLength;
    }

    /**
     * Vider la table
     */
    public void vider() {
        for (int i = 0; i < TAILLE; i++) {
            table[i] = null;
        }
        nombreElements = 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("TableHachage[");
        sb.append("taille=").append(TAILLE);
        sb.append(", éléments=").append(nombreElements);
        sb.append(", taux=").append(String.format("%.2f%%", getTauxRemplissage() * 100));
        sb.append(", collisions=").append(getNombreCollisions());
        sb.append("]");
        return sb.toString();
    }
}