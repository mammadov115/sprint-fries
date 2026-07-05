package com.sprintfries.api.controller;

import com.sprintfries.api.config.TenantContext;
import com.sprintfries.api.dto.LoginRequestDto;
import com.sprintfries.api.dto.LoginResponseDto;
import com.sprintfries.api.exception.AccountLockedException;
import com.sprintfries.api.exception.BadCredentialsException;
import com.sprintfries.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto dto) {
        String currentTenant = TenantContext.getCurrentTenant();
        
        // Ensure a tenant context is established (cannot login under public schema)
        if (currentTenant == null || "public".equalsIgnoreCase(currentTenant)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Tenant context is required! Provide a valid Host header or X-Tenant-ID header."));
        }

        try {
            String token = userService.login(dto.getEmail(), dto.getPassword(), currentTenant);
            return ResponseEntity.ok(new LoginResponseDto(token, "Bearer"));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        } catch (AccountLockedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "error", e.getMessage(),
                            "remainingMinutes", e.getRemainingMinutes()
                    ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
