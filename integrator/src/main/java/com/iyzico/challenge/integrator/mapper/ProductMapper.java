package com.iyzico.challenge.integrator.mapper;

import com.iyzico.challenge.integrator.data.entity.LongText;
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
            dtos.add(map(product, false));
        }

        return dtos;
    }

    public ProductDto map(Product product) {
        return map(product, false);
    }

    public ProductDto mapWithDescription(Product product) {
        return map(product, true);
    }

    private ProductDto map(Product product, boolean withDescription) {
        ProductDto dto = new ProductDto();

        dto.setId(product.getId());
        dto.setBarcode(product.getBarcode());
        dto.setName(product.getName());
        dto.setStockCount(product.getStockCount());
        dto.setPrice(product.getPrice());
        dto.setStatus(ProductDto.Status.valueOf(product.getStatus().name()));

        if (withDescription) {
            LongText description = product.getDescription();
            if (description != null) {
                dto.setDescription(description.getContent());
            }
        }

        return dto;
    }
}
