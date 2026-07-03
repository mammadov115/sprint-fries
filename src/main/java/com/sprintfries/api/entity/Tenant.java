package com.sprintfries.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

// Django-dakı class Tenant(models.Model) bərabəridir. 
// Bu obyekt verilənlər bazasında "public" şemadakı "tenants" cədvəlinə xəritələnir (map olunur).
@Entity
@Table(name = "tenants", schema = "public")
@Getter
@Setter
public class Tenant {

    // Django-dakı models.BigAutoField(primary_key=True)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Django-dakı models.CharField(max_length=100)
    @Column(nullable = false)
    private String name;

    // unique=true – bu subdomen unikallığı qoruyur (shirket.taskfries.com üçün 'shirket' hissəsi)
    @Column(nullable = false, unique = true)
    private String subdomain;

    @Column(name = "schema_name", nullable = false, unique = true)
    private String schemaName;

    // Django-dakı models.DateTimeField(auto_now_add=True) ekvivalentidir
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}