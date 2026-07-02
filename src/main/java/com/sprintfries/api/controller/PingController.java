package com.sprintfries.api.controller;

import com.sprintfries.api.config.TenantContext;
import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PingController {

    @GetMapping("/ping")
    public Map<String, String> ping() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "SprintFries API is frying smoothly!");
        response.put("active_tenant", TenantContext.getCurrentTenant()); // Filterin tutduğu tenant-ı göstəririk
        return response;
    }
}