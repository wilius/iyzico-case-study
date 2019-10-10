package com.iyzico.challenge.integrator.dto.selling;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class AddProductToTheBasketRequest {
    @Min(0)
    @NotNull
    private long productId;

    @Min(0)
    @NotNull
    private int count;

    public long getProductId() {
        return productId;
    }

    public void setProductId(long productId) {
        this.productId = productId;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
