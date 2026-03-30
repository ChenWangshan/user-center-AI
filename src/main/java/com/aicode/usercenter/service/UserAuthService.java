package com.aicode.usercenter.service;

import com.aicode.usercenter.model.UserAccount;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserAuthService {

    private static final RowMapper<UserAccount> USER_ACCOUNT_ROW_MAPPER = (rs, rowNum) -> new UserAccount(
            rs.getLong("id"),
            rs.getString("username"),
            rs.getString("password"),
            rs.getString("display_name"),
            rs.getString("status")
    );

    private final JdbcTemplate jdbcTemplate;

    public UserAuthService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<UserAccount> authenticate(String username, String password) {
        String sql = """
                SELECT id, username, password, display_name, status
                FROM users
                WHERE username = ? AND password = ?
                """;

        List<UserAccount> users = jdbcTemplate.query(sql, USER_ACCOUNT_ROW_MAPPER, username, password);
        return users.stream().findFirst();
    }
}
