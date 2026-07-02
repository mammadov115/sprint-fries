package com.sprintfries.api.config;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;

/**
 * CurrentTenantIdentifierResolver: Hibernate-in xüsusi interfeysidir.
 * Hər SQL sorğusunda bura müraciət edib "Aktiv tenant kimdir?" deyə soruşur.
 */
@Component
public class TenantSchemaResolver implements CurrentTenantIdentifierResolver<String> {

    private static final String DEFAULT_SCHEMA = "public";

    @Override
    public String resolveCurrentTenantIdentifier() {
        String tenant = TenantContext.getCurrentTenant();
        // Əgər filter-dən heç bir tenant gəlməyibsə (məs. qeydiyyat API-sında), public şemasına yönləndiririk.
        return tenant != null ? tenant : DEFAULT_SCHEMA;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        // Hər sorğuda session-ların yenidən yoxlanılmasını təmin edirik.
        return true;
    }
}