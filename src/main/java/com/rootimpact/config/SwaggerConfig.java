package com.rootimpact.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI CustomOpenAPI() {
        Info info = new Info()
                .title("Bloom")
                .version("1.0")
                .description("Bloom");

        String jwtSchemeName = "jwtAuth";

        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);

        Components components = new Components()
                .addSecuritySchemes(jwtSchemeName, new SecurityScheme().name(jwtSchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("Bearer").bearerFormat("JWT"));
        // 안녕안녕
        // 서버 설정을 추가하여 https로 호출하도록 지정
        Server server = new Server()
                .url("https://13.125.19.104/api") // 실제 도메인과 경로로 변경
                .description("Production server");

        return new OpenAPI()
                .components(components)
                .info(info)
                .addSecurityItem(securityRequirement)
                .addServersItem(server); // servers에 https 설정 추가
    }
}