package com.iyzico.challenge.integrator.data.entity;

import mockit.StrictExpectations;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDateTime;

@RunWith(JMockit.class)
public class UserTest {
    @Tested
    User tested;

    @Test
    public void mapping_test() {
        long id = 1;
        long userProfileId = 2;
        String username = "username";
        String password = "password";
        LocalDateTime lastLoginDate = LocalDateTime.now();
        String lastSessionKey = "lastSessionKey";
        boolean admin = false;
        boolean active = true;

        UserProfile profile = new UserProfile();

        new StrictExpectations() {{
        }};

        tested.setId(id);
        tested.setLastLoginDate(lastLoginDate);
        tested.setProfile(profile);
        tested.setActive(active);
        tested.setAdmin(admin);
        tested.setLastSessionKey(lastSessionKey);
        tested.setPassword(password);
        tested.setUsername(username);
        tested.setUserProfileId(userProfileId);

        Assert.assertEquals(id, tested.getId());
        Assert.assertEquals(lastLoginDate, tested.getLastLoginDate());
        Assert.assertEquals(profile, tested.getProfile());
        Assert.assertEquals(active, tested.isActive());
        Assert.assertEquals(admin, tested.isAdmin());
        Assert.assertEquals(lastSessionKey, tested.getLastSessionKey());
        Assert.assertEquals(password, tested.getPassword());
        Assert.assertEquals(username, tested.getUsername());
        Assert.assertEquals(userProfileId, tested.getUserProfileId());
    }
}