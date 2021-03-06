package com.iyzico.challenge.integrator.dto.user.request;

import com.iyzico.challenge.integrator.dto.user.validator.Country;
import com.iyzico.challenge.integrator.dto.user.validator.PhoneNumber;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

public class CreateUserRequest {

    @NotNull
    @Length(min = 3)
    private String username;

    @NotNull
    @Length(min = 4)
    private String password;

    @NotNull
    @Length(min = 3)
    private String name;

    @NotNull
    @Length(min = 2)
    private String surname;

    @NotNull
    @Length(min = 5)
    private String identityNo;

    @NotNull
    @Length(min = 2)
    private String city;

    @NotNull
    @Length(min = 2, max = 3)
    @Country
    private String country;

    @NotNull
    @Email
    private String email;

    @NotNull
    @PhoneNumber
    private String phoneNumber;

    @NotNull
    @Length(min = 10)
    private String address;

    @NotNull
    @Length(min = 3)
    private String zipCode;

    private boolean admin = false;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getIdentityNo() {
        return identityNo;
    }

    public void setIdentityNo(String identityNo) {
        this.identityNo = identityNo;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }
}
