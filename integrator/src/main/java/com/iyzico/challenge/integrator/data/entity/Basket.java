package com.iyzico.challenge.integrator.data.entity;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "basket", indexes = {
        @Index(columnList = "user_id,status", name = "idx_basket___user_id__status")
})
public class Basket {
    private long id;
    private long userId;
    private Status status;

    private User user;
    private Set<BasketProduct> products = new HashSet<>();

    public enum Status {
        ACTIVE, TIMEOUT, COMPLETED
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
    @Column(name = "user_id", insertable = false, updatable = false)
    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    @Basic
    @Column(name = "status", length = 16)
    @Enumerated(EnumType.STRING)
    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @ManyToOne(targetEntity = User.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @OneToMany(targetEntity = BasketProduct.class, fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "basket", orphanRemoval = true)
    public Set<BasketProduct> getProducts() {
        return products;
    }

    public void setProducts(Set<BasketProduct> products) {
        this.products = products;
    }

    @Transient
    public BigDecimal getTotal() {
        return products.stream()
                .map(BasketProduct::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
