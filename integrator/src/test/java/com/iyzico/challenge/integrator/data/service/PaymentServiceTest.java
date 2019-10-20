package com.iyzico.challenge.integrator.data.service;

import com.iyzico.challenge.integrator.data.entity.Basket;
import com.iyzico.challenge.integrator.data.entity.BasketProduct;
import com.iyzico.challenge.integrator.data.entity.PaymentProduct;
import com.iyzico.challenge.integrator.data.entity.Product;
import com.iyzico.challenge.integrator.data.entity.User;
import com.iyzico.challenge.integrator.data.entity.UserPayment;
import com.iyzico.challenge.integrator.data.repository.PaymentRepository;
import mockit.Injectable;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.StrictExpectations;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Set;

@RunWith(JMockit.class)
public class PaymentServiceTest {
    @Tested
    private PaymentService tested;

    @Injectable
    private PaymentRepository repository;

    @Injectable
    private BasketService basketService;

    @Injectable
    private UserOrderService userOrderService;

    @Test
    public void startPayment(@Mocked User user,
                             @Mocked Basket basket,
                             @Mocked Product product) {
        BigDecimal total = BigDecimal.TEN;
        BasketProduct basketProduct = new BasketProduct();
        basketProduct.setProduct(product);
        basketProduct.setCount(5);

        Set<BasketProduct> products = Collections.singleton(basketProduct);
        new NonStrictExpectations() {{
            basket.getTotal();
            result = total;

            product.getPrice();
            result = BigDecimal.ONE;

            basket.getProducts();
            result = products;
        }};

        new StrictExpectations() {{
            repository.save(withInstanceOf(UserPayment.class));
        }};

        UserPayment result = tested.startPayment(user, basket);
        Assert.assertNotNull(result.getAmount());
        Assert.assertEquals(total, result.getAmount());
        Assert.assertEquals(user, result.getUser());
        Assert.assertEquals(basket, result.getBasket());
        Assert.assertEquals(UserPayment.Status.IN_PROGRESS, result.getStatus());
        Assert.assertNotNull(result.getProducts());
        Assert.assertEquals(1, result.getProducts().size());

        PaymentProduct resultProduct = result.getProducts().iterator().next();
        Assert.assertEquals(5, resultProduct.getCount());
        Assert.assertEquals(BigDecimal.ONE, resultProduct.getPrice());
        Assert.assertEquals(product, resultProduct.getProduct());
        Assert.assertEquals(result, resultProduct.getPayment());

    }

    @Test
    public void markAsSuccess(@Mocked User user,
                              @Mocked UserPayment payment,
                              @Mocked Basket basket,
                              @Mocked com.iyzipay.model.Payment response) {

        String paymentId = "paymentId";

        new NonStrictExpectations() {{
            response.getPaymentId();
            result = paymentId;
        }};

        new StrictExpectations() {{
            payment.setStatus(UserPayment.Status.SUCCESS);
            payment.setPaymentGatewayId(paymentId);
            userOrderService.create(user, payment, basket);
            basketService.complete(basket);
            repository.save(payment);
        }};

        tested.markAsSuccess(user, payment, basket, response);
    }

    @Test
    public void markAsFailure() {
        String message = "message";
        long id = 1;
        UserPayment payment = new UserPayment();
        payment.setId(id);
        new StrictExpectations() {{
            repository.save(payment);
        }};
        tested.markAsFailure(payment, message);
        Assert.assertEquals(UserPayment.Status.ERROR, payment.getStatus());
        Assert.assertNotNull(payment.getFailReason());
        Assert.assertNotNull(UserPayment.TABLE_NAME, payment.getFailReason().getTable());
        Assert.assertNotNull(UserPayment.FAIL_REASON_COLUMN_NAME, payment.getFailReason().getColumnName());
        Assert.assertNotNull(String.valueOf(id), payment.getFailReason().getRecordId());
        Assert.assertNotNull(message, payment.getFailReason().getContent());

    }
}