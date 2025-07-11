package com.tt343ereij33.configuration;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import javax.sql.DataSource;

/**
 * Provides general application configuration and property management.
 *
 * <p>This class loads application-level properties from the {@code application.properties}
 * file in the classpath using the {@code @PropertySource} annotation and can define
 * beans related to application infrastructure, such as data sources, service layer
 * beans, or property placeholder configurations.
 *
 * <p>This configuration is loaded in the root application context via
 * {@code getRootConfigClasses()} in {@code WebApplicationInitializer} so that these
 * properties and beans are available across the entire application, including to
 * other configuration classes such as {@code WebSecurityConfig}.
 */
@Configuration
@PropertySource("classpath:application.properties")
public class ApplicationConfig {
    @Value("${dataSource.driverClassName}")
    private String driverClassName;

    @Bean
    public DataSource dataSourceHikari() {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(System.getenv("DB_URL"));
        hikariConfig.setUsername(System.getenv("DB_USERNAME"));
        hikariConfig.setPassword(System.getenv("DB_PASSWORD"));
        hikariConfig.setDriverClassName(driverClassName);
        return new HikariDataSource(hikariConfig);
    }
}
