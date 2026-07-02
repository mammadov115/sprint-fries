package com.sprintfries.api.controller;

import com.sprintfries.api.config.TenantContext;
import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @RestController: DRF-dəki APIView və ya Django Ninja-dakı Router-in tam qarşılığıdır.
 * Spring-ə deyir ki, bu sinif HTTP sorğularını qəbul edir və metodların qaytardığı data
 * (məsələn, Map/Lüğət) avtomatik olaraq JSON formatına (HttpResponse) çevrilir.
 * DRF-dəki Response(data) render mexanizmini bu tək bir annotasiya həll edir.
 */
@RestController
public class PingController {

    /**
     * @GetMapping("/ping"): Django urls.py-dakı path('ping/', views.ping_view) kimidir.
     * Bu metoda gələn HTTP GET sorğularını birbaşa bura bağlayır (Routing).
     * 
     * Map<String, String>: Python-dakı standart dict (lüğət) strukturunun Java qarşılığıdır.
     */
    @GetMapping("/ping")
    public Map<String, String> ping() {
        // Cavab üçün boş bir lüğət (dict) yaradırıq
        Map<String, String> response = new HashMap<>();
        
        // DRF-də response = {"status": "UP"} yazmaq kimidir
        response.put("status", "UP");
        response.put("message", "SprintFries API is frying smoothly!");
        
        /*
         * TenantContext.getCurrentTenant(): Django Middleware daxilində request.tenant 
         * obyektini oxumaq kimidir. Qapıdakı mühafizəçinin (TenantFilter) cari işçinin (Thread) 
         * yaddas cibinə qoyduğu aktiv müştəri ID-sini buradan çəkirik.
         */
        response.put("active_tenant", TenantContext.getCurrentTenant());
        
        // Bu Map avtomatik olaraq JSON-a çevrilib (serialize olunub) 200 OK ilə müştəriyə qayıdır
        return response;
    }
}