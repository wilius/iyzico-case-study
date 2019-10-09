package com.iyzico.challenge.integrator.data.entity;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static com.iyzico.challenge.integrator.util.Constant.DB_PRECISION;
import static com.iyzico.challenge.integrator.util.Constant.DB_SCALE;

@Entity
@Table(name = "user_order", indexes = {
        @Index(columnList = "user_id,create_time desc", name = "idx_user_order___user_id__create_time")
})
public class UserOrder {
    private long id;
    private long userId;
    private long paymentId;
    private long basketId;
    private BigDecimal total;
    private LocalDateTime createTime;

    private User user;
    private Payment payment;
    private Basket basket;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Basic
    @Column(name = "user_id", insertable = false, updatable = false)
    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    @Basic
    @Column(name = "payment_id", insertable = false, updatable = false)
    public long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(long paymentId) {
        this.paymentId = paymentId;
    }

    @Basic
    @Column(name = "basket_id", insertable = false, updatable = false)
    public long getBasketId() {
        return basketId;
    }

    public void setBasketId(long basketId) {
        this.basketId = basketId;
    }

    @Basic
    @Column(name = "total", precision = DB_PRECISION, scale = DB_SCALE)
    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    @Basic
    @Column(name = "create_time")
    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    @ManyToOne(targetEntity = User.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @OneToOne(targetEntity = Payment.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", referencedColumnName = "id")
    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    @OneToOne(targetEntity = Basket.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "basket_id", referencedColumnName = "id")
    public Basket getBasket() {
        return basket;
    }

    public void setBasket(Basket basket) {
        this.basket = basket;
    }
}
