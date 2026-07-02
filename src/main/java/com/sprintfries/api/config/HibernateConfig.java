package com.sprintfries.api.config;

import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.hibernate.cfg.AvailableSettings;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

/**
 * @Configuration: Django settings.py-da xüsusi üçüncü tərəf app konfiqurasiyası yazmaq kimidir.
 * Bu sinif Spring Boot qalxanda mərkəzi Bean-ləri konteynerə yükləyir.
 */
@Configuration
public class HibernateConfig {

    private final JpaProperties jpaProperties;

    public HibernateConfig(JpaProperties jpaProperties) {
        this.jpaProperties = jpaProperties;
    }

    /**
     * @Bean: Django settings-dəki DATABASES mühərrikinin və daxili ORM idarəçisinin əllə qurulmasıdır.
     * LocalContainerEntityManagerFactoryBean: Django-nun mərkəzi backend DB idarəetmə strukturudur.
     * Modelləri skan edir və əlaqələri idarə edir.
     */
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            DataSource dataSource,
            MultiTenantConnectionProvider<String> connectionProvider,
            CurrentTenantIdentifierResolver<String> tenantResolver) {

        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        
        // Django-nun INSTALLED_APPS-da modelləri avtomatik axtarması kimi,
        // bura skan olunacaq əsas paketi veririk ki, @Entity-ləri tapa bilsin.
        em.setPackagesToScan("com.sprintfries.api");

        // Hibernate-i JPA provayderi (mühərriki) olaraq təyin edirik.
        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        // application.yml daxilindən gələn standart JPA (Hibernate) sazlamalarını kopyalayırıq.
        Map<String, Object> properties = new HashMap<>(jpaProperties.getProperties());
        
        // django-tenants paketinin arxada database router və middleware səviyyəsində
        // idarə etdiyi çoxlu bağlantı mühərrikini Hibernate-ə qeydiyyatdan keçiririk:
        properties.put(AvailableSettings.MULTI_TENANT_CONNECTION_PROVIDER, connectionProvider);
        properties.put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, tenantResolver);
        
        // Hibernate 6+ üçün: Sistemə multi-tenancy strategiyamızın SCHEMA (şema) olduğunu deyirik.
        properties.put("hibernate.multiTenancy", "SCHEMA");

        em.setJpaPropertyMap(properties);
        return em;
    }
}