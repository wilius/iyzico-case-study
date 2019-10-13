package com.iyzico.challenge.integrator.dto.basket;

import com.iyzico.challenge.integrator.dto.product.ProductDto;

import java.math.BigDecimal;

public class BasketProductDto {
    private long id;
    private int count;
    private BigDecimal subtotal;
    private ProductDto product;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal total) {
        this.subtotal = total;
    }

    public ProductDto getProduct() {
        return product;
    }

    public void setProduct(ProductDto product) {
        this.product = product;
    }
}
