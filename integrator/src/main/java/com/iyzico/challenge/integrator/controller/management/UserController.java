package com.iyzico.challenge.integrator.controller.management;

import com.iyzico.challenge.integrator.data.entity.User;
import com.iyzico.challenge.integrator.data.service.UserService;
import com.iyzico.challenge.integrator.dto.ListResponse;
import com.iyzico.challenge.integrator.dto.user.UserDto;
import com.iyzico.challenge.integrator.dto.user.request.CreateUserRequest;
import com.iyzico.challenge.integrator.dto.user.request.UpdateUserRequest;
import com.iyzico.challenge.integrator.mapper.UserMapper;
import com.iyzico.challenge.integrator.session.SecuredEndpoint;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@SecuredEndpoint(requireAdminPermission = true)
@RequestMapping("management/user")
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;

    @Autowired
    public UserController(UserService userService,
                          UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @ApiOperation(
            value = "Get User With Id",
            notes = "Gets the user with given id that belongs to the same agency"
    )
    @RequestMapping(path = "/{id}", method = RequestMethod.GET)
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public UserDto get(@PathVariable("id") long id) {
        User user = userService.getById(id);
        return userMapper.map(user);
    }

    @ApiOperation(
            value = "Get All Users",
            notes = "Gets all the users defined on application"
    )
    @RequestMapping(method = RequestMethod.GET)
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public ListResponse<UserDto> getAll() {
        return new ListResponse<>(userMapper.map(userService.getAll()));
    }

    @ApiOperation(
            value = "Create User",
            notes = "Creates a new user"
    )
    @RequestMapping(method = RequestMethod.PUT)
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Throwable.class)
    public UserDto create(@RequestBody @Valid CreateUserRequest request) {
        User user = userService.createUser(request);
        return userMapper.map(user);
    }

    @ApiOperation(
            value = "Update User",
            notes = "Updates the user which specified by id in request body with the expected parameters"
    )
    @RequestMapping(method = RequestMethod.POST)
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Throwable.class)
    public UserDto update(@ModelAttribute("request") @Valid UpdateUserRequest request) {
        User user = userService.updateUser(request.getId(), request.getPassword(), request.isAdmin());
        return userMapper.map(user);
    }

    @ApiOperation(
            value = "Inactivate User",
            notes = "Inactivates the user with given id"
    )
    @RequestMapping(path = "/{id}", method = RequestMethod.DELETE)
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Throwable.class)
    public void inactivate(@PathVariable("id") long id) {
        userService.inactivate(id);
    }
}
