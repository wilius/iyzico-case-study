package com.iyzico.challenge.integrator.dto.product;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;

public class ProductDto {
    private long id;
    private String barcode;
    private String name;
    private long stockCount;
    private long awaitingDeliveryCount;
    private Status status;
    private BigDecimal price;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String description;

    public enum Status {
        IN_STOCK, OUT_OF_STOCK, UNPUBLISHED
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

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

    public long getAwaitingDeliveryCount() {
        return awaitingDeliveryCount;
    }

    public void setAwaitingDeliveryCount(long awaitingDeliveryCount) {
        this.awaitingDeliveryCount = awaitingDeliveryCount;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
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
