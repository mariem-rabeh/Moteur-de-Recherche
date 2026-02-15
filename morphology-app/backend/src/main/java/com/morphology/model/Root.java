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
    
    public Root(String racine) {
        this.racine = racine != null ? racine.trim() : "";
        this.lettres = extraireLettres(this.racine);
        this.isValid = validerRacine();
    }
    
    private String[] extraireLettres(String racine) {
        String clean = racine.replaceAll("[^\\u0600-\\u06FF]", "");
        
        if (clean.length() != 3) {
            this.errorMessage = "La racine doit contenir exactement 3 lettres arabes";
            return new String[0];
        }
        
        return new String[] {
            String.valueOf(clean.charAt(0)),
            String.valueOf(clean.charAt(1)),
            String.valueOf(clean.charAt(2))
        };
    }
    
    private boolean validerRacine() {
        if (lettres.length != 3) return false;
        
        for (String lettre : lettres) {
            if (!lettre.matches("[\\u0600-\\u06FF]")) {
                errorMessage = "Caractères non arabes détectés";
                return false;
            }
        }
        return true;
    }
    
    public String getL1() { return lettres.length > 0 ? lettres[0] : ""; }
    public String getL2() { return lettres.length > 1 ? lettres[1] : ""; }
    public String getL3() { return lettres.length > 2 ? lettres[2] : ""; }
}