package com.sprintfries.api.config;

/**
 * TenantContext: Cari HTTP sorğusunu emal edən Thread daxilində 
 * Tenant (müştəri) ID-sini izolyasiya olunmuş şəkildə saxlamaq üçün mərkəz.
 */
public class TenantContext {

    /* 
     * ThreadLocal: Django-dakı threading.local() mexanizminin tam ekvivalentidir.
     * Django-da hər request-in öz konteksti olduğu kimi, Spring Boot-da da hər bir 
     * HTTP sorğusu ayrıca bir Thread (işçi) tərəfindən idarə olunur. 
     * ThreadLocal hər thread-ə yalnız özünün daxil ola biləcəyi xüsusi bir yaddaş cibi verir.
     * Beləliklə, Uber və Volt-dan eyni anda gələn sorğuların tenant məlumatları əsla qarışmır.
     */
    private static final ThreadLocal<String> currentTenant = new ThreadLocal<>();

    /**
     * Cari sorğu (Thread) üçün Tenant ID-ni yaddaşa yazır.
     * Django-dakı `request.tenant = tenant_id` mənimsədilməsinin Spring qarşılığıdır.
     * Bu metod adətən Filter (Middleware) tərəfindən sorğu qapıdan girəndə çağırılır.
     */
    public static void setCurrentTenant(String tenantId) { 
        currentTenant.set(tenantId); 
    }

    /**
     * Cari sorğuya aid olan Tenant ID-ni yaddaşdan oxuyur.
     * Django-da View daxilində `request.tenant` deməklə eynidir.
     * Gələcəkdə verilənlər bazası (Database Routing) sorğularında hansı sxemə 
     * və ya bazaya müraciət edəcəyimizi bu metod vasitəsilə təyin edəcəyik.
     */
    public static String getCurrentTenant() { 
        return currentTenant.get(); 
    }

    /**
     * Sorğu tamamlandıqdan sonra cari thread-in yaddasını tamamilə təmizləyir.
     * ÇOX VACİBDİR: Django-da sorğu bitəndə request obyekti avtomatik məhv edilir. 
     * Amma Spring Boot-da resurs effektivliyi üçün "Thread Pool" (işçi hovuzu) istifadə olunur, 
     * yəni eyni Thread növbəti başqa bir müştərinin sorğusunu qəbul edə bilər. 
     * Əgər clear() etməsək, növbəti gələn anonim müştəri köhnə müştərinin datasına (Tenant ID) sahib olar 
     * və bu da ağır "Memory Leak" (yaddaş sızması) və təhlükəsizlik açığı deməkdir.
     */
    public static void clear() { 
        currentTenant.remove(); 
    }
}