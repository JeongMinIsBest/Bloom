package com.rootimpact.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfiguration {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("https://bloom-frontend-new.vercel.app", "https://localhost:3000", "http://bloom-frontend-new.vercel.app", "http://localhost:3000", "https://bloom3.site")  // 특정 domain 허용.
                        .allowedHeaders("Content-Type", "Authorization", "X-Custom-Header")  // 모든 Header 허용
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS", "PATCH") // 허용할 HTTP 메서드
                        .allowCredentials(true); // 자격 증명 허용 (쿠키 포함 요청 가능)
            }
        };
    }
}