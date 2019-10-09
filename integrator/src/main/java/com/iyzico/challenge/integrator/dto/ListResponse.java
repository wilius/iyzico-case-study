package com.iyzico.challenge.integrator.dto;

import java.util.List;

public class ListResponse<T> {
    private List<T> items;

    public ListResponse() {
    }

    public ListResponse(List<T> items) {
        this.items = items;
    }

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }
}
