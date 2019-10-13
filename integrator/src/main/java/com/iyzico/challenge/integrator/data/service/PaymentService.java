package com.iyzico.challenge.integrator.data.service;

import com.iyzico.challenge.integrator.data.entity.Basket;
import com.iyzico.challenge.integrator.data.entity.Payment;
import com.iyzico.challenge.integrator.data.entity.PaymentProduct;
import com.iyzico.challenge.integrator.data.entity.User;
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

    public PaymentService(PaymentRepository repository,
                          BasketService basketService) {
        this.repository = repository;
        this.basketService = basketService;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Throwable.class)
    public Payment startPayment(User user, Basket basket) {
        Payment payment = new Payment();
        payment.setBasket(basket);
        payment.setUser(user);
        payment.setAmount(basket.getTotal());
        payment.setStatus(Payment.Status.IN_PROGRESS);
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
    public void markAsSuccess(Payment payment, Basket basket) {
        payment.setStatus(Payment.Status.SUCCESS);
        basketService.complete(basket);
        repository.save(payment);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Throwable.class)
    public void markAsFailure(Payment payment) {
        payment.setStatus(Payment.Status.ERROR);
        repository.save(payment);
    }
}
