package com.iyzico.challenge.integrator.mapper;

import com.iyzico.challenge.integrator.data.entity.Product;
import com.iyzico.challenge.integrator.dto.product.ProductDto;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

@Component
public class ProductMapper {

    public List<ProductDto> map(Iterable<Product> products) {
        List<ProductDto> dtos = new LinkedList<>();
        for (Product product : products) {
            dtos.add(map(product));
        }

        return dtos;
    }

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
