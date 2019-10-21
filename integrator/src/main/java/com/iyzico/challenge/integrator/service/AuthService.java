package com.iyzico.challenge.integrator.service;

import com.iyzico.challenge.integrator.data.entity.User;
import com.iyzico.challenge.integrator.data.service.UserService;
import com.iyzico.challenge.integrator.exception.BaseIntegratorException;
import com.iyzico.challenge.integrator.exception.auth.InvalidCredentialsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class AuthService {
    private final UserService userService;

    @Autowired
    public AuthService(UserService userService) {
        this.userService = userService;
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY)
    public User getUser(String username, String password) throws InvalidCredentialsException {
        try {
            User user = userService.getUserByUsername(username);
            if (user != null && Objects.equals(user.getPassword(), password) && user.isActive()) {
                return user;
            }
        } catch (BaseIntegratorException ignored) {
        }

        throw new InvalidCredentialsException("Invalid credentials");
    }

    public void markAsLoggedIn(User user, String sessionKey) {
        userService.markAsLoggedIn(user, sessionKey);
    }
}
