package com.iyzico.challenge.integrator.data.service;

import com.iyzico.challenge.integrator.data.entity.Basket;
import com.iyzico.challenge.integrator.data.entity.LongText;
import com.iyzico.challenge.integrator.data.entity.PaymentProduct;
import com.iyzico.challenge.integrator.data.entity.User;
import com.iyzico.challenge.integrator.data.entity.UserPayment;
import com.iyzico.challenge.integrator.data.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    private final PaymentRepository repository;
    private final BasketService basketService;
    private final LongTextService longTextService;
    private final UserOrderService userOrderService;

    public PaymentService(PaymentRepository repository,
                          BasketService basketService,
                          LongTextService longTextService,
                          UserOrderService userOrderService) {
        this.repository = repository;
        this.basketService = basketService;
        this.longTextService = longTextService;
        this.userOrderService = userOrderService;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Throwable.class)
    public UserPayment startPayment(User user, Basket basket) {
        UserPayment payment = new UserPayment();
        payment.setBasket(basket);
        payment.setUser(user);
        payment.setAmount(basket.getTotal());
        payment.setStatus(UserPayment.Status.IN_PROGRESS);
        payment.setCreateTime(LocalDateTime.now());
        payment.setProducts(
                basket.getProducts()
                        .stream()
                        .map(x -> {
                            PaymentProduct product = new PaymentProduct();
                            product.setPayment(payment);
                            product.setProduct(x.getProduct());
                            product.setCount(x.getCount());
                            product.setPrice(x.getProduct().getPrice());
                            return product;
                        })
                        .collect(Collectors.toSet()));

        return repository.save(payment);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Throwable.class)
    public void markAsSuccess(User user, UserPayment payment, Basket basket, com.iyzipay.model.Payment response) {
        payment.setStatus(UserPayment.Status.SUCCESS);
        payment.setPaymentGatewayId(response.getPaymentId());
        userOrderService.create(user, payment, basket);
        basketService.complete(basket);
        repository.save(payment);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Throwable.class)
    public void markAsFailure(UserPayment payment, String message) {
        payment.setStatus(UserPayment.Status.ERROR);
        LongText failReason = longTextService.create(
                UserPayment.TABLE_NAME,
                UserPayment.FAIL_REASON_COLUMN_NAME,
                String.valueOf(payment.getId()),
                message);

        // payment.setFailReason(failReason);
        repository.save(payment);
    }
}
