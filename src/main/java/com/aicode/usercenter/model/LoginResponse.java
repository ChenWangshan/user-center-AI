package com.aicode.usercenter.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "LoginResponse", description = "登录响应对象")
public record LoginResponse(
        @Schema(description = "是否登录成功", example = "true")
        boolean success,
        @Schema(description = "响应消息", example = "Login successful")
        String message,
        @Schema(description = "用户 ID", example = "1001", nullable = true)
        Long userId,
        @Schema(description = "用户名", example = "demo_user", nullable = true)
        String username,
        @Schema(description = "显示名称", example = "Demo User", nullable = true)
        String displayName,
        @Schema(description = "账号状态", example = "ACTIVE", nullable = true)
        String status
) {
}
