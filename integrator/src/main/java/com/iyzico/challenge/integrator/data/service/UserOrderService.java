package com.iyzico.challenge.integrator.data.service;

import com.iyzico.challenge.integrator.data.entity.Basket;
import com.iyzico.challenge.integrator.data.entity.UserPayment;
import com.iyzico.challenge.integrator.data.entity.User;
import com.iyzico.challenge.integrator.data.entity.UserOrder;
import com.iyzico.challenge.integrator.data.repository.UserOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UserOrderService {
    private final UserOrderRepository repository;

    public UserOrderService(UserOrderRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.MANDATORY, rollbackFor = Throwable.class)
    public UserOrder create(User user, UserPayment payment, Basket basket) {
        UserOrder order = new UserOrder();
        order.setBasket(basket);
        order.setPayment(payment);
        order.setUser(user);
        order.setCreateTime(LocalDateTime.now());
        repository.save(order);
        return order;
    }
}
