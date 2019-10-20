package com.iyzico.challenge.integrator.data.entity;

import mockit.StrictExpectations;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;

@RunWith(JMockit.class)
public class UserPaymentTest {
    @Tested
    private UserPayment tested;

    @Test
    public void mapping_test() {
        long id = 1;
        long userId = 2;
        long basketId = 3;
        UserPayment.Status status = UserPayment.Status.ERROR;
        BigDecimal amount = BigDecimal.TEN;
        LocalDateTime createTime = LocalDateTime.now();
        String paymentGatewayId = "paymentGatewayId";
        User user = new User();
        Basket basket = new Basket();
        LongText failReason = new LongText();
        Set<PaymentProduct> products = Collections.unmodifiableSet(Collections.emptySet());

        new StrictExpectations() {{
        }};

        tested.setId(id);
        tested.setAmount(amount);
        tested.setBasket(basket);
        tested.setBasketId(basketId);
        tested.setCreateTime(createTime);
        tested.setFailReason(failReason);
        tested.setPaymentGatewayId(paymentGatewayId);
        tested.setProducts(products);
        tested.setStatus(status);
        tested.setUser(user);
        tested.setUserId(userId);

        Assert.assertEquals(id, tested.getId());
        Assert.assertEquals(amount, tested.getAmount());
        Assert.assertEquals(basket, tested.getBasket());
        Assert.assertEquals(basketId, tested.getBasketId());
        Assert.assertEquals(createTime, tested.getCreateTime());
        Assert.assertEquals(failReason, tested.getFailReason());
        Assert.assertEquals(paymentGatewayId, tested.getPaymentGatewayId());
        Assert.assertEquals(products, tested.getProducts());
        Assert.assertEquals(status, tested.getStatus());
        Assert.assertEquals(user, tested.getUser());
        Assert.assertEquals(userId, tested.getUserId());
    }
}