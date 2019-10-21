package com.iyzico.challenge.integrator.session.model;

import com.iyzico.challenge.integrator.data.entity.User;
import com.iyzico.challenge.integrator.service.SessionService;

import java.time.LocalDateTime;

public class UserSession {
    private final SessionService service;
    private final String key;
    private final User user;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    private boolean invalidated = false;

    public UserSession(SessionService service, String key, User user, LocalDateTime createdAt, LocalDateTime lastLogin) {
        this.service = service;
        this.key = key;
        this.user = user;
        this.createdAt = createdAt;
        this.lastLogin = lastLogin;
    }

    public String getSessionKey() {
        if (invalidated) {
            return null;
        }

        return key;
    }

    public User getUser() {
        checkInvalidated();
        return user;
    }

    public LocalDateTime getLastLoginDate() {
        checkInvalidated();
        return lastLogin;
    }

    public LocalDateTime getCreatedDate() {
        checkInvalidated();
        return createdAt;
    }

    void setValue(String key, String value) {
        checkInvalidated();

        service.setSessionValue(this.key, key, value);
    }

    public String getValue(String key) {
        checkInvalidated();

        return service.getSessionValue(this.key, key);
    }

    void deleteValue(String key) {
        checkInvalidated();

        service.deleteSessionValue(this.key, key);
    }

    void invalidate() {
        checkInvalidated();

        service.deleteSession(key);
    }

    private void checkInvalidated() {
        if (invalidated) {
            throw new IllegalStateException("UserSession was invalidated!");
        }
    }
}
