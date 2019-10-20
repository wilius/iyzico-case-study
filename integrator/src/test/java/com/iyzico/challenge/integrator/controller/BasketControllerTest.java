package com.iyzico.challenge.integrator.controller;

import com.iyzico.challenge.integrator.data.entity.Basket;
import com.iyzico.challenge.integrator.data.entity.User;
import com.iyzico.challenge.integrator.data.service.BasketService;
import com.iyzico.challenge.integrator.dto.basket.AddProductToTheBasketRequest;
import com.iyzico.challenge.integrator.dto.basket.BasketDto;
import com.iyzico.challenge.integrator.mapper.BasketMapper;
import com.iyzico.challenge.integrator.session.model.ApiSession;
import mockit.StrictExpectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.StrictExpectations;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMockit.class)
public class BasketControllerTest {
    @Tested
    private BasketController tested;

    @Injectable
    private BasketService service;

    @Injectable
    private BasketMapper mapper;

    @Test
    public void add(@Mocked ApiSession session,
                    @Mocked User user) {

        long productId = 1L;
        int count = 5;
        AddProductToTheBasketRequest request = new AddProductToTheBasketRequest();
        request.setProductId(productId);
        request.setCount(count);

        new StrictExpectations() {{
            session.getUser();
            result = user;

            service.addItem(user, productId, count);
        }};

        tested.add(request, session);
    }

    @Test
    public void delete(@Mocked ApiSession session,
                       @Mocked User user) {
        long productId = 1L;

        new StrictExpectations() {{
            session.getUser();
            result = user;

            service.deleteItem(user, productId);
        }};

        tested.delete(productId, session);

    }

    @Test
    public void get(@Mocked ApiSession session,
                    @Mocked User user,
                    @Mocked Basket basket,
                    @Mocked BasketDto dto) {

        new StrictExpectations() {{
            session.getUser();
            result = user;

            service.getUserBasket(user);
            result = basket;

            mapper.map(basket);
            result = dto;
        }};


        BasketDto result = tested.get(session);
        Assert.assertEquals(dto, result);
    }
}