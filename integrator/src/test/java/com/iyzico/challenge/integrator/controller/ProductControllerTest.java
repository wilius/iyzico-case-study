package com.iyzico.challenge.integrator.controller;

import com.iyzico.challenge.integrator.data.entity.Product;
import com.iyzico.challenge.integrator.data.service.ProductService;
import com.iyzico.challenge.integrator.dto.ListResponse;
import com.iyzico.challenge.integrator.dto.product.ProductDto;
import com.iyzico.challenge.integrator.mapper.ProductMapper;
import mockit.StrictExpectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.StrictExpectations;
import mockit.Tested;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

public class ProductControllerTest {
    @Tested
    private ProductController tested;

    @Injectable
    private ProductMapper mapper;

    @Injectable
    private ProductService service;

    @Test
    public void get(@Mocked Product entity,
                    @Mocked ProductDto dto) {
        long id = 1L;
        new StrictExpectations() {{
            service.getPublishedItem(id);
            result = entity;

            mapper.mapWithDescription(entity);
            result = dto;
        }};

        ProductDto result = tested.get(id);
        Assert.assertEquals(dto, result);

    }

    @Test
    public void getAll(@Mocked Product entity,
                       @Mocked ProductDto dto) {

        Iterable<Product> products = Collections.singletonList(entity);
        Iterable<ProductDto> dtos = Collections.singletonList(dto);
        new StrictExpectations() {{
            service.getAllPublishedItems();
            result = products;

            mapper.map(products);
            result = dtos;
        }};

        ListResponse<ProductDto> result = tested.getAll();
        Assert.assertNotNull(result);
        Assert.assertNotNull(result.getItems());
        Assert.assertEquals(dtos, result.getItems());
    }
}