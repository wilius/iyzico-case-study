package com.iyzico.challenge.integrator.mapper;

import com.iyzico.challenge.integrator.data.entity.Basket;
import com.iyzico.challenge.integrator.data.entity.BasketProduct;
import com.iyzico.challenge.integrator.dto.basket.BasketDto;
import com.iyzico.challenge.integrator.dto.basket.BasketProductDto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
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
        BigDecimal total = BigDecimal.ZERO;
        List<BasketProductDto> products = new ArrayList<>(basket.getProducts().size());
        for (BasketProduct product : basket.getProducts()) {
            BasketProductDto productDto = new BasketProductDto();
            productDto.setId(product.getId());
            productDto.setCount(product.getCount());
            productDto.setProduct(productMapper.map(product.getProduct()));
            total = total.add(product.getProduct().getPrice());
            products.add(productDto);
        }

        dto.setTotal(total);
        dto.setProducts(products);
        return dto;
    }
}
