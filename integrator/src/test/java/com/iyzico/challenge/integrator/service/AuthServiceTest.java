package com.iyzico.challenge.integrator.service;

import com.iyzico.challenge.integrator.data.entity.User;
import com.iyzico.challenge.integrator.data.service.UserService;
import com.iyzico.challenge.integrator.exception.UserNotFoundException;
import com.iyzico.challenge.integrator.exception.auth.InvalidCredentialsException;
import mockit.Injectable;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.StrictExpectations;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMockit.class)
public class AuthServiceTest {
    @Tested
    private AuthService tested;

    @Injectable
    private UserService userService;

    @Test(expected = InvalidCredentialsException.class)
    public void getUser_nullValueFromQuery() {
        String username = "username";
        String password = "password";
        new StrictExpectations() {{
            userService.getUserByUsername(username);
            result = null;
        }};

        tested.getUser(username, password);
    }

    @Test(expected = InvalidCredentialsException.class)
    public void getUser_UserNotFoundException() {
        String username = "username";
        String password = "password";
        new StrictExpectations() {{
            userService.getUserByUsername(username);
            result = new UserNotFoundException("test");
        }};

        tested.getUser(username, password);
    }

    @Test(expected = InvalidCredentialsException.class)
    public void getUser_InvalidPassword(@Mocked User user) {
        String username = "username";
        String password = "password";
        new NonStrictExpectations() {{
            user.getPassword();
            result = password + password;
        }};

        new StrictExpectations() {{
            userService.getUserByUsername(username);
            result = user;
        }};

        tested.getUser(username, password);
    }

    @Test(expected = InvalidCredentialsException.class)
    public void getUser_UserNotActive(@Mocked User user) {
        String username = "username";
        String password = "password";
        new NonStrictExpectations() {{
            user.getPassword();
            result = password;

            user.isActive();
            result = false;
        }};

        new StrictExpectations() {{
            userService.getUserByUsername(username);
            result = user;
        }};

        tested.getUser(username, password);
    }

    @Test
    public void getUser(@Mocked User user) {
        String username = "username";
        String password = "password";
        new NonStrictExpectations() {{
            user.getPassword();
            result = password;

            user.isActive();
            result = true;
        }};

        new StrictExpectations() {{
            userService.getUserByUsername(username);
            result = user;
        }};

        User result = tested.getUser(username, password);
        Assert.assertNotNull(result);
        Assert.assertEquals(user, result);
    }

    @Test
    public void markAsLoggedIn(@Mocked User user) {
        String lastSessionKey = "lastSessionKey";
        new StrictExpectations() {{
            userService.markAsLoggedIn(user, lastSessionKey);
        }};

        tested.markAsLoggedIn(user, lastSessionKey);
    }
}