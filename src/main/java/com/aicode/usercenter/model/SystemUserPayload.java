package com.aicode.usercenter.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "SystemUserPayload", description = "用户新增或修改请求对象")
public record SystemUserPayload(
        @Schema(description = "用户名", example = "alice")
        @NotBlank(message = "username cannot be blank")
        String username,
        @Schema(description = "手机号", example = "13800000000")
        @NotBlank(message = "phone cannot be blank")
        String phone,
        @Schema(description = "邮箱", example = "alice@example.com")
        @NotBlank(message = "email cannot be blank")
        @Email(message = "email format is invalid")
        String email,
        @Schema(description = "密码", example = "123456")
        @NotBlank(message = "password cannot be blank")
        String password
) {
}
