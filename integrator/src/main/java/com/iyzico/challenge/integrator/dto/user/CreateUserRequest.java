package com.iyzico.challenge.integrator.dto.user;

import javax.validation.constraints.NotNull;

public class CreateUserRequest {
    @NotNull
    private String name;

    @NotNull
    private String username;

    @NotNull
    private String password;

    private boolean admin = false;

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }
}
