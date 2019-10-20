package com.iyzico.challenge.integrator.data.entity;

import mockit.StrictExpectations;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDateTime;

@RunWith(JMockit.class)
public class UserProfileTest {
    @Tested
    private UserProfile tested;

    @Test
    public void mapping_test() {
        long id = 1;
        String name = "name";
        String surname = "surname";
        String identityNo = "identityNo";
        String city = "city";
        String country = "country";
        String email = "email";
        String phoneNumber = "phoneNumber";
        String address = "address";
        String zipCode = "zipCode";
        LocalDateTime registrationDate = LocalDateTime.now();

        new StrictExpectations() {{
        }};

        tested.setId(id);
        tested.setIdentityNo(identityNo);
        tested.setEmail(email);
        tested.setCountry(country);
        tested.setCity(city);
        tested.setAddress(address);
        tested.setName(name);
        tested.setPhoneNumber(phoneNumber);
        tested.setRegistrationDate(registrationDate);
        tested.setSurname(surname);
        tested.setZipCode(zipCode);

        Assert.assertEquals(id, tested.getId());
        Assert.assertEquals(identityNo, tested.getIdentityNo());
        Assert.assertEquals(email, tested.getEmail());
        Assert.assertEquals(country, tested.getCountry());
        Assert.assertEquals(city, tested.getCity());
        Assert.assertEquals(address, tested.getAddress());
        Assert.assertEquals(name, tested.getName());
        Assert.assertEquals(phoneNumber, tested.getPhoneNumber());
        Assert.assertEquals(registrationDate, tested.getRegistrationDate());
        Assert.assertEquals(surname, tested.getSurname());
        Assert.assertEquals(zipCode, tested.getZipCode());
    }
}