package com.iyzico.challenge.payment.service;

import com.iyzico.challenge.payment.properties.CardValidationProperties;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class CardMaskingServiceTest {

    @Before
    public void setUp() {
    }

    @Test
    public void null_card_validation() {
        CardValidationProperties properties = new CardValidationProperties();
        CardMaskingService cardMaskingService = new CardMaskingService(properties);

        String maskedCardNumber = cardMaskingService.maskCardNumber(null);
        assertThat(maskedCardNumber).isNull();
    }

    @Test
    public void empty_card_validation() {
        CardValidationProperties properties = new CardValidationProperties();
        CardMaskingService cardMaskingService = new CardMaskingService(properties);

        String maskedCardNumber = cardMaskingService.maskCardNumber("");
        assertThat(maskedCardNumber).isEmpty();
    }

    @Test
    public void should_mask_digits_for_basic_credit_cards() {
        CardValidationProperties properties = new CardValidationProperties();
        CardMaskingService cardMaskingService = new CardMaskingService(properties);

        String cardNumber = "4729150000000005";
        String maskedCardNumber = cardMaskingService.maskCardNumber(cardNumber);
        assertThat(maskedCardNumber).isEqualTo("472915******0005");
    }

    @Test
    public void should_mask_digits_for_credit_cards_in_different_format() {
        CardValidationProperties properties = new CardValidationProperties();
        CardMaskingService cardMaskingService = new CardMaskingService(properties);

        String cardNumber = "4729-1500-0000-0005";
        String maskedCardNumber = cardMaskingService.maskCardNumber(cardNumber);
        assertThat(maskedCardNumber).isEqualTo("4729-15**-****-0005");
    }

    @Test
    public void should_not_mask_anything_for_non_numeric_characters() {
        CardValidationProperties properties = new CardValidationProperties();
        CardMaskingService cardMaskingService = new CardMaskingService(properties);

        String cardNumber = "John Doe";
        String maskedCardNumber = cardMaskingService.maskCardNumber(cardNumber);
        assertThat(maskedCardNumber).isEqualTo(cardNumber);
    }

    @Test
    public void no_enough_digit_to_mask() {
        CardValidationProperties properties = new CardValidationProperties();
        CardMaskingService cardMaskingService = new CardMaskingService(properties);

        String cardNumber = "72915000000005";
        String maskedCardNumber = cardMaskingService.maskCardNumber(cardNumber);
        assertThat(maskedCardNumber).isEqualTo(cardNumber);
    }

    @Test
    public void no_enough_numeric_digit_to_mask_with_non_numeric_digits() {
        CardValidationProperties properties = new CardValidationProperties();
        CardMaskingService cardMaskingService = new CardMaskingService(properties);

        String cardNumber = "7291500-test-000005";
        String maskedCardNumber = cardMaskingService.maskCardNumber(cardNumber);
        assertThat(maskedCardNumber).isEqualTo(cardNumber);
    }

    @Test
    public void over_maximum_allowed_numeric_digits() {
        CardValidationProperties properties = new CardValidationProperties();
        CardMaskingService cardMaskingService = new CardMaskingService(properties);

        String cardNumber = "14729150000000005";
        String maskedCardNumber = cardMaskingService.maskCardNumber(cardNumber);
        assertThat(maskedCardNumber).isEqualTo(cardNumber);
    }

    @Test
    public void over_maximum_allowed_numeric_digits_with_non_numeric_digits() {
        CardValidationProperties properties = new CardValidationProperties();
        CardMaskingService cardMaskingService = new CardMaskingService(properties);

        String cardNumber = "1472-9150-0000-test-00005";
        String maskedCardNumber = cardMaskingService.maskCardNumber(cardNumber);
        assertThat(maskedCardNumber).isEqualTo(cardNumber);
    }
}
