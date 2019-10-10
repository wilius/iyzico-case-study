package com.iyzico.challenge.integrator.dto.user;

import javax.validation.constraints.NotNull;

public class UserDto {
    @NotNull
    private long id;

    @NotNull
    private String name;

    @NotNull
    private String username;

    @NotNull
    private boolean admin;

    @NotNull
    private boolean active;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
}
