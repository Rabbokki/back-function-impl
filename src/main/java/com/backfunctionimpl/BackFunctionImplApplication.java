package com.backfunctionimpl;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableJpaAuditing
@EnableTransactionManagement
@EnableCaching
public class BackFunctionImplApplication {

    @Value("${DB_USERNAME:not-found}")
    private String dbUsername;

    @Value("${DB_PASSWORD:not-found}")
    private String dbPassword;

    @Value("${google.maps-key}")
    private String apiKey;

    public static void main(String[] args) {
        // ✅ .env 파일을 명시적으로 읽어 환경변수로 등록
        Dotenv dotenv = Dotenv.configure()
                .filename(".env")
                .directory("./")
                .ignoreIfMalformed()
                .ignoreIfMissing()
                .load();

        dotenv.entries().forEach(entry -> {
            System.setProperty(entry.getKey(), entry.getValue());
        });

        SpringApplication.run(BackFunctionImplApplication.class, args);
        System.out.println("✅ Application started.");
    }

    @PostConstruct
    public void debugEnvVars() {
        System.out.println("🔐 DB_USERNAME: " + dbUsername);
        System.out.println("🔐 DB_PASSWORD: " + dbPassword);
        System.out.println("🗺️ GOOGLE_MAPS_KEY in Application = " + apiKey);
    }

}
