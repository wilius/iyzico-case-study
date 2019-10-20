package com.iyzico.challenge.integrator.controller;

import com.iyzico.challenge.integrator.data.entity.User;
import com.iyzico.challenge.integrator.dto.auth.LoginRequest;
import com.iyzico.challenge.integrator.dto.user.UserDto;
import com.iyzico.challenge.integrator.exception.auth.InvalidCredentialsException;
import com.iyzico.challenge.integrator.mapper.UserMapper;
import com.iyzico.challenge.integrator.service.AuthService;
import com.iyzico.challenge.integrator.session.model.ApiSession;
import com.iyzico.challenge.integrator.session.wrapper.ApiSessionRequestWrapper;
import mockit.StrictExpectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.StrictExpectations;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMockit.class)
public class AuthControllerTest {
    @Tested
    private AuthController tested;

    @Injectable
    private AuthService service;

    @Injectable
    private UserMapper mapper;

    @Test(expected = InvalidCredentialsException.class)
    public void login_Exception_InvalidCredentialsException(@Mocked ApiSessionRequestWrapper session) {

        String username = "username";
        String password = "password";

        LoginRequest request = new LoginRequest();
        request.setUsername(username);
        request.setPassword(password);
        new StrictExpectations() {{
            service.getUser(username, password);
            result = new InvalidCredentialsException("test");
        }};

        tested.login(request, session);
    }

    @Test
    public void login(@Mocked ApiSessionRequestWrapper session,
                      @Mocked User user,
                      @Mocked UserDto dto) {

        String sessionKey = "sessionKey";
        String username = "username";
        String password = "password";

        LoginRequest request = new LoginRequest();
        request.setUsername(username);
        request.setPassword(password);
        new StrictExpectations() {{
            service.getUser(username, password);
            result = user;

            session.createSession(user);

            session.getRequestedSessionId();
            result = sessionKey;

            service.markAsLoggedIn(user, sessionKey);

            mapper.map(user);
            result = dto;
        }};

        UserDto result = tested.login(request, session);
        Assert.assertEquals(dto, result);
    }

    @Test
    public void logout(@Mocked ApiSession session) {
        new StrictExpectations() {{
            session.invalidate();
        }};

        tested.logout(session);
    }
}