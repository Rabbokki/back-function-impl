package com.backfunctionimpl;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableJpaAuditing
@EnableTransactionManagement
@EnableCaching
public class BackFunctionImplApplication {
    @Value("${DB_USERNAME:not-found}")
    private String dbUsername;

    @Value("${DB_PASSWORD:not-found}")
    private String dbPassword;

    public static void main(String[] args) {
        // 명시적으로 .env 파일 로드
        Dotenv dotenv = Dotenv.configure()
                .filename(".env")
                .directory("./")
                .load();
        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
        SpringApplication.run(BackFunctionImplApplication.class, args);
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~START~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
    }

    @PostConstruct
    public void debugEnvVars() {
        System.out.println("DB_USERNAME: " + dbUsername);
        System.out.println("DB_PASSWORD: " + dbPassword);
    }
}
