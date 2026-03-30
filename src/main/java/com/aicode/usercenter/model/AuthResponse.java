package com.aicode.usercenter.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "AuthResponse", description = "imds-AI 登录响应对象")
public record AuthResponse(
        @Schema(description = "令牌有效期，单位秒", example = "28800")
        long expiresIn,
        @Schema(description = "访问令牌", example = "8f9f4b9d4f924e63a2b3e8b72ea0d221")
        String token,
        @Schema(description = "令牌类型", example = "Bearer")
        String tokenType,
        @Schema(description = "用户名", example = "chenwangshan")
        String username
) {
}
