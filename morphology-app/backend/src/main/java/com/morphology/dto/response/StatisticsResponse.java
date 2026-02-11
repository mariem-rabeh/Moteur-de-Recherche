package com.morphology.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsResponse {
    private int totalRoots;
    private int totalSchemes;
    private int totalDerivatives;
    private int totalGenerated;
    private double avgDerivatives;
    private int totalFrequency;
}