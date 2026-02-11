package com.morphology.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RootsPageResponse {
    private List<String> roots;
    private int currentPage;
    private int totalPages;
    private long totalElements;
}