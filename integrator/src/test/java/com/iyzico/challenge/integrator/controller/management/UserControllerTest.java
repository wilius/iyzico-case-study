package com.iyzico.challenge.integrator.controller.management;

import com.iyzico.challenge.integrator.data.entity.User;
import com.iyzico.challenge.integrator.data.service.UserService;
import com.iyzico.challenge.integrator.dto.ListResponse;
import com.iyzico.challenge.integrator.dto.user.UserDto;
import com.iyzico.challenge.integrator.dto.user.request.CreateUserRequest;
import com.iyzico.challenge.integrator.dto.user.request.UpdateUserRequest;
import com.iyzico.challenge.integrator.mapper.UserMapper;
import mockit.Injectable;
import mockit.Mocked;
import mockit.StrictExpectations;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;

@RunWith(JMockit.class)
public class UserControllerTest {
    @Tested
    private UserController tested;

    @Injectable
    private UserService service;

    @Injectable
    private UserMapper mapper;

    @Test
    public void get(@Mocked User user,
                    @Mocked UserDto dto) {
        long id = 1L;
        new StrictExpectations() {{
            service.getById(id);
            result = user;

            mapper.map(user);
            result = dto;
        }};

        UserDto result = tested.get(id);
        Assert.assertEquals(dto, result);
    }

    @Test
    public void getAll(@Mocked User entity,
                       @Mocked UserDto dto) {
        Iterable<User> entities = Collections.singletonList(entity);
        Iterable<UserDto> dtos = Collections.singletonList(dto);
        new StrictExpectations() {{
            service.getAll();
            result = entities;

            mapper.map(entities);
            result = dtos;
        }};

        ListResponse<UserDto> result = tested.getAll();
        Assert.assertEquals(dtos, result.getItems());
    }

    @Test
    public void create(@Mocked CreateUserRequest request,
                       @Mocked User entity,
                       @Mocked UserDto dto) {
        new StrictExpectations() {{
            service.createUser(request);
            result = entity;

            mapper.map(entity);
            result = dto;

        }};

        UserDto result = tested.create(request);
        Assert.assertEquals(dto, result);
    }

    @Test
    public void update(@Mocked User entity,
                       @Mocked UserDto dto) {
        long id = 1L;
        String password = "password";
        boolean admin = true;

        UpdateUserRequest request = new UpdateUserRequest();
        request.setId(id);
        request.setPassword(password);
        request.setAdmin(admin);

        new StrictExpectations() {{
            service.updateUser(id, password, admin);
            result = entity;

            mapper.map(entity);
            result = dto;
        }};

        UserDto result = tested.update(request);
        Assert.assertEquals(dto, result);
    }

    @Test
    public void inactivate() {
        long id = 1L;

        new StrictExpectations() {{
            service.inactivate(id);
        }};

        tested.inactivate(id);
    }
}