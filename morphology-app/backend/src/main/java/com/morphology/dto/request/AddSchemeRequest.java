package com.morphology.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddSchemeRequest {
    
    @NotBlank(message = "Le nom du schème ne peut pas être vide")
    private String name;
    
    @NotBlank(message = "La règle ne peut pas être vide")
    private String rule;
}