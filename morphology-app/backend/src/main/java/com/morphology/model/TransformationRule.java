package com.morphology.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TransformationRule {
    private RootType typeRacine;
    private String conditionScheme;
    private String transformation;
    private TransformationType type;
    private int positionCible;
    private String lettreSource;
    private String lettreCible;
    
    public enum TransformationType {
        HAMZA_CONVERSION,
        ALIF_CONVERSION,
        YAA_MAQSURA,
        DELETION,
        ASSIMILATION,
        SHADDA_ADDITION
    }
    
    public TransformationRule(RootType typeRacine, String conditionScheme, 
        String transformation, TransformationType type) {
        this.typeRacine = typeRacine;
        this.conditionScheme = conditionScheme;
        this.transformation = transformation;
        this.type = type;
        this.positionCible = 0;
        this.lettreSource = "";
        this.lettreCible = "";
    }
}