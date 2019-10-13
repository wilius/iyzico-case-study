package com.iyzico.challenge.integrator.dto.payment.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class CVCValidator implements ConstraintValidator<CVC, String> {

    private static final Pattern pattern;

    static {
        pattern = Pattern.compile("^\\d{3}$");
    }

    @Override
    public void initialize(CVC constraintAnnotation) {
    }

    @Override
    public boolean isValid(String cvc, ConstraintValidatorContext constraintValidatorContext) {
        return pattern.matcher(cvc).matches();
    }
}
