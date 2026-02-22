package com.morphology.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.morphology.dto.request.GenerateWordRequest;
import com.morphology.dto.response.ApiResponse;
import com.morphology.dto.response.GeneratedWordResponse;
import com.morphology.model.NoeudAVL;
import com.morphology.service.GenerationService;
import com.morphology.service.RootService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/generate")
@RequiredArgsConstructor
public class GenerationController {

    private final GenerationService generationService;
    private final RootService       rootService;

    // ================================================================
    // توليد كلمة واحدة من جذر ووزن
    // ================================================================
    @PostMapping("/word")
    public ResponseEntity<ApiResponse<GeneratedWordResponse>> generateWord(
            @Valid @RequestBody GenerateWordRequest request) {

        log.info("POST /generate/word — الجذر='{}', الوزن='{}'",
            request.getRoot(), request.getScheme());

        if (request.getRoot() == null || request.getRoot().isBlank()) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("لا يمكن أن يكون الجذر فارغاً."));
        }

        if (request.getScheme() == null || request.getScheme().isBlank()) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("لا يمكن أن يكون الوزن فارغاً."));
        }

        GeneratedWordResponse response = generationService.generateWord(
            request.getRoot(), request.getScheme()
        );

        if (!response.isSuccess()) {
            log.warn("❌ فشل التوليد: الجذر='{}', الوزن='{}'",
                request.getRoot(), request.getScheme());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(response.getMessage()));
        }

        log.info("✅ كلمة مولّدة: '{}'", response.getWord());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ================================================================
    // توليد العائلة الصرفية الكاملة لجذر
    // ================================================================
    @PostMapping("/family")
    public ResponseEntity<ApiResponse<List<GeneratedWordResponse>>> generateFamily(
            @RequestBody GenerateWordRequest request) {

        log.info("POST /generate/family — الجذر='{}'", request.getRoot());

        if (request.getRoot() == null || request.getRoot().isBlank()) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("لا يمكن أن يكون الجذر فارغاً."));
        }

        List<GeneratedWordResponse> family = generationService.generateFamily(request.getRoot());

        long succes = family.stream().filter(GeneratedWordResponse::isSuccess).count();
        log.info("✅ تم توليد {}/{} كلمة للجذر '{}'",
            succes, family.size(), request.getRoot());

        return ResponseEntity.ok(ApiResponse.success(family));
    }

    // ================================================================
    // توليد كلمات من جميع الجذور على نفس الوزن
    // FIX : ApiResponse.success() يقبل معامل واحد فقط
    // ================================================================
    @PostMapping("/by-scheme")
    public ResponseEntity<ApiResponse<List<GeneratedWordResponse>>> generateByScheme(
            @RequestBody GenerateWordRequest request) {

        log.info("POST /generate/by-scheme — الوزن='{}'", request.getScheme());

        if (request.getScheme() == null || request.getScheme().isBlank()) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("لا يمكن أن يكون الوزن فارغاً."));
        }

        List<NoeudAVL>              allNodes = rootService.getAllNodes();
        List<GeneratedWordResponse> results  = new ArrayList<>();

        if (allNodes == null || allNodes.isEmpty()) {
            log.warn("⚠️ قاعدة البيانات فارغة، لا توجد جذور مسجّلة.");
            return ResponseEntity.ok(ApiResponse.success(results));
        }

        for (NoeudAVL node : allNodes) {
            try {
                GeneratedWordResponse res = generationService.generateWord(
                    node.getRacine(), request.getScheme()
                );
                if (res.isSuccess() && res.getWord() != null) {
                    results.add(res);
                }
            } catch (Exception e) {
                log.debug("⚠️ تعذّر التوليد: الجذر='{}' + الوزن='{}'",
                    node.getRacine(), request.getScheme());
            }
        }

        log.info("✅ {} نتيجة للوزن '{}'", results.size(), request.getScheme());
        return ResponseEntity.ok(ApiResponse.success(results));
    }
}   