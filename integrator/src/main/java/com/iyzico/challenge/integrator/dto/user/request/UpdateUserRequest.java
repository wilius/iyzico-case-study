package com.iyzico.challenge.integrator.dto.user.request;

import javax.validation.constraints.NotNull;

public class UpdateUserRequest {
    @NotNull
    private long id;

    private String password;

    private boolean admin;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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
