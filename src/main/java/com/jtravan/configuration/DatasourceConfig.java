package com.jtravan.configuration;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import java.util.Properties;

@Configuration
public class DatasourceConfig {

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    @Bean
    @ConfigurationProperties(prefix = "datasource.postgresql")
    @Primary
    public HikariDataSource datasource() {
        HikariDataSource hikariDataSource = new HikariDataSource();

        hikariDataSource.setDataSource(DataSourceBuilder.create()
                .driverClassName(driverClassName)
                .url(url)
                .username(username)
                .password(password)
                .type(HikariDataSource.class)
                .build());

        Properties properties = new Properties();
        properties.put("socketFactory", "com.google.cloud.sql.postgres.SocketFactory");
        properties.put("cloudSqlInstance", "plenary-shade-228414:us-east1:dynamic-reputation-database");
        hikariDataSource.setDataSourceProperties(properties);

        return hikariDataSource;
    }
}
