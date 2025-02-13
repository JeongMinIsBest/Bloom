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
                        .allowedOriginPatterns("*")  // 모든 Origin 허용
                        .allowedHeaders("*")  // 모든 Header 허용
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS", "PATCH") // 허용할 HTTP 메서드
                        .exposedHeaders("Authorization", "RefreshToken") // 클라이언트에서 접근할 수 있도록 노출할 헤더
                        .allowCredentials(true); // 자격 증명 허용 (쿠키 포함 요청 가능)
            }
        };
    }
}