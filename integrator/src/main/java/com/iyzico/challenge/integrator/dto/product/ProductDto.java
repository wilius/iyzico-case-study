package com.iyzico.challenge.integrator.dto.product;

import java.math.BigDecimal;

public class ProductDto {
    private long id;
    private String name;
    private long stockCount;
    private long awaitingDeliveryCount;
    private Status status;
    private BigDecimal price;

    public enum Status {
        IN_STOCK, OUT_OF_STOCK, UNPUBLISHED, DELETED
    }

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
}
