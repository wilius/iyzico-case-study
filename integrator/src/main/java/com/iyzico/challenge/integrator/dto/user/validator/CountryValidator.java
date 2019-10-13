package com.iyzico.challenge.integrator.dto.user.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Locale;

public class CountryValidator implements ConstraintValidator<Country, String> {
    @Override
    public void initialize(Country constraintAnnotation) {
    }

    @Override
    public boolean isValid(String countryCode, ConstraintValidatorContext constraintValidatorContext) {
        for (String isoCountry : Locale.getISOCountries()) {
            if (isoCountry.equalsIgnoreCase(countryCode)) {
                return true;
            }
        }
        return false;
    }
}
