package com.iyzico.challenge.integrator.dto.user;

import javax.validation.constraints.NotNull;

public class UpdateUserRequest {
    @NotNull
    private long id;

    @NotNull
    private String name;

    private String password;

    private boolean admin;

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
