package com.aicode.usercenter.service;

import com.aicode.usercenter.model.UserAccount;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class AuthTokenService {

    public static final long EXPIRES_IN_SECONDS = 8 * 60 * 60;

    private final ConcurrentMap<String, TokenSession> sessions = new ConcurrentHashMap<>();

    public String issueToken(UserAccount userAccount) {
        String token = UUID.randomUUID().toString().replace("-", "");
        Instant expiresAt = Instant.now().plusSeconds(EXPIRES_IN_SECONDS);
        sessions.put(token, new TokenSession(userAccount, expiresAt));
        return token;
    }

    public Optional<UserAccount> resolveUser(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }

        TokenSession session = sessions.get(token);
        if (session == null) {
            return Optional.empty();
        }

        if (session.expiresAt().isBefore(Instant.now())) {
            sessions.remove(token);
            return Optional.empty();
        }

        return Optional.of(session.userAccount());
    }

    private record TokenSession(UserAccount userAccount, Instant expiresAt) {
    }
}
