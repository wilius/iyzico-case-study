package com.iyzico.challenge.integrator.dto.product.request;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

public class CreateProductRequest {
    @NotNull
    private String name;

    @Min(0)
    @NotNull
    private long stockCount;

    @Min(0)
    @NotNull
    private BigDecimal price;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getStockCount() {
        return stockCount;
    }

    public void setStockCount(long stockCount) {
        this.stockCount = stockCount;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
