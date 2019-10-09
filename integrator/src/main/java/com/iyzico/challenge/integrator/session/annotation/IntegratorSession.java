package com.iyzico.challenge.integrator.session.annotation;

import com.iyzico.challenge.integrator.session.SecurityInterceptor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface IntegratorSession {
    String DEFAULT_VALUE = SecurityInterceptor.HOLIDEA_SESSION;

    String value() default DEFAULT_VALUE;
}
