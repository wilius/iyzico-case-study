package com.iyzico.challenge.integrator.dto.product.request;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class UpdateProductRequest extends CreateProductRequest {

    @NotNull
    @Min(0)
    private long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
