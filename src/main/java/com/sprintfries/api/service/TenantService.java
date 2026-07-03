package com.sprintfries.api.service;

import com.sprintfries.api.dto.TenantRegisterDto;
import com.sprintfries.api.entity.Tenant;
import com.sprintfries.api.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Bu klass bir Spring Bean-dir. Biznes məntiqi tam olaraq burda reallaşır.
@Service
@RequiredArgsConstructor // Constructor-based Dependency Injection-ı avtomatik edir (Lombok tərəfindən)
public class TenantService {

    private final TenantRepository tenantRepository;
    // Spring-in yerli SQL işlətmək üçün olan alətidir, cursor execution kimidir.
    private final JdbcTemplate jdbcTemplate;

    // Django-dakı @transaction.atomic dekoratorudur. 
    // Metod daxilində nəsə partlasa, bütün DB əməliyyatları rollback olunacaq.
    @Transactional
    public Tenant registerTenant(TenantRegisterDto dto) {
        
        // Validation: Subdomen yoxlanılır
        if (tenantRepository.findBySubdomain(dto.getSubdomain()).isPresent()) {
            throw new IllegalArgumentException("Bu subdomen artıq qeydiyyatdan keçib!");
        }

        if (dto.getAdminEmail() == null || dto.getAdminEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Admin email-i boş ola bilməz!");
        }

        if (dto.getAdminPassword() == null || dto.getAdminPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Admin şifrəsi boş ola bilməz!");
        }

        String targetSchema = "tenant_" + dto.getSubdomain().toLowerCase().replaceAll("[^a-z0-9]", "");

        // 1. "public" şemadakı mərkəzi cədvələ tenantı yazırıq
        Tenant tenant = new Tenant();
        tenant.setName(dto.getName());
        tenant.setSubdomain(dto.getSubdomain().toLowerCase());
        tenant.setSchemaName(targetSchema);
        tenant = tenantRepository.save(tenant);

        // 2. Verilənlər bazasında bu tenant üçün yeni isolated Şema (Schema) yaradırıq
        jdbcTemplate.execute("CREATE SCHEMA " + targetSchema);

        // 3. Yeni yaradılan şemanın içinə əsas cədvəlləri quraşdırırıq.
        // Gələcəkdə bura Liquibase/Flyway inteqrasiya olunacaq.
        jdbcTemplate.execute("CREATE TABLE " + targetSchema + ".users (" +
                "id BIGSERIAL PRIMARY KEY, " +
                "email VARCHAR(255) NOT NULL UNIQUE, " +
                "password VARCHAR(255) NOT NULL, " +
                "full_name VARCHAR(255), " +
                "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                ")");

        // 4. İlk admin istifadəçisini yaradırıq
        // JPA ilə şema keçidi etmək mürəkkəb ola bilər, ona görə birbaşa JDBC ilə daxil edirik.
        jdbcTemplate.update("INSERT INTO " + targetSchema + ".users (email, password, full_name) VALUES (?, ?, ?)",
                dto.getAdminEmail(), dto.getAdminPassword(), "Admin " + dto.getName());

        return tenant;
    }
}