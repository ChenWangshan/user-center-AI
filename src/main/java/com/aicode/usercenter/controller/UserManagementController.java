package com.aicode.usercenter.controller;

import com.aicode.usercenter.model.ApiErrorResponse;
import com.aicode.usercenter.model.SystemUserPageResponse;
import com.aicode.usercenter.model.SystemUserPayload;
import com.aicode.usercenter.model.SystemUserResponse;
import com.aicode.usercenter.model.UserAccount;
import com.aicode.usercenter.service.UserManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@Tag(name = "用户管理", description = "兼容 imds-AI 用户管理模块的增删改查接口")
public class UserManagementController {

    private final UserManagementService userManagementService;

    public UserManagementController(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    @GetMapping
    @Operation(summary = "分页查询用户", description = "支持 `currentPage/page/pageNum` 与 `pageSize/size` 参数，并支持按用户名模糊检索。")
    @ApiResponse(
            responseCode = "200",
            description = "查询成功",
            content = @Content(
                    schema = @Schema(implementation = SystemUserPageResponse.class),
                    examples = @ExampleObject(
                            name = "分页响应",
                            value = """
                                    {
                                      "data": [
                                        {
                                          "id": 1,
                                          "username": "chenwangshan",
                                          "phone": "13800000000",
                                          "email": "chenwangshan@example.com",
                                          "password": "123456"
                                        }
                                      ],
                                      "recordsTotal": 1
                                    }
                                    """
                    )
            )
    )
    public SystemUserPageResponse page(
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "currentPage", required = false) Integer currentPage,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "size", required = false) Integer size) {
        int resolvedPage = firstNonNull(currentPage, page, pageNum, 1);
        int resolvedSize = firstNonNull(pageSize, size, 20);

        return new SystemUserPageResponse(
                userManagementService.findPage(username, resolvedPage, resolvedSize)
                        .stream()
                        .map(this::toResponse)
                        .toList(),
                userManagementService.count(username)
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取用户详情", description = "根据用户 ID 获取单个用户详情。")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "查询成功",
                    content = @Content(schema = @Schema(implementation = SystemUserResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "用户不存在",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<?> detail(@PathVariable("id") Long id) {
        return userManagementService.findById(id)
                .<ResponseEntity<?>>map(userAccount -> ResponseEntity.ok(toResponse(userAccount)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiErrorResponse("User not found")));
    }

    @PostMapping
    @Operation(summary = "新增用户", description = "创建用户后返回新建的用户信息。")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "创建成功",
                    content = @Content(schema = @Schema(implementation = SystemUserResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "用户名已存在",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<?> create(@Valid @org.springframework.web.bind.annotation.RequestBody SystemUserPayload payload) {
        try {
            return ResponseEntity.ok(toResponse(userManagementService.create(payload)));
        } catch (DuplicateKeyException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiErrorResponse("Username already exists"));
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改用户", description = "根据用户 ID 更新用户名、手机号、邮箱和密码。")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "更新成功",
                    content = @Content(schema = @Schema(implementation = SystemUserResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "用户不存在",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "用户名已存在",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<?> update(@PathVariable("id") Long id,
                                    @Valid @org.springframework.web.bind.annotation.RequestBody SystemUserPayload payload) {
        try {
            return userManagementService.update(id, payload)
                    .<ResponseEntity<?>>map(userAccount -> ResponseEntity.ok(toResponse(userAccount)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new ApiErrorResponse("User not found")));
        } catch (DuplicateKeyException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiErrorResponse("Username already exists"));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户", description = "根据用户 ID 删除用户。")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "删除成功",
                    content = @Content(
                            examples = @ExampleObject(
                                    name = "删除响应",
                                    value = """
                                            {
                                              "message": "Deleted"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "用户不存在",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<?> delete(@PathVariable("id") Long id) {
        if (userManagementService.delete(id)) {
            return ResponseEntity.ok(Map.of("message", "Deleted"));
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiErrorResponse("User not found"));
    }

    private SystemUserResponse toResponse(UserAccount userAccount) {
        return new SystemUserResponse(
                userAccount.id(),
                userAccount.username(),
                userAccount.phone(),
                userAccount.email(),
                userAccount.password()
        );
    }

    private int firstNonNull(Integer first, Integer second, Integer third, int fallback) {
        if (first != null) {
            return first;
        }
        if (second != null) {
            return second;
        }
        if (third != null) {
            return third;
        }
        return fallback;
    }

    private int firstNonNull(Integer first, Integer second, int fallback) {
        if (first != null) {
            return first;
        }
        if (second != null) {
            return second;
        }
        return fallback;
    }
}
