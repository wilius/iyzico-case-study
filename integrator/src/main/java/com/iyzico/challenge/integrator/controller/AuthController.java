package com.iyzico.challenge.integrator.controller;

import com.iyzico.challenge.integrator.data.entity.User;
import com.iyzico.challenge.integrator.dto.auth.LoginRequest;
import com.iyzico.challenge.integrator.dto.user.UserDto;
import com.iyzico.challenge.integrator.mapper.UserMapper;
import com.iyzico.challenge.integrator.service.AuthService;
import com.iyzico.challenge.integrator.session.annotation.IntegratorSession;
import com.iyzico.challenge.integrator.session.model.ApiSession;
import com.iyzico.challenge.integrator.session.wrapper.ApiSessionRequestWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    private final UserMapper userMapper;

    @Autowired
    public AuthController(AuthService authService,
                          UserMapper userMapper) {
        this.authService = authService;
        this.userMapper = userMapper;
    }

    @RequestMapping(path = "/login", method = RequestMethod.POST)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public UserDto login(@RequestBody @Valid LoginRequest request,
                         @ApiIgnore ApiSessionRequestWrapper sessionRequest) {
        User user = authService.getUser(request.getUsername(), request.getPassword());
        sessionRequest.createSession(user);
        return userMapper.map(user);
    }

    @RequestMapping(path = "/logout", method = RequestMethod.GET)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logout(@ApiIgnore @IntegratorSession ApiSession session) {
        session.invalidate();
    }
}
