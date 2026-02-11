package com.morphology.model;

/**
 * Représente un mot dérivé d'une racine avec sa fréquence
 */
public class MotDerive {
    private String mot;
    private int frequence;

    public MotDerive(String mot) {
        this.mot = mot;
        this.frequence = 1;
    }

    public MotDerive(String mot, int frequence) {
        this.mot = mot;
        this.frequence = frequence;
    }

    // Getters et Setters
    public String getMot() {
        return mot;
    }

    public void setMot(String mot) {
        this.mot = mot;
    }

    public int getFrequence() {
        return frequence;
    }

    public void setFrequence(int frequence) {
        this.frequence = frequence;
    }

    /**
     * Incrémenter la fréquence
     */
    public void incrementerFrequence() {
        this.frequence++;
    }

    @Override
    public String toString() {
        return mot + " (fréquence: " + frequence + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        MotDerive other = (MotDerive) obj;
        return mot != null && mot.equals(other.mot);
    }

    @Override
    public int hashCode() {
        return mot != null ? mot.hashCode() : 0;
    }
}