package com.morphology.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GenerateWordRequest {
    
    @NotBlank(message = "La racine ne peut pas être vide")
    private String root;
    
    @NotBlank(message = "Le schème ne peut pas être vide")
    private String scheme;
}