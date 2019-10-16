package com.iyzico.challenge.integrator.session;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SecuredEndpoint {
    boolean isAllowAnonymous() default false;

    boolean requireAdminPermission() default false;
}
