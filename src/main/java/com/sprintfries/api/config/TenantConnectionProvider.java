package com.sprintfries.api.config;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.stereotype.Component;

/**
 * MultiTenantConnectionProvider: Verilənlər bazası bağlantı hovuzunu (HikariCP) idarə edir.
 * Baza ilə hər təmasda düzgün PostgreSQL şemasına keçid əmrini (search_path) tetikleyir.
 */
@Component
public class TenantConnectionProvider implements MultiTenantConnectionProvider<String> {

    private final DataSource dataSource;

    // Spring avtomatik olaraq application.yml-də qurduğumuz əsas DataSource-u bura inject edir.
    public TenantConnectionProvider(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // Heç bir tenant spesifik olmayan, ümumi (məsələn, public) bir bağlantı lazım olduqda işə düşür.
    // HikariCP hovuzundan birbaşa çiy xətt qopardır.
    @Override
    public Connection getAnyConnection() throws SQLException {
        return dataSource.getConnection();
    }

    // Ümumi bağlantı ilə işimiz bitəndə onu yenidən HikariCP hovuzuna qaytarır.
    // Diqqət et, connection.close() xətti tamamilə məhv etmir, sadəcə hovuza "mən azadam" siqnalı verir.
    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        connection.close();
    }

    @Override
    public Connection getConnection(String tenantIdentifier) throws SQLException {
        Connection connection = getAnyConnection();
        try {
            // Django-da 'django-tenants' paketinin arxada etdiyi iş:
            // PostgreSQL-ə deyirik ki, bu thread-dən gələn sorğularda yalnız bu tenant-ın sxemini gör.
            connection.createStatement().execute("SET search_path TO " + tenantIdentifier);
        } catch (SQLException e) {
            throw new SQLException("PostgreSQL scheme can not be set to " + tenantIdentifier, e);
        }
        return connection;
    }

    @Override
    public void releaseConnection(String tenantIdentifier, Connection connection) throws SQLException {
        try {
            // Təhlükəsizlik üçün bağlantı hovuza (pool) qayıtmazdan əvvəl onu yenidən standart public şemasına sıfırlayırıq.
            connection.createStatement().execute("SET search_path TO public");
        } catch (SQLException e) {
            throw new SQLException("Schema cannot be reset to public", e);
        }
        releaseAnyConnection(connection);
    }

    // Hibernate-ə deyir ki, "Sorğu bitən kimi bağlantını aqressiv şəkildə dərhal əlimdən alma".
    // Biz 'false' qaytarırıq ki, transaction tam bitənə qədər search_path sabit qalsın.
    @Override
    public boolean supportsAggressiveRelease() {
        return false;
    }

    // Aşağıdakı iki metod Java-nın daxili 'Unwrap' interfeysindən gəlir.
    // Əgər kimsə çiy Spring DataSource-u birbaşa altındakı HikariCP obyektinə çevirmək (cast etmək)
    // istəsə, buna icazə verib-verməyəcəyimizi təyin edir. Bizə lazım olmadığı üçün false və null keçirik.
    @Override
    public boolean isUnwrappableAs(Class<?> unwrapType) {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> unwrapType) {
        return null;
    }
}