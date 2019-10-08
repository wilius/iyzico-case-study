package com.iyzico.challenge.payment.service;

import com.iyzico.challenge.payment.properties.CardValidationProperties;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CardMaskingService {
    private final Logger logger = LoggerFactory.getLogger(CardMaskingService.class);
    private final CardValidationProperties properties;

    public CardMaskingService(CardValidationProperties properties) {
        this.properties = properties;
    }

    public String maskCardNumber(String cardNumber) {
        logger.trace("Starting to mask '{}' as card number", cardNumber);

        if (StringUtils.isEmpty(cardNumber)) {
            logger.trace("Card number candidate '{}' is empty", cardNumber);
            return cardNumber;
        }

        int length = cardNumber.length();
        int digitCount = 0;
        for (int i = 0; i < length; i++) {
            if (isNumeric(cardNumber.charAt(i))) {
                digitCount++;
            }
        }

        logger.trace("Total numeric digit count for '{}' is {}}", cardNumber, digitCount);
        if (digitCount < properties.getMinimumNumberLength() ||
                digitCount > properties.getMaximumNumberLength()) {
            logger.trace("Value '{}' does not have enough numeric digit to mask. skipping..", cardNumber);
            return cardNumber;
        }

        int lowerBound = properties.getMostSignificantUnmaskLength();
        int upperBound = digitCount - properties.getLeastSignificantUnmaskLength() + 1;
        StringBuilder masked = new StringBuilder();
        for (int i = 0, digitIndex = 0; i < length; i++) {
            char item = cardNumber.charAt(i);
            if (!isNumeric(item)) {
                masked.append(item);
                continue;
            }

            digitIndex++;

            if (digitIndex > lowerBound && digitIndex < upperBound) {
                masked.append("*");
            } else {
                masked.append(item);
            }
        }

        return masked.toString();
    }

    private boolean isNumeric(char c) {
        return c >= '0' && c <= '9';
    }
}
