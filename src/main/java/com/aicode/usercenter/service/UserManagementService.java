package com.aicode.usercenter.service;

import com.aicode.usercenter.model.SystemUserPayload;
import com.aicode.usercenter.model.UserAccount;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserManagementService {

    private static final RowMapper<UserAccount> USER_ACCOUNT_ROW_MAPPER = (rs, rowNum) -> new UserAccount(
            rs.getLong("id"),
            rs.getString("username"),
            rs.getString("password"),
            rs.getString("phone"),
            rs.getString("email"),
            rs.getString("display_name"),
            rs.getString("status")
    );

    private final JdbcTemplate jdbcTemplate;

    public UserManagementService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<UserAccount> findPage(String username, int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(size, 1);
        int offset = (safePage - 1) * safeSize;

        if (username == null || username.isBlank()) {
            return jdbcTemplate.query("""
                    SELECT id, username, password, phone, email, display_name, status
                    FROM users
                    ORDER BY id DESC
                    LIMIT ? OFFSET ?
                    """, USER_ACCOUNT_ROW_MAPPER, safeSize, offset);
        }

        String keyword = "%" + username.trim() + "%";
        return jdbcTemplate.query("""
                SELECT id, username, password, phone, email, display_name, status
                FROM users
                WHERE username ILIKE ?
                ORDER BY id DESC
                LIMIT ? OFFSET ?
                """, USER_ACCOUNT_ROW_MAPPER, keyword, safeSize, offset);
    }

    public long count(String username) {
        if (username == null || username.isBlank()) {
            Long total = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Long.class);
            return total == null ? 0 : total;
        }

        String keyword = "%" + username.trim() + "%";
        Long total = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE username ILIKE ?", Long.class, keyword);
        return total == null ? 0 : total;
    }

    public Optional<UserAccount> findById(Long id) {
        List<UserAccount> users = jdbcTemplate.query("""
                SELECT id, username, password, phone, email, display_name, status
                FROM users
                WHERE id = ?
                """, USER_ACCOUNT_ROW_MAPPER, id);
        return users.stream().findFirst();
    }

    public UserAccount create(SystemUserPayload payload) {
        Long id = jdbcTemplate.queryForObject("""
                INSERT INTO users (username, password, phone, email, display_name, status)
                VALUES (?, ?, ?, ?, ?, 'ACTIVE')
                RETURNING id
                """, Long.class,
                payload.username().trim(),
                payload.password(),
                payload.phone().trim(),
                payload.email().trim(),
                payload.username().trim());

        return findById(id).orElseThrow();
    }

    public Optional<UserAccount> update(Long id, SystemUserPayload payload) {
        int updated = jdbcTemplate.update("""
                UPDATE users
                SET username = ?, password = ?, phone = ?, email = ?, display_name = ?
                WHERE id = ?
                """,
                payload.username().trim(),
                payload.password(),
                payload.phone().trim(),
                payload.email().trim(),
                payload.username().trim(),
                id);

        if (updated == 0) {
            return Optional.empty();
        }

        return findById(id);
    }

    public boolean delete(Long id) {
        return jdbcTemplate.update("DELETE FROM users WHERE id = ?", id) > 0;
    }
}
