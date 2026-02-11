package com.morphology.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Noeud de l'arbre AVL représentant une racine arabe
 */
public class NoeudAVL {
    private String racine;
    private int hauteur;
    private List<MotDerive> listeDerives;
    private int frequenceRacine;

    public NoeudAVL(String racine) {
        this.racine = racine;
        this.hauteur = 1;
        this.listeDerives = new ArrayList<>();
        this.frequenceRacine = 0;
    }

    // Getters et Setters
    public String getRacine() {
        return racine;
    }

    public void setRacine(String racine) {
        this.racine = racine;
    }

    public int getHauteur() {
        return hauteur;
    }

    public void setHauteur(int hauteur) {
        this.hauteur = hauteur;
    }

    public List<MotDerive> getListeDerives() {
        return listeDerives;
    }

    public void setListeDerives(List<MotDerive> listeDerives) {
        this.listeDerives = listeDerives;
    }

    public int getFrequenceRacine() {
        return frequenceRacine;
    }

    public void setFrequenceRacine(int frequenceRacine) {
        this.frequenceRacine = frequenceRacine;
    }

    /**
     * Incrémenter la fréquence de la racine
     */
    public void incrementerFrequenceRacine() {
        this.frequenceRacine++;
    }

    /**
     * Ajouter un dérivé à la racine
     */
    public void ajouterDerive(String mot) {
        // Vérifier si le dérivé existe déjà
        for (MotDerive derive : listeDerives) {
            if (derive.getMot().equals(mot)) {
                derive.incrementerFrequence();
                return;
            }
        }
        // Ajouter un nouveau dérivé
        listeDerives.add(new MotDerive(mot));
    }

    /**
     * Rechercher un dérivé
     */
    public MotDerive rechercherDerive(String mot) {
        for (MotDerive derive : listeDerives) {
            if (derive.getMot().equals(mot)) {
                return derive;
            }
        }
        return null;
    }

    /**
     * Vérifier si un dérivé existe
     */
    public boolean contientDerive(String mot) {
        return rechercherDerive(mot) != null;
    }

    /**
     * Obtenir le nombre de dérivés
     */
    public int getNombreDerives() {
        return listeDerives.size();
    }

    /**
     * Obtenir la fréquence totale de tous les dérivés
     */
    public int getFrequenceTotaleDerives() {
        int total = 0;
        for (MotDerive derive : listeDerives) {
            total += derive.getFrequence();
        }
        return total;
    }

    /**
     * Vérifier si la racine est valide (3 caractères arabes)
     */
    public boolean estRacineValide() {
        if (racine == null || racine.length() != 3) {
            return false;
        }
        return racine.matches("[\\u0600-\\u06FF]{3}");
    }

    /**
     * Obtenir le dérivé le plus fréquent
     */
    public MotDerive getDeriveLesPlusFrequent() {
        if (listeDerives.isEmpty()) {
            return null;
        }

        MotDerive plusFrequent = listeDerives.get(0);
        for (MotDerive derive : listeDerives) {
            if (derive.getFrequence() > plusFrequent.getFrequence()) {
                plusFrequent = derive;
            }
        }
        return plusFrequent;
    }

    @Override
    public String toString() {
        return "Racine: " + racine + 
               " | Fréquence: " + frequenceRacine + 
               " | Dérivés: " + listeDerives.size() + 
               " | Hauteur: " + hauteur;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        NoeudAVL other = (NoeudAVL) obj;
        return racine != null && racine.equals(other.racine);
    }

    @Override
    public int hashCode() {
        return racine != null ? racine.hashCode() : 0;
    }
}