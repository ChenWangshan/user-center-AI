package com.aicode.usercenter.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI userCenterOpenAPI() {
        return new OpenAPI()
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("Token")
                                .description("imds-AI 登录后返回的 Bearer Token")))
                .info(new Info()
                        .title("用户中心接口文档")
                        .description("用户中心服务的 OpenAPI/Swagger 接口文档，包含健康检查、演示资料查询，以及兼容 imds-AI 前端的登录与当前用户查询接口。")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("AI Code Team")
                                .email("support@aicode.local"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(new Server()
                        .url("http://localhost:8081")
                        .description("本地开发环境")));
    }
}
