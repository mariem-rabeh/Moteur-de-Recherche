package com.morphology.model;

import lombok.Getter;

@Getter
public enum RootType {
    SALIM("سالم", "Saine", "Aucune lettre faible - conjugaison régulière", "🟢"),
    MAHMOUZ("مهموز", "Hamzée", "Contient une Hamza (أ, إ, ؤ, ئ, ء)", "🟡"),
    MOUDAAF("مضعف", "Doublée", "L2 = L3 (lettres identiques avec Shadda)", "🟡"),
    MITHAL("مثال", "Assimilée", "Commence par و ou ي (disparaît souvent)", "🔴"),
    AJWAF("أجوف", "Concave", "Lettre faible au milieu (و/ي → ا/ء)", "🔴"),
    NAQIS("ناقص", "Défectueuse finale", "Se termine par و ou ي (→ ى)", "🔴"),
    LAFEEF("لفيف", "Double faiblesse", "2 lettres faibles ou plus", "🔴");
    
    private final String nomArabe;
    private final String nomFrancais;
    private final String description;
    private final String emoji;
    
    RootType(String nomArabe, String nomFrancais, String description, String emoji) {
        this.nomArabe = nomArabe;
        this.nomFrancais = nomFrancais;
        this.description = description;
        this.emoji = emoji;
    }
}