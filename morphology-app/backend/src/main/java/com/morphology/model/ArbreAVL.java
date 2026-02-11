package com.morphology.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Arbre AVL pour stocker les racines arabes de manière équilibrée
 */
public class ArbreAVL {
    private NoeudAVL noeud;
    private ArbreAVL gauche;
    private ArbreAVL droit;

    public ArbreAVL() {
        this.noeud = null;
        this.gauche = null;
        this.droit = null;
    }

    public ArbreAVL(NoeudAVL noeud) {
        this.noeud = noeud;
        this.gauche = new ArbreAVL();
        this.droit = new ArbreAVL();
    }

    // Getters et Setters
    public NoeudAVL getNoeud() {
        return noeud;
    }

    public void setNoeud(NoeudAVL noeud) {
        this.noeud = noeud;
    }

    public ArbreAVL getGauche() {
        return gauche;
    }

    public void setGauche(ArbreAVL gauche) {
        this.gauche = gauche;
    }

    public ArbreAVL getDroit() {
        return droit;
    }

    public void setDroit(ArbreAVL droit) {
        this.droit = droit;
    }

    /**
     * Vérifier si l'arbre est vide
     */
    public boolean estVide() {
        return noeud == null;
    }

    /**
     * Vérifier si c'est une feuille
     */
    public boolean estFeuille() {
        return !estVide() && 
               (gauche == null || gauche.estVide()) && 
               (droit == null || droit.estVide());
    }

    /**
     * Obtenir la hauteur de l'arbre
     */
    private int hauteur() {
        if (estVide()) {
            return 0;
        }
        return noeud.getHauteur();
    }

    /**
     * Calculer le facteur d'équilibre
     */
    private int getBalance() {
        if (estVide()) {
            return 0;
        }
        
        int hauteurGauche = (gauche != null) ? gauche.hauteur() : 0;
        int hauteurDroit = (droit != null) ? droit.hauteur() : 0;
        
        return hauteurGauche - hauteurDroit;
    }

    /**
     * Mettre à jour la hauteur du noeud
     */
    private void updateHauteur() {
        if (!estVide()) {
            int hauteurGauche = (gauche != null) ? gauche.hauteur() : 0;
            int hauteurDroit = (droit != null) ? droit.hauteur() : 0;
            noeud.setHauteur(1 + Math.max(hauteurGauche, hauteurDroit));
        }
    }

    /**
     * Rotation droite
     */
    private void rotationDroite() {
        if (!estVide() && gauche != null && !gauche.estVide()) {
            NoeudAVL oldNoeud = this.noeud;
            ArbreAVL oldDroit = this.droit;

            this.noeud = this.gauche.noeud;
            
            ArbreAVL nouveauDroit = new ArbreAVL(oldNoeud);
            nouveauDroit.gauche = this.gauche.droit;
            nouveauDroit.droit = oldDroit;
            
            this.droit = nouveauDroit;
            this.gauche = this.gauche.gauche;

            if (this.droit != null) {
                this.droit.updateHauteur();
            }
            this.updateHauteur();
        }
    }

    /**
     * Rotation gauche
     */
    private void rotationGauche() {
        if (!estVide() && droit != null && !droit.estVide()) {
            NoeudAVL oldNoeud = this.noeud;
            ArbreAVL oldGauche = this.gauche;

            this.noeud = this.droit.noeud;
            
            ArbreAVL nouveauGauche = new ArbreAVL(oldNoeud);
            nouveauGauche.droit = this.droit.gauche;
            nouveauGauche.gauche = oldGauche;
            
            this.gauche = nouveauGauche;
            this.droit = this.droit.droit;

            if (this.gauche != null) {
                this.gauche.updateHauteur();
            }
            this.updateHauteur();
        }
    }

    /**
     * Équilibrer l'arbre après insertion ou suppression
     */
    private void equilibrer() {
        int balance = getBalance();

        // Cas gauche-gauche
        if (balance > 1 && gauche != null && gauche.getBalance() >= 0) {
            rotationDroite();
        }
        // Cas gauche-droit
        else if (balance > 1 && gauche != null && gauche.getBalance() < 0) {
            gauche.rotationGauche();
            rotationDroite();
        }
        // Cas droit-droit
        else if (balance < -1 && droit != null && droit.getBalance() <= 0) {
            rotationGauche();
        }
        // Cas droit-gauche
        else if (balance < -1 && droit != null && droit.getBalance() > 0) {
            droit.rotationDroite();
            rotationGauche();
        }
    }

    /**
     * Insérer une racine dans l'arbre
     */
    public boolean inserer(String racine) {
        if (estVide()) {
            this.noeud = new NoeudAVL(racine);
            this.gauche = new ArbreAVL();
            this.droit = new ArbreAVL();
            return true;
        }

        int comparaison = racine.compareTo(noeud.getRacine());

        if (comparaison < 0) {
            boolean inserted = gauche.inserer(racine);
            updateHauteur();
            equilibrer();
            return inserted;
        } else if (comparaison > 0) {
            boolean inserted = droit.inserer(racine);
            updateHauteur();
            equilibrer();
            return inserted;
        } else {
            // La racine existe déjà
            return false;
        }
    }

    /**
     * Rechercher une racine
     */
    public NoeudAVL rechercher(String racine) {
        if (estVide()) {
            return null;
        }

        if (noeud.getRacine().equals(racine)) {
            return noeud;
        }

        if (racine.compareTo(noeud.getRacine()) < 0) {
            return gauche.rechercher(racine);
        } else {
            return droit.rechercher(racine);
        }
    }

    /**
     * Vérifier si une racine existe
     */
    public boolean existe(String racine) {
        return rechercher(racine) != null;
    }

    /**
     * Supprimer une racine
     */
    public boolean supprimer(String racine) {
        if (estVide()) {
            return false;
        }

        int comparaison = racine.compareTo(noeud.getRacine());

        if (comparaison < 0) {
            boolean deleted = gauche.supprimer(racine);
            updateHauteur();
            equilibrer();
            return deleted;
        } else if (comparaison > 0) {
            boolean deleted = droit.supprimer(racine);
            updateHauteur();
            equilibrer();
            return deleted;
        } else {
            // Noeud trouvé, le supprimer
            if (gauche.estVide() && droit.estVide()) {
                // Feuille
                this.noeud = null;
                this.gauche = null;
                this.droit = null;
            } else if (gauche.estVide()) {
                // Pas de fils gauche
                this.noeud = droit.noeud;
                this.gauche = droit.gauche;
                this.droit = droit.droit;
            } else if (droit.estVide()) {
                // Pas de fils droit
                this.noeud = gauche.noeud;
                this.droit = gauche.droit;
                this.gauche = gauche.gauche;
            } else {
                // Deux fils : remplacer par le minimum du sous-arbre droit
                NoeudAVL min = droit.trouverMin();
                this.noeud.setRacine(min.getRacine());
                this.noeud.setListeDerives(min.getListeDerives());
                this.noeud.setFrequenceRacine(min.getFrequenceRacine());
                droit.supprimer(min.getRacine());
            }

            updateHauteur();
            equilibrer();
            return true;
        }
    }

    /**
     * Trouver le minimum dans le sous-arbre
     */
    private NoeudAVL trouverMin() {
        if (gauche != null && !gauche.estVide()) {
            return gauche.trouverMin();
        }
        return noeud;
    }

    /**
     * Parcours infixe (ordre croissant)
     */
    public void parcourirInfixe(List<String> liste) {
        if (!estVide()) {
            if (gauche != null) {
                gauche.parcourirInfixe(liste);
            }
            liste.add(noeud.getRacine());
            if (droit != null) {
                droit.parcourirInfixe(liste);
            }
        }
    }

    /**
     * Parcours infixe (retourne une liste)
     */
    public List<String> parcourirInfixe() {
        List<String> liste = new ArrayList<>();
        parcourirInfixe(liste);
        return liste;
    }

    /**
     * Obtenir tous les noeuds
     */
    public void obtenirTousLesNoeuds(List<NoeudAVL> liste) {
        if (!estVide()) {
            if (gauche != null) {
                gauche.obtenirTousLesNoeuds(liste);
            }
            liste.add(noeud);
            if (droit != null) {
                droit.obtenirTousLesNoeuds(liste);
            }
        }
    }

    /**
     * Obtenir tous les noeuds (retourne une liste)
     */
    public List<NoeudAVL> obtenirTousLesNoeuds() {
        List<NoeudAVL> liste = new ArrayList<>();
        obtenirTousLesNoeuds(liste);
        return liste;
    }

    /**
     * Compter le nombre de racines
     */
    public int getNombreRacines() {
        if (estVide()) {
            return 0;
        }

        int count = 1;
        if (gauche != null) {
            count += gauche.getNombreRacines();
        }
        if (droit != null) {
            count += droit.getNombreRacines();
        }
        return count;
    }

    @Override
    public String toString() {
        if (estVide()) {
            return "[]";
        }
        
        String gaucheStr = (gauche != null && !gauche.estVide()) ? 
            gauche.noeud.getRacine() : "∅";
        String droitStr = (droit != null && !droit.estVide()) ? 
            droit.noeud.getRacine() : "∅";
        
        return "[" + noeud.getRacine() + ", G:" + gaucheStr + ", D:" + droitStr + "]";
    }
}