package com.iyzico.challenge.integrator.dto.basket;

import java.math.BigDecimal;
import java.util.List;

public class BasketDto {
    private BigDecimal total;

    private List<BasketProductDto> products;

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public List<BasketProductDto> getProducts() {
        return products;
    }

    public void setProducts(List<BasketProductDto> products) {
        this.products = products;
    }
}
