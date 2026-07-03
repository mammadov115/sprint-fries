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
    public ResponseEntity<Tenant> register(@RequestBody TenantRegisterDto dto) {
        try {
            Tenant newTenant = tenantService.registerTenant(dto);
            // Response status: 21 Created və body olaraq Tenant datası qaytarılır
            return new ResponseEntity<>(newTenant, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            // DRF-dəki return Response({"error": ...}, status=status.HTTP_400_BAD_REQUEST)
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }
}