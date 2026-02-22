package com.morphology.model;

import java.util.ArrayList;
import java.util.List;

public class NoeudAVL {
    private String racine;
    private int hauteur;
    private List<MotDerive> listeDerives;
    private int frequenceRacine;
    private RootType typeMorphologique;

    // FIX : champ manquant requis par RootService
    private boolean contientHamza;

    public NoeudAVL(String racine) {
        this.racine = racine;
        this.hauteur = 1;
        this.listeDerives = new ArrayList<>();
        this.frequenceRacine = 0;
        this.typeMorphologique = null;
        this.contientHamza = false;
    }

    // --- Type morphologique ---
    public RootType getTypeMorphologique()           { return typeMorphologique; }
    public void setTypeMorphologique(RootType type)  { this.typeMorphologique = type; }

    // --- FIX : getter/setter contientHamza ---
    public boolean isContientHamza()                 { return contientHamza; }
    public void setContientHamza(boolean v)          { this.contientHamza = v; }

    // --- Champs de base ---
    public String getRacine()                        { return racine; }
    public void setRacine(String racine)             { this.racine = racine; }
    public int getHauteur()                          { return hauteur; }
    public void setHauteur(int hauteur)              { this.hauteur = hauteur; }
    public List<MotDerive> getListeDerives()         { return listeDerives; }
    public void setListeDerives(List<MotDerive> l)   { this.listeDerives = l; }
    public int getFrequenceRacine()                  { return frequenceRacine; }
    public void setFrequenceRacine(int f)            { this.frequenceRacine = f; }
    public void incrementerFrequenceRacine()         { this.frequenceRacine++; }

    // --- Gestion des dérivés ---
    public void ajouterDerive(String mot) {
        for (MotDerive d : listeDerives) {
            if (d.getMot().equals(mot)) { d.incrementerFrequence(); return; }
        }
        listeDerives.add(new MotDerive(mot));
    }

    public MotDerive rechercherDerive(String mot) {
        for (MotDerive d : listeDerives)
            if (d.getMot().equals(mot)) return d;
        return null;
    }

    public boolean contientDerive(String mot)  { return rechercherDerive(mot) != null; }
    public int getNombreDerives()              { return listeDerives.size(); }

    public int getFrequenceTotaleDerives() {
        int total = 0;
        for (MotDerive d : listeDerives) total += d.getFrequence();
        return total;
    }

    public MotDerive getDeriveLesPlusFrequent() {
        if (listeDerives.isEmpty()) return null;
        MotDerive max = listeDerives.get(0);
        for (MotDerive d : listeDerives)
            if (d.getFrequence() > max.getFrequence()) max = d;
        return max;
    }

    public boolean estRacineValide() {
        if (racine == null || racine.length() != 3) return false;
        return racine.matches("[\\u0621-\\u064A]{3}"); // consonnes seulement
    }

    @Override
    public String toString() {
        return "Racine: " + racine
            + " | Type: " + (typeMorphologique != null ? typeMorphologique.getNomArabe() : "?")
            + " | Hamza: " + contientHamza
            + " | Dérivés: " + listeDerives.size()
            + " | Fréquence: " + frequenceRacine
            + " | Hauteur: " + hauteur;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        return racine != null && racine.equals(((NoeudAVL) obj).racine);
    }

    @Override
    public int hashCode() { return racine != null ? racine.hashCode() : 0; }
}