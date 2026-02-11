package com.morphology.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeneratedWordResponse {
    private String word;
    private String root;
    private String scheme;
    private boolean success;
    private String message;
}