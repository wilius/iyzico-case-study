package com.iyzico.challenge.integrator.dto.payment;

import java.math.BigDecimal;
import java.util.List;

public class InstallmentDto {
    private String cardType;
    private String cardAssociation;
    private Long bankCode;
    private String bankName;
    private boolean force3ds;
    private boolean forceCvc;
    private boolean commercial;

    private List<InstallmentPrice> prices;

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public String getCardAssociation() {
        return cardAssociation;
    }

    public void setCardAssociation(String cardAssociation) {
        this.cardAssociation = cardAssociation;
    }

    public Long getBankCode() {
        return bankCode;
    }

    public void setBankCode(Long bankCode) {
        this.bankCode = bankCode;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public boolean isForce3ds() {
        return force3ds;
    }

    public void setForce3ds(boolean force3ds) {
        this.force3ds = force3ds;
    }

    public boolean isForceCvc() {
        return forceCvc;
    }

    public void setForceCvc(boolean forceCvc) {
        this.forceCvc = forceCvc;
    }

    public boolean isCommercial() {
        return commercial;
    }

    public void setCommercial(boolean commercial) {
        this.commercial = commercial;
    }

    public List<InstallmentPrice> getPrices() {
        return prices;
    }

    public void setPrices(List<InstallmentPrice> prices) {
        this.prices = prices;
    }

    public static class InstallmentPrice {
        private BigDecimal installment;
        private BigDecimal total;
        private int count;

        public BigDecimal getInstallment() {
            return installment;
        }

        public void setInstallment(BigDecimal installment) {
            this.installment = installment;
        }

        public BigDecimal getTotal() {
            return total;
        }

        public void setTotal(BigDecimal total) {
            this.total = total;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }
}
