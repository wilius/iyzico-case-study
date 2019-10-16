package com.iyzico.challenge.integrator.session.annotation;

import com.iyzico.challenge.integrator.session.SecurityInterceptor;
import com.iyzico.challenge.integrator.session.model.ApiSession;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class UserArgumentResolver implements HandlerMethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        IntegratorSession session = parameter.getParameterAnnotation(IntegratorSession.class);
        return session != null && ApiSession.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        IntegratorSession session = parameter.getParameterAnnotation(IntegratorSession.class);

        if (session == null) {
            throw new IllegalStateException("Missing annotation.");
        }

        return webRequest.getAttribute(SecurityInterceptor.SESSION, RequestAttributes.SCOPE_REQUEST);
    }
}
