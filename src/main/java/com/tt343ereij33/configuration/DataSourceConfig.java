package com.tt343ereij33.configuration;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import javax.sql.DataSource;
import java.util.concurrent.TimeUnit;

@Configuration
@PropertySource("classpath:application.properties")
public class DataSourceConfig {
    @Value("${dataSource.driverClassName}")
    private String driverClassName;

    @Bean
    public DataSource dataSource() {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(System.getenv("DB_URL"));
        hikariConfig.setUsername(System.getenv("DB_USERNAME"));
        hikariConfig.setPassword(System.getenv("DB_PASSWORD"));
        hikariConfig.setDriverClassName(driverClassName);
        hikariConfig.setMaximumPoolSize(15);
        hikariConfig.setMinimumIdle(2);
        hikariConfig.setConnectionTimeout(TimeUnit.SECONDS.toMillis(30));
        hikariConfig.setIdleTimeout(TimeUnit.MINUTES.toMillis(10));
        hikariConfig.setMaxLifetime(TimeUnit.MINUTES.toMillis(30));
        return new HikariDataSource(hikariConfig);
    }
}