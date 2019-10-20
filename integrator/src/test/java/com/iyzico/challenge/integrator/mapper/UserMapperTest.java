package com.iyzico.challenge.integrator.mapper;

import com.iyzico.challenge.integrator.data.entity.User;
import com.iyzico.challenge.integrator.data.entity.UserProfile;
import com.iyzico.challenge.integrator.dto.user.UserDto;
import com.iyzico.challenge.integrator.dto.user.UserProfileDto;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@RunWith(JMockit.class)
public class UserMapperTest {
    @Tested
    private UserMapper tested;

    @Test
    public void map_withNullProfile() {
        User user = new User();
        user.setId(1);
        user.setAdmin(true);
        user.setUsername("username");
        user.setActive(true);

        UserDto result = tested.map(user);
        Assert.assertNotNull(result);
        Assert.assertEquals(user.getId(), result.getId());
        Assert.assertEquals(user.getUsername(), result.getUsername());
        Assert.assertEquals(user.isActive(), result.isActive());
        Assert.assertEquals(user.isAdmin(), result.isAdmin());
        Assert.assertNull(result.getProfile());
    }


    @Test
    public void map() {
        User user = new User();
        user.setId(1);
        user.setAdmin(true);
        user.setUsername("username");
        user.setActive(true);

        UserProfile profile = new UserProfile();
        profile.setZipCode("zipCode");
        profile.setSurname("surname");
        profile.setRegistrationDate(LocalDateTime.now());
        profile.setPhoneNumber("phoneNumber");
        profile.setName("name");
        profile.setAddress("address");
        profile.setCity("city");
        profile.setCountry("country");
        profile.setEmail("email");
        profile.setIdentityNo("identityNo");

        user.setProfile(profile);
        UserDto result = tested.map(user);
        Assert.assertNotNull(result);
        Assert.assertEquals(user.getId(), result.getId());
        Assert.assertEquals(user.getUsername(), result.getUsername());
        Assert.assertEquals(user.isActive(), result.isActive());
        Assert.assertEquals(user.isAdmin(), result.isAdmin());
        UserProfileDto resultProfile = result.getProfile();
        Assert.assertNotNull(resultProfile);

        Assert.assertEquals(profile.getAddress(), resultProfile.getAddress());
        Assert.assertEquals(profile.getCity(), resultProfile.getCity());
        Assert.assertEquals(profile.getCountry(), resultProfile.getCountry());
        Assert.assertEquals(profile.getEmail(), resultProfile.getEmail());
        Assert.assertEquals(profile.getIdentityNo(), resultProfile.getIdentityNo());
        Assert.assertEquals(profile.getName(), resultProfile.getName());
        Assert.assertEquals(profile.getPhoneNumber(), resultProfile.getPhoneNumber());
        Assert.assertEquals(profile.getSurname(), resultProfile.getSurname());
        Assert.assertEquals(profile.getZipCode(), resultProfile.getZipCode());
        Assert.assertEquals(profile.getRegistrationDate(), resultProfile.getRegistrationDate());

    }

    @Test
    public void map_iterable() {
        User user = new User();
        user.setId(1);
        user.setAdmin(true);
        user.setUsername("username");
        user.setActive(true);

        UserProfile profile = new UserProfile();
        profile.setZipCode("zipCode");
        profile.setSurname("surname");
        profile.setRegistrationDate(LocalDateTime.now());
        profile.setPhoneNumber("phoneNumber");
        profile.setName("name");
        profile.setAddress("address");
        profile.setCity("city");
        profile.setCountry("country");
        profile.setEmail("email");
        profile.setIdentityNo("identityNo");

        user.setProfile(profile);
        List<UserDto> result = tested.map(Collections.singletonList(user));
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());

        UserDto dto = result.iterator().next();
        ;
        Assert.assertEquals(user.getId(), dto.getId());
        Assert.assertEquals(user.getUsername(), dto.getUsername());
        Assert.assertEquals(user.isActive(), dto.isActive());
        Assert.assertEquals(user.isAdmin(), dto.isAdmin());
        UserProfileDto resultProfile = dto.getProfile();
        Assert.assertNotNull(resultProfile);

        Assert.assertEquals(profile.getAddress(), resultProfile.getAddress());
        Assert.assertEquals(profile.getCity(), resultProfile.getCity());
        Assert.assertEquals(profile.getCountry(), resultProfile.getCountry());
        Assert.assertEquals(profile.getEmail(), resultProfile.getEmail());
        Assert.assertEquals(profile.getIdentityNo(), resultProfile.getIdentityNo());
        Assert.assertEquals(profile.getName(), resultProfile.getName());
        Assert.assertEquals(profile.getPhoneNumber(), resultProfile.getPhoneNumber());
        Assert.assertEquals(profile.getSurname(), resultProfile.getSurname());
        Assert.assertEquals(profile.getZipCode(), resultProfile.getZipCode());
        Assert.assertEquals(profile.getRegistrationDate(), resultProfile.getRegistrationDate());

    }
}