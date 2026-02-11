package com.morphology.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DecompositionResponse {
    private boolean success;
    private String word;
    private String root;
    private String scheme;
    private List<String> addedElements;
    private String message;
}