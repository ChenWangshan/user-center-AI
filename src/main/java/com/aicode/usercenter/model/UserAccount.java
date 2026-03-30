package com.aicode.usercenter.model;

import io.swagger.v3.oas.annotations.Hidden;

@Hidden
public record UserAccount(
        Long id,
        String username,
        String password,
        String displayName,
        String status
) {
}
