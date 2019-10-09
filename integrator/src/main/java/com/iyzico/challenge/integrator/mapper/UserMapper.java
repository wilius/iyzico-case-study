package com.iyzico.challenge.integrator.mapper;

import com.iyzico.challenge.integrator.data.entity.User;
import com.iyzico.challenge.integrator.dto.UserDto;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserMapper() {
    }

    public UserDto map(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setName(user.getName());
        dto.setAdmin(user.isAdmin());

        return dto;
    }
}
