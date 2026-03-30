package com.aicode.usercenter.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ApiErrorResponse", description = "接口错误响应")
public record ApiErrorResponse(
        @Schema(description = "错误消息", example = "Unauthorized")
        String message
) {
}
