package com.aicode.usercenter.controller;

import com.aicode.usercenter.model.ApiErrorResponse;
import com.aicode.usercenter.model.AuthResponse;
import com.aicode.usercenter.model.LoginRequest;
import com.aicode.usercenter.model.SessionUser;
import com.aicode.usercenter.model.UserAccount;
import com.aicode.usercenter.service.AuthTokenService;
import com.aicode.usercenter.service.UserAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@Tag(name = "imds-AI 认证接口", description = "兼容 imds-AI 前端登录流程的认证接口")
public class AuthController {

    private final UserAuthService userAuthService;
    private final AuthTokenService authTokenService;

    public AuthController(UserAuthService userAuthService, AuthTokenService authTokenService) {
        this.userAuthService = userAuthService;
        this.authTokenService = authTokenService;
    }

    @PostMapping("/auth/login")
    @Operation(
            summary = "imds-AI 登录",
            description = "按 imds-AI 前端要求返回 token、tokenType、expiresIn 和 username。",
            requestBody = @RequestBody(
                    required = true,
                    description = "登录请求参数",
                    content = @Content(
                            schema = @Schema(implementation = LoginRequest.class),
                            examples = @ExampleObject(
                                    name = "登录示例",
                                    value = """
                                            {
                                              "username": "chenwangshan",
                                              "password": "123456"
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "登录成功",
                    content = @Content(
                            schema = @Schema(implementation = AuthResponse.class),
                            examples = @ExampleObject(
                                    name = "成功响应",
                                    value = """
                                            {
                                              "expiresIn": 28800,
                                              "token": "8f9f4b9d4f924e63a2b3e8b72ea0d221",
                                              "tokenType": "Bearer",
                                              "username": "chenwangshan"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "用户名或密码错误",
                    content = @Content(
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "认证失败",
                                    value = """
                                            {
                                              "message": "Invalid username or password"
                                            }
                                            """
                            )
                    )
            )
    })
    public ResponseEntity<?> login(@Valid @org.springframework.web.bind.annotation.RequestBody LoginRequest request) {
        return userAuthService.authenticate(request.username(), request.password())
                .<ResponseEntity<?>>map(this::buildLoginSuccessResponse)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Invalid username or password")));
    }

    @GetMapping("/users/me")
    @Operation(
            summary = "获取当前登录用户",
            description = "根据 Bearer Token 返回当前登录用户信息。请先调用 `/api/auth/login` 获取 token，再在 `Authorization` 请求头中传入 `Bearer <token>`。",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "获取成功",
                    content = @Content(
                            schema = @Schema(implementation = SessionUser.class),
                            examples = @ExampleObject(
                                    name = "用户信息",
                                    value = """
                                            {
                                              "enabled": true,
                                              "id": 1,
                                              "username": "chenwangshan"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "令牌无效、缺失或已过期",
                    content = @Content(
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject(
                                    name = "未授权",
                                    value = """
                                            {
                                              "message": "Unauthorized"
                                            }
                                            """
                            )
                    )
            )
    })
    public ResponseEntity<?> currentUser(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        return extractBearerToken(authorization)
                .flatMap(authTokenService::resolveUser)
                .<ResponseEntity<?>>map(userAccount -> ResponseEntity.ok(new SessionUser(
                        isEnabled(userAccount),
                        userAccount.id(),
                        userAccount.username()
                )))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Unauthorized")));
    }

    private ResponseEntity<AuthResponse> buildLoginSuccessResponse(UserAccount userAccount) {
        String token = authTokenService.issueToken(userAccount);
        return ResponseEntity.ok(new AuthResponse(
                AuthTokenService.EXPIRES_IN_SECONDS,
                token,
                "Bearer",
                userAccount.username()
        ));
    }

    private Optional<String> extractBearerToken(String authorization) {
        if (authorization == null || authorization.isBlank()) {
            return Optional.empty();
        }

        if (!authorization.startsWith("Bearer ")) {
            return Optional.empty();
        }

        String token = authorization.substring("Bearer ".length()).trim();
        return token.isEmpty() ? Optional.empty() : Optional.of(token);
    }

    private boolean isEnabled(UserAccount userAccount) {
        return "ACTIVE".equalsIgnoreCase(userAccount.status());
    }
}
