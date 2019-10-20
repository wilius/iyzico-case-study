package com.iyzico.challenge.integrator.data.entity;

import org.hibernate.annotations.Where;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.iyzico.challenge.integrator.util.Constant.DB_PRECISION;
import static com.iyzico.challenge.integrator.util.Constant.DB_SCALE;

@Entity
@Table(name = UserPayment.TABLE_NAME)
public class UserPayment {
    public static final String TABLE_NAME = "payment";
    public static final String FAIL_REASON_COLUMN_NAME = "fail_reason";

    private long id;
    private long userId;
    private long basketId;
    private Status status;
    private BigDecimal amount;
    private LocalDateTime createTime;
    private String paymentGatewayId;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = LongText.class)
    @MapKey(name = "columnName")
    @JoinColumn(name = "record_id", foreignKey = @ForeignKey(name = "none"))
    @Where(clause = "table_name = '" + TABLE_NAME + "'")
    private Map<String, LongText> longTexts = new HashMap<>();

    private User user;
    private Basket basket;
    private Set<PaymentProduct> products;

    public enum Status {
        IN_PROGRESS, ERROR, SUCCESS
    }

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
    @Column(name = "user_id", insertable = false, updatable = false, nullable = false)
    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    @Basic
    @Column(name = "basket_id", insertable = false, updatable = false, nullable = false)
    public long getBasketId() {
        return basketId;
    }

    public void setBasketId(long basketId) {
        this.basketId = basketId;
    }

    @Basic
    @Column(name = "status", length = 16, nullable = false)
    @Enumerated(EnumType.STRING)
    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Basic
    @Column(name = "amount", precision = DB_PRECISION, scale = DB_SCALE, nullable = false)
    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    @Basic
    @Column(name = "create_time", nullable = false)
    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    @Basic
    @Column(name = "payment_gateway_id", length = 128)
    public String getPaymentGatewayId() {
        return paymentGatewayId;
    }

    public void setPaymentGatewayId(String paymentGatewayId) {
        this.paymentGatewayId = paymentGatewayId;
    }

    @ManyToOne(targetEntity = User.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @ManyToOne(targetEntity = Basket.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "basket_id", referencedColumnName = "id")
    public Basket getBasket() {
        return basket;
    }

    public void setBasket(Basket basket) {
        this.basket = basket;
    }

    @OneToMany(targetEntity = PaymentProduct.class, fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "payment", orphanRemoval = true)
    public Set<PaymentProduct> getProducts() {
        return products;
    }

    public void setProducts(Set<PaymentProduct> products) {
        this.products = products;
    }

    @Transient
    public LongText getFailReason() {
        return longTexts.get(FAIL_REASON_COLUMN_NAME);
    }

    public void setFailReason(LongText failReason) {
        longTexts.put(FAIL_REASON_COLUMN_NAME, failReason);
    }
}
