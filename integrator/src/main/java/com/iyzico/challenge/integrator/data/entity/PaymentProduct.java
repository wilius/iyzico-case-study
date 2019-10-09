package com.iyzico.challenge.integrator.data.entity;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.math.BigDecimal;

import static com.iyzico.challenge.integrator.util.Constant.DB_PRECISION;
import static com.iyzico.challenge.integrator.util.Constant.DB_SCALE;

@Entity
@Table(name = "payment_product")
public class PaymentProduct {
    private long id;
    private long paymentId;
    private long productId;
    private BigDecimal total;

    private Payment payment;
    private Product product;

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
    @Column(name = "payment_id", insertable = false, updatable = false)
    public long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(long paymentId) {
        this.paymentId = paymentId;
    }

    @Basic
    @Column(name = "product_id", insertable = false, updatable = false)
    public long getProductId() {
        return productId;
    }

    public void setProductId(long productId) {
        this.productId = productId;
    }

    @Basic
    @Column(name = "total", precision = DB_PRECISION, scale = DB_SCALE)
    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    @ManyToOne(targetEntity = Payment.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", referencedColumnName = "id")
    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    @ManyToOne(targetEntity = Product.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", referencedColumnName = "id")
    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
}
