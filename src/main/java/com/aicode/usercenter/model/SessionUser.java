package com.aicode.usercenter.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "UserProfile", description = "imds-AI 当前登录用户信息")
public record SessionUser(
        @Schema(description = "账号是否启用", example = "true")
        boolean enabled,
        @Schema(description = "用户 ID", example = "1")
        Long id,
        @Schema(description = "用户名", example = "chenwangshan")
        String username
) {
}
