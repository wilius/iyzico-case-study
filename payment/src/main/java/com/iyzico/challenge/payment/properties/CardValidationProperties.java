package com.iyzico.challenge.payment.properties;

import com.iyzico.challenge.payment.properties.exception.CardNumberPropertyValidationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@ConfigurationProperties(
        prefix = "iyzico.card.validation"
)
@Validated
public class CardValidationProperties implements InitializingBean {
    @Min(12)
    @Max(32)
    private int minimumNumberLength = 15;

    @Min(12)
    @Max(32)
    private int maximumNumberLength = 16;

    @Min(2)
    @Max(6)
    private int mostSignificantUnmaskLength = 6;

    @Min(2)
    @Max(6)
    private int leastSignificantUnmaskLength = 4;

    public int getMinimumNumberLength() {
        return minimumNumberLength;
    }

    public void setMinimumNumberLength(int minimumNumberLength) {
        this.minimumNumberLength = minimumNumberLength;
    }

    public int getMaximumNumberLength() {
        return maximumNumberLength;
    }

    public void setMaximumNumberLength(int maximumNumberLength) {
        this.maximumNumberLength = maximumNumberLength;
    }

    public int getMostSignificantUnmaskLength() {
        return mostSignificantUnmaskLength;
    }

    public void setMostSignificantUnmaskLength(int mostSignificantUnmaskLength) {
        this.mostSignificantUnmaskLength = mostSignificantUnmaskLength;
    }

    public int getLeastSignificantUnmaskLength() {
        return leastSignificantUnmaskLength;
    }

    public void setLeastSignificantUnmaskLength(int leastSignificantUnmaskLength) {
        this.leastSignificantUnmaskLength = leastSignificantUnmaskLength;
    }

    private void validate() {
        if (minimumNumberLength > maximumNumberLength) {
            throw new CardNumberPropertyValidationException("Minimum length of card digits should be less than maximum number of it");
        }
    }

    @Override
    public void afterPropertiesSet() {
        validate();
    }
}
