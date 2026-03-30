package com.aicode.usercenter.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "UserProfileResponse", description = "用户资料响应对象")
public record UserProfileResponse(
        @Schema(description = "用户 ID", example = "1001")
        Long id,
        @Schema(description = "用户名", example = "demo_user")
        String username,
        @Schema(description = "显示名称", example = "Demo User")
        String displayName,
        @Schema(description = "账号状态", example = "ACTIVE")
        String status
) {
}
