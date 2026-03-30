package com.aicode.usercenter.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "SystemUserResponse", description = "用户管理响应对象")
public record SystemUserResponse(
        @Schema(description = "用户 ID", example = "1")
        Long id,
        @Schema(description = "用户名", example = "chenwangshan")
        String username,
        @Schema(description = "手机号", example = "13800000000")
        String phone,
        @Schema(description = "邮箱", example = "chenwangshan@example.com")
        String email,
        @Schema(description = "密码", example = "123456")
        String password
) {
}
