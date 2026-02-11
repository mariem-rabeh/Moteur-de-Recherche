package com.morphology.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ValidateWordRequest {
    
    @NotBlank(message = "Le mot ne peut pas être vide")
    private String word;
    
    @NotBlank(message = "La racine ne peut pas être vide")
    private String root;
}