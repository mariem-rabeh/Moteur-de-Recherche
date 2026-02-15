package com.morphology.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RootAnalysisResponse {
    private String racine;
    private String type;
    private String typeArabe;
    private String emoji;
    private String description;
    private String explication;
    private boolean isValid;
    private String errorMessage;
    private String[] lettres;
    
    public static RootAnalysisResponse error(String racine, String errorMessage) {
        return new RootAnalysisResponse(
            racine, null, null, "❌", null, 
            errorMessage, false, errorMessage, null
        );
    }
    
    public static RootAnalysisResponse success(
        String racine, String type, String typeArabe, String emoji,
        String description, String explication, String[] lettres
    ) {
        return new RootAnalysisResponse(
            racine, type, typeArabe, emoji, description,
            explication, true, null, lettres
        );
    }
}