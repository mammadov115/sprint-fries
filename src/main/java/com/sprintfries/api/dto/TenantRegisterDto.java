package com.sprintfries.api.dto;

import lombok.Getter;
import lombok.Setter;

// DRF Serializer-dəki input fields (read_only=False olan sahələr) kimidir.
// Request-dən gələn JSON-ı bu obyektə conversion edirik.
@Getter
@Setter
public class TenantRegisterDto {
    private String name;
    private String subdomain;
    private String adminEmail;
    private String adminPassword;
}