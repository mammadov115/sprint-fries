package com.sprintfries.api.filter;

import com.sprintfries.api.config.TenantContext;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import org.springframework.stereotype.Component;

@Component
public class TenantFilter implements Filter {
    private static final String TENANT_HEADER = "X-Tenant-ID";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String tenantId = httpRequest.getHeader(TENANT_HEADER);

        if (tenantId != null) {
            TenantContext.setCurrentTenant(tenantId);
        } else {
            TenantContext.setCurrentTenant("public"); // Tapılmayanda defolt olaraq public qəbul edirik
        }

        try {
            chain.doFilter(request, response);
        } finally {
            TenantContext.clear(); // Sorğu bitəndə thread-i təmizləyirik
        }
    }
}