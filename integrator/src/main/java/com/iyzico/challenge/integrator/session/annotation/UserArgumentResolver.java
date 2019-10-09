package com.iyzico.challenge.integrator.session.annotation;

import com.iyzico.challenge.integrator.session.model.UserSession;
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
        return session != null && UserSession.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        IntegratorSession session = parameter.getParameterAnnotation(IntegratorSession.class);

        if (session == null) {
            throw new IllegalStateException("Missing BirdUser annotation.");
        }

        return webRequest.getAttribute(session.value(), RequestAttributes.SCOPE_REQUEST);
    }
}
