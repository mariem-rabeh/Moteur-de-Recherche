package com.morphology.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AddRootRequest {
    
    @NotBlank(message = "La racine ne peut pas être vide")
    @Size(min = 3, max = 3, message = "La racine doit contenir exactement 3 caractères")
    private String root;
}