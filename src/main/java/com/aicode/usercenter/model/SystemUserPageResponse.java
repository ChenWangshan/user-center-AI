package com.aicode.usercenter.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "SystemUserPageResponse", description = "用户分页查询响应")
public record SystemUserPageResponse(
        @Schema(description = "用户列表数据")
        List<SystemUserResponse> data,
        @Schema(description = "总记录数", example = "1")
        long recordsTotal
) {
}
