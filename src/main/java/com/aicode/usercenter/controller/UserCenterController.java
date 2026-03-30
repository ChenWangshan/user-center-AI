package com.aicode.usercenter.controller;

import com.aicode.usercenter.model.LoginRequest;
import com.aicode.usercenter.model.LoginResponse;
import com.aicode.usercenter.model.UserAccount;
import com.aicode.usercenter.model.UserProfileResponse;
import com.aicode.usercenter.service.UserAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user-center")
@Tag(name = "用户中心", description = "用户中心相关接口")
public class UserCenterController {

    private final UserAuthService userAuthService;

    public UserCenterController(UserAuthService userAuthService) {
        this.userAuthService = userAuthService;
    }

    @GetMapping("/health")
    @Operation(summary = "服务健康检查", description = "返回当前用户中心服务的运行状态与关键词信息")
    public Map<String, Object> health() {
        return Map.of(
                "service", "user-center",
                "status", "UP",
                "keywords", List.of("user-center", "user", "account", "profile")
        );
    }

    @GetMapping("/profile/demo")
    @Operation(summary = "获取演示用户资料", description = "返回一个用于接口演示的示例用户资料")
    public UserProfileResponse demoProfile() {
        return new UserProfileResponse(
                1001L,
                "demo_user",
                "Demo User",
                "ACTIVE"
        );
    }

    @PostMapping("/login")
    @Operation(
            summary = "用户登录",
            description = "使用用户名和密码进行身份认证，认证成功后返回用户基础信息。",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "登录请求参数",
                    content = @Content(
                            schema = @Schema(implementation = LoginRequest.class),
                            examples = @ExampleObject(
                                    name = "登录示例",
                                    value = """
                                            {
                                              "username": "demo_user",
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
                            schema = @Schema(implementation = LoginResponse.class),
                            examples = @ExampleObject(
                                    name = "成功响应",
                                    value = """
                                            {
                                              "success": true,
                                              "message": "Login successful",
                                              "userId": 1001,
                                              "username": "demo_user",
                                              "displayName": "Demo User",
                                              "status": "ACTIVE"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "用户名或密码错误",
                    content = @Content(
                            schema = @Schema(implementation = LoginResponse.class),
                            examples = @ExampleObject(
                                    name = "认证失败",
                                    value = """
                                            {
                                              "success": false,
                                              "message": "Invalid username or password",
                                              "userId": null,
                                              "username": "demo_user",
                                              "displayName": null,
                                              "status": null
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "请求参数校验失败")
    })
    public ResponseEntity<LoginResponse> login(@Valid @org.springframework.web.bind.annotation.RequestBody LoginRequest request) {
        return userAuthService.authenticate(request.username(), request.password())
                .map(this::buildSuccessLoginResponse)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        new LoginResponse(
                                false,
                                "Invalid username or password",
                                null,
                                request.username(),
                                null,
                                null
                        )
                ));
    }

    private ResponseEntity<LoginResponse> buildSuccessLoginResponse(UserAccount userAccount) {
        return ResponseEntity.ok(new LoginResponse(
                true,
                "Login successful",
                userAccount.id(),
                userAccount.username(),
                userAccount.displayName(),
                userAccount.status()
        ));
    }
}
