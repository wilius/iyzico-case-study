package com.iyzico.challenge.integrator.dto.user;

import javax.validation.constraints.NotNull;

public class UserDto {
    @NotNull
    private long id;

    @NotNull
    private String username;

    @NotNull
    private boolean admin;

    @NotNull
    private boolean active;

    private UserProfileDto profile;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setProfile(UserProfileDto profile) {
        this.profile = profile;
    }

    public UserProfileDto getProfile() {
        return profile;
    }
}
