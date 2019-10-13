package com.iyzico.challenge.integrator.mapper;

import com.iyzico.challenge.integrator.data.entity.Basket;
import com.iyzico.challenge.integrator.data.entity.BasketProduct;
import com.iyzico.challenge.integrator.dto.basket.BasketDto;
import com.iyzico.challenge.integrator.dto.basket.BasketProductDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class BasketMapper {
    private final ProductMapper productMapper;

    public BasketMapper(ProductMapper productMapper) {
        this.productMapper = productMapper;
    }

    public BasketDto map(Basket basket) {
        BasketDto dto = new BasketDto();
        List<BasketProductDto> products = new ArrayList<>(basket.getProducts().size());
        for (BasketProduct product : basket.getProducts()) {
            BasketProductDto productDto = new BasketProductDto();
            productDto.setId(product.getId());
            productDto.setCount(product.getCount());
            productDto.setProduct(productMapper.map(product.getProduct()));

            productDto.setSubtotal(product.getSubtotal());
            products.add(productDto);
        }

        dto.setTotal(basket.getTotal());
        dto.setProducts(products);
        return dto;
    }
}
