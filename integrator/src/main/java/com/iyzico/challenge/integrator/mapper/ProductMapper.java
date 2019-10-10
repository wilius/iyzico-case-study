package com.iyzico.challenge.integrator.mapper;

import com.iyzico.challenge.integrator.data.entity.Product;
import com.iyzico.challenge.integrator.dto.product.ProductDto;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductDto map(Product product) {
        ProductDto dto = new ProductDto();

        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setStockCount(product.getStockCount());
        dto.setAwaitingDeliveryCount(product.getAwaitingDeliveryCount());
        dto.setPrice(product.getPrice());
        dto.setStatus(ProductDto.Status.valueOf(product.getStatus().name()));

        return dto;
    }
}
