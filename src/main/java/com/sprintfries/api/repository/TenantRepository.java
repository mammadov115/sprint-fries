package com.sprintfries.api.repository;

import com.sprintfries.api.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

// Django-dakı Tenant.objects (BaseManager) qatıdır.
// JpaRepository-ni extends etməklə save(), findById(), delete() kimi metodlar avtomatik gəlir.
@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {
    
    // Spring Data JPA bu funksiyanın adından avtomatik SQL sorğusu generasiya edir:
    // SELECT * FROM public.tenants WHERE subdomain = :subdomain
    // Optional qaytarır ki, Django-dakı Tenant.DoesNotExist xətasını handle etmək asan olsun.
    Optional<Tenant> findBySubdomain(String subdomain);
}