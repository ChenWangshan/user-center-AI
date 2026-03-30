package com.aicode.usercenter.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "LoginRequest", description = "登录请求对象")
public record LoginRequest(
        @Schema(description = "登录用户名", example = "demo_user", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "username cannot be blank")
        String username,
        @Schema(description = "登录密码", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "password cannot be blank")
        String password
) {
}
