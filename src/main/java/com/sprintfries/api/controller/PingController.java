package com.sprintfries.api.controller;

import com.sprintfries.api.config.TenantContext;
import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @RestController: Spring Boot-a deyir ki, bu sinif HTTP sorğularını qəbul edən bir Controller-dir.
 * Django-dakı view funksiyaları və ya Django Ninja Router-inin tam qarşılığıdır[cite: 1, 2].
 * Bu annotasiya daxilindəki metodların qaytardığı datanı (məs. Map) avtomatik olaraq JSON formatına çevirir[cite: 1, 2].
 */
@RestController
public class PingController {

    /**
     * @GetMapping("/ping"): HTTP GET sorğularını "/ping" url-i ilə bu metoda bağlayır[cite: 1, 2].
     * Django-dakı `path('ping/', ping_view)` marşrutlaşdırması ilə eyni işi görür[cite: 1, 2].
     */
    @GetMapping("/ping")
    public Map<String, String> ping() {
        // Java-da JSON obyekti qaytarmaq üçün ən sadə yol Key-Value lüğəti (Map) yaratmaqdır[cite: 1, 2].
        // Python-dakı standart `response = {}` dict strukturunun Java ekvivalentidir[cite: 2].
        Map<String, String> response = new HashMap<>();
        
        response.put("status", "UP"); //[cite: 1, 2]
        response.put("message", "SprintFries API is frying smoothly!"); //[cite: 1, 2]
        
        /*
         * Filter (Middleware) tərəfindən cari Thread-in yaddaşına yazılmış tenant ID-ni oxuyuruq[cite: 1, 2].
         * Django-da hər funksiyaya `request` arqumentini ötürürdük və `request.tenant` deyirdik[cite: 2].
         * Spring-də static context sayəsində arqument daşımadan, birbaşa istənilən yerdən cari tenantı çəkə bilirik[cite: 2].
         */
        response.put("active_tenant", TenantContext.getCurrentTenant()); //[cite: 1, 2]
        
        return response; // Spring bu Map-i götürüb avtomatik 200 OK statusu ilə JSON olaraq render edir[cite: 1, 2].
    }
}