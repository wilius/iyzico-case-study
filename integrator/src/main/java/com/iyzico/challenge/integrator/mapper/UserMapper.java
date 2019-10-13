package com.iyzico.challenge.integrator.mapper;

import com.iyzico.challenge.integrator.data.entity.User;
import com.iyzico.challenge.integrator.data.entity.UserProfile;
import com.iyzico.challenge.integrator.dto.user.UserDto;
import com.iyzico.challenge.integrator.dto.user.UserProfileDto;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserMapper() {
    }

    public UserDto map(User user) {
        UserDto dto = new UserDto();

        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setAdmin(user.isAdmin());
        dto.setActive(user.isActive());

        if (user.getProfile() != null) {
            dto.setProfile(map(user.getProfile()));
        }

        return dto;
    }

    public UserProfileDto map(UserProfile profile) {
        UserProfileDto dto = new UserProfileDto();
        dto.setName(profile.getName());
        dto.setSurname(profile.getSurname());
        dto.setIdentityNo(profile.getIdentityNo());
        dto.setCity(profile.getCity());
        dto.setCountry(profile.getCountry());
        dto.setEmail(profile.getEmail());
        dto.setPhoneNumber(profile.getPhoneNumber());
        dto.setAddress(profile.getAddress());
        dto.setZipCode(profile.getZipCode());
        dto.setRegistrationDate(profile.getRegistrationDate());
        return dto;
    }
}
