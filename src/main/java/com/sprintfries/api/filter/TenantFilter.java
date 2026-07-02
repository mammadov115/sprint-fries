package com.sprintfries.api.filter;

import com.sprintfries.api.config.TenantContext;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import org.springframework.stereotype.Component;

/**
 * @Component: Spring-ə deyir ki, "Bu sinfi tətbiq işə düşəndə avtomatik aşkar et və bir Bean (obyekt) olaraq yarat".
 * Django-da middleware-i 'MIDDLEWARE' siyahısına əllə yazmalı idik, Spring-də isə bu annotasiya kifayətdir.
 * 
 * 'Filter' interfeysini implement etməklə, bu sinfi rəsmi olaraq HTTP sorğu-cavab zəncirinə daxil edirik.
 */
@Component
public class TenantFilter implements Filter {
    
    // Müştərinin öz kimliyini (Tenant ID) göndərəcəyi HTTP Header adı.
    private static final String TENANT_HEADER = "X-Tenant-ID";

    /**
     * doFilter: Gələn hər bir HTTP sorğusu Controller-ə (View-ya) çatmamışdan qabaq mütləq bu metoddan keçir
     * Django-dakı `process_request` və ya middleware-in daxilindəki `__call__` metodunun tam qarşılığıdır.
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        // ServletRequest-i HTTP-yə spesifik olan HttpServletRequest-ə cast edirik ki, Header-ləri oxuya bilək
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        
        // Django-dakı `request.headers.get('X-Tenant-ID')` məntiqi ilə eynidir.
        String tenantId = httpRequest.getHeader(TENANT_HEADER);

        if (tenantId != null) {
            // Əgər başlıq tapıldısa, mərkəzi kontekstimizə (cari thread-in cibinə) yazırıq
            TenantContext.setCurrentTenant(tenantId);
        } else {
            // Əgər başlıq yoxdursa, defolt olaraq ümumi ("public") sxem təyin edirik
            TenantContext.setCurrentTenant("public");
        }

        try {
            /*
             * Sorğunu zəncirdəki növbəti filterə və ya hədəf Controller-ə ötürürük
             * Django-dakı `response = self.get_response(request)` çağırışının tam ekvivalentidir.
             */
            chain.doFilter(request, response);
        } finally {
            /*
             * finally bloku: Sorğu emal edilib bitdikdən və müştəriyə cavab qaytarılandan SONRA mütləq işə düşür
             * Tomcat-in Thread-ləri təkrar istifadə olunduğu üçün, bu thread-in yaddaşını təmizləyirik.
             * Bunu etməsək, sonrakı gələn tamamilə fərqli bir sorğu köhnə tenantın datasına daxil ola bilər (Təhlükəsizlik açığı!).
             */
            TenantContext.clear();
        }
    }
}