package com.iyzico.challenge.integrator.dto.product.request;

import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

public class CreateProductRequest {
    @NotNull
    @Length(min = 5, max = 512)
    private String name;

    @NotNull
    @Length(min = 3, max = 32)
    private String barcode;

    @Min(0)
    @NotNull
    private long stockCount;

    @Min(0)
    @NotNull
    private BigDecimal price;

    @NotNull
    @Length(min = 20)
    private String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
