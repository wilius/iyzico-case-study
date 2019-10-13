package com.iyzico.challenge.integrator.dto.payment.validator;

import org.apache.commons.validator.routines.CreditCardValidator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class BankCardValidator implements ConstraintValidator<BankCard, String> {

    private static final CreditCardValidator VALIDATOR;

    static {
        VALIDATOR = new CreditCardValidator(
                CreditCardValidator.VISA +
                        CreditCardValidator.MASTERCARD
        );
    }

    @Override
    public void initialize(BankCard constraintAnnotation) {
    }

    @Override
    public boolean isValid(String cardNumber, ConstraintValidatorContext constraintValidatorContext) {
        return VALIDATOR.isValid(cardNumber);
    }
}
