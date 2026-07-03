package com.sprintfries.api.controller;

import com.sprintfries.api.dto.TenantRegisterDto;
import com.sprintfries.api.entity.Tenant;
import com.sprintfries.api.service.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// DRF-dəki APIView və ya @api_view(['POST']) ekvivalentidir.
@RestController
@RequestMapping("/api/public/tenants") // Bütün endpoint-lər bu prefikslə başlayacaq
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;

    // DRF-dəki def post(self, request): metodudur
    // @RequestBody anonsu gələn HTTP body-dəki JSON-ı avtomatik TenantRegisterDto-ya çevirir (De-serialization).
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody TenantRegisterDto dto) {
        try {
            Tenant newTenant = tenantService.registerTenant(dto);
            // Response status: 201 Created və body olaraq Tenant datası qaytarılır
            return new ResponseEntity<>(newTenant, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            // Xəta mesajını geri qaytarırıq ki, müştəri nəyin səhv olduğunu bilsin
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }
}