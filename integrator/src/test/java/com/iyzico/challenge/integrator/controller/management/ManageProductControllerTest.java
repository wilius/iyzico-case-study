package com.iyzico.challenge.integrator.controller.management;

import com.iyzico.challenge.integrator.data.entity.Product;
import com.iyzico.challenge.integrator.data.entity.User;
import com.iyzico.challenge.integrator.data.service.ProductService;
import com.iyzico.challenge.integrator.dto.ListResponse;
import com.iyzico.challenge.integrator.dto.product.ProductDto;
import com.iyzico.challenge.integrator.dto.product.request.CreateProductRequest;
import com.iyzico.challenge.integrator.dto.product.request.UpdateProductRequest;
import com.iyzico.challenge.integrator.mapper.ProductMapper;
import com.iyzico.challenge.integrator.session.model.ApiSession;
import mockit.StrictExpectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigDecimal;
import java.util.Collections;

@RunWith(JMockit.class)
public class ManageProductControllerTest {
    @Tested
    private ManageProductController tested;

    @Injectable
    private ProductService service;

    @Injectable
    private ProductMapper mapper;

    @Test
    public void create(@Mocked ApiSession session,
                       @Mocked User user) {
        String name = "name";
        String barcode = "barcode";
        long stockCount = Long.MAX_VALUE;
        BigDecimal price = BigDecimal.TEN;
        String description = "description";

        CreateProductRequest request = new CreateProductRequest();
        request.setBarcode(barcode);
        request.setDescription(description);
        request.setName(name);
        request.setStockCount(stockCount);
        request.setPrice(price);

        Product product = new Product();
        ProductDto dto = new ProductDto();
        new StrictExpectations() {{
            session.getUser();
            result = user;

            service.create(user, barcode, name, stockCount, price, description);
            result = product;

            mapper.mapWithDescription(product);
            result = dto;
        }};
        ProductDto result = tested.create(request, session);
        Assert.assertEquals(dto, result);
    }

    @Test
    public void update() {
        long id = 1L;
        String name = "name";
        String barcode = "barcode";
        long stockCount = Long.MAX_VALUE;
        BigDecimal price = BigDecimal.TEN;
        String description = "description";

        UpdateProductRequest request = new UpdateProductRequest();
        request.setId(id);
        request.setBarcode(barcode);
        request.setDescription(description);
        request.setName(name);
        request.setStockCount(stockCount);
        request.setPrice(price);

        Product product = new Product();
        ProductDto dto = new ProductDto();
        new StrictExpectations() {{
            service.update(id, barcode, name, stockCount, price, description);
            result = product;

            mapper.mapWithDescription(product);
            result = dto;
        }};

        ProductDto result = tested.update(request);
        Assert.assertEquals(dto, result);
    }

    @Test
    public void publish() {
        long id = 1L;
        new StrictExpectations() {{
            service.publish(id);
        }};

        tested.publish(id);
    }

    @Test
    public void unpublish() {
        long id = 1L;
        new StrictExpectations() {{
            service.unpublish(id);
        }};

        tested.unpublish(id);
    }

    @Test
    public void get(@Mocked Product product,
                    @Mocked ProductDto dto) {
        long id = 1L;
        new StrictExpectations() {{
            service.getById(id);
            result = product;

            mapper.mapWithDescription(product);
            result = dto;
        }};

        ProductDto result = tested.get(id);
        Assert.assertEquals(dto, result);
    }

    @Test
    public void getAll(@Mocked Product product,
                       @Mocked ProductDto dto) {
        Iterable<Product> products = Collections.singletonList(product);
        Iterable<ProductDto> dtos = Collections.singletonList(dto);
        new StrictExpectations() {{
            service.getAllItems();
            result = products;

            mapper.map(products);
            result = dtos;
        }};

        ListResponse<ProductDto> result = tested.getAll();
        Assert.assertNotNull(result);
        Assert.assertNotNull(result.getItems());
        Assert.assertEquals(dtos, result.getItems());
    }

    @Test
    public void getUnpublishedProducts(@Mocked Product product,
                                       @Mocked ProductDto dto) {
        Iterable<Product> products = Collections.singletonList(product);
        Iterable<ProductDto> dtos = Collections.singletonList(dto);
        new StrictExpectations() {{
            service.getAllUnpublishedItems();
            result = products;

            mapper.map(products);
            result = dtos;
        }};

        ListResponse<ProductDto> result = tested.getUnpublishedProducts();
        Assert.assertEquals(dtos, result.getItems());
    }
}