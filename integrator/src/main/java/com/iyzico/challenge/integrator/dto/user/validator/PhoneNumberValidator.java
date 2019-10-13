package com.iyzico.challenge.integrator.dto.user.validator;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PhoneNumberValidator implements ConstraintValidator<PhoneNumber, String> {
    @Override
    public void initialize(PhoneNumber constraintAnnotation) {
    }

    @Override
    public boolean isValid(String phoneNumber, ConstraintValidatorContext constraintValidatorContext) {
        PhoneNumberUtil instance = PhoneNumberUtil.getInstance();
        Phonenumber.PhoneNumber parsed;
        try {
            parsed = instance.parse(phoneNumber, "TR");
        } catch (NumberParseException e) {
            throw new InvalidPhoneNumberException(e);
        }

        return instance.isValidNumber(parsed);
    }

    public static class InvalidPhoneNumberException extends RuntimeException {
        public InvalidPhoneNumberException(Throwable cause) {
            super(cause);
        }
    }
}
