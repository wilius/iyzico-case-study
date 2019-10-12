package com.iyzico.challenge.integrator.data.entity;

import org.hibernate.annotations.Where;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.math.BigDecimal;

import static com.iyzico.challenge.integrator.util.Constant.DB_PRECISION;
import static com.iyzico.challenge.integrator.util.Constant.DB_SCALE;

@Entity
@Table(name = Product.TABLE_NAME, indexes = {
        @Index(columnList = "status", name = "idx_product___status")
})
public class Product {
    public static final String TABLE_NAME = "product";
    public static final String DESCRIPTION_COLUMN_NAME = "description";
    private long id;
    private String name;
    private long userId;
    private long stockCount;
    private long awaitingDeliveryCount;
    private Status status;
    private BigDecimal price;
    private String barcode;
    private LongText description;

    private User user;

    public enum Status {
        IN_STOCK, OUT_OF_STOCK, UNPUBLISHED
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
    @Column(name = "name", length = 512)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
    @Column(name = "stock_count")
    public long getStockCount() {
        return stockCount;
    }

    public void setStockCount(long stockCount) {
        this.stockCount = stockCount;
    }

    @Basic
    @Column(name = "awaiting_delivery_count")
    public long getAwaitingDeliveryCount() {
        return awaitingDeliveryCount;
    }

    public void setAwaitingDeliveryCount(long awaitingDeliveryCount) {
        this.awaitingDeliveryCount = awaitingDeliveryCount;
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

    @Basic
    @Column(name = "price", precision = DB_PRECISION, scale = DB_SCALE)
    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    @Basic
    @Column(name = "barcode", length = 32)
    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getBarcode() {
        return barcode;
    }


    @ManyToOne(targetEntity = User.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @OneToOne(targetEntity = LongText.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "id", referencedColumnName = "recordId", foreignKey = @ForeignKey(name = "none"))
    @Where(clause = " owner_type = 'PRODUCT' and column = 'DESCRIPTION' ")
    public LongText getDescription() {
        return description;
    }

    public void setDescription(LongText description) {
        this.description = description;
    }

    @Transient
    public boolean hasItemToSell() {
        return stockCount - awaitingDeliveryCount > 0;
    }

}
