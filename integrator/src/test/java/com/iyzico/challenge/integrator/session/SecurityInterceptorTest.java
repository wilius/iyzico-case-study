package com.iyzico.challenge.integrator.session;

import com.iyzico.challenge.integrator.controller.ProductController;
import com.iyzico.challenge.integrator.controller.management.ManageProductController;
import com.iyzico.challenge.integrator.data.entity.User;
import com.iyzico.challenge.integrator.exception.auth.AuthorizationException;
import com.iyzico.challenge.integrator.session.model.ApiSession;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.StrictExpectations;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;

@RunWith(JMockit.class)
public class SecurityInterceptorTest {
    @Tested
    private SecurityInterceptor tested;

    @Test
    public void preHandle_NotSecured(@Mocked HttpServletRequest request,
                                     @Mocked HttpServletResponse response,
                                     @Mocked HandlerMethod handlerMethod) {


        new NonStrictExpectations() {{
            handlerMethod.getMethodAnnotation(SecuredEndpoint.class);
            result = null;

            handlerMethod.getBeanType();
            result = Object.class;
        }};

        boolean result = tested.preHandle(request, response, handlerMethod);
        Assert.assertTrue(result);
    }

    @Test(expected = AuthorizationException.class)
    public void preHandle_NotAllowAnonymous(@Mocked HttpServletRequest request,
                                            @Mocked HttpServletResponse response,
                                            @Mocked HandlerMethod handlerMethod) {


        new NonStrictExpectations() {{
            handlerMethod.getMethodAnnotation(SecuredEndpoint.class);
            result = null;

            handlerMethod.getBeanType();
            result = ManageProductController.class;

            request.getSession(false);
            result = null;
        }};

        tested.preHandle(request, response, handlerMethod);
    }

    @Test
    public void preHandle_AllowAnonymous(@Mocked HttpServletRequest request,
                                         @Mocked HttpServletResponse response,
                                         @Mocked HandlerMethod handlerMethod) {

        SecuredEndpoint secured = new SecuredEndpoint() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return null;
            }

            @Override
            public boolean isAllowAnonymous() {
                return true;
            }

            @Override
            public boolean requireAdminPermission() {
                return false;
            }
        };

        new NonStrictExpectations() {{
            handlerMethod.getMethodAnnotation(SecuredEndpoint.class);
            result = secured;

            handlerMethod.getBeanType();
            result = Object.class;

            request.getSession(false);
            result = null;
        }};

        boolean result = tested.preHandle(request, response, handlerMethod);
        Assert.assertTrue(result);
    }

    @Test(expected = AuthorizationException.class)
    public void preHandle_RequireAdminPermission(@Mocked HttpServletRequest request,
                                                 @Mocked HttpServletResponse response,
                                                 @Mocked HandlerMethod handlerMethod,
                                                 @Mocked ApiSession session,
                                                 @Mocked User user) {


        new NonStrictExpectations() {{
            handlerMethod.getMethodAnnotation(SecuredEndpoint.class);
            result = null;

            handlerMethod.getBeanType();
            result = ManageProductController.class;

            request.getSession(false);
            result = session;

            session.getUser();
            result = user;

            user.isAdmin();
            result = false;
        }};

        tested.preHandle(request, response, handlerMethod);
    }

    @Test
    public void preHandle_NotRequireAdminPermission(@Mocked HttpServletRequest request,
                                                    @Mocked HttpServletResponse response,
                                                    @Mocked HandlerMethod handlerMethod,
                                                    @Mocked ApiSession session,
                                                    @Mocked User user) {


        new NonStrictExpectations() {{
            handlerMethod.getMethodAnnotation(SecuredEndpoint.class);
            result = null;

            handlerMethod.getBeanType();
            result = ProductController.class;

            request.getSession(false);
            result = session;

            session.getUser();
            result = user;

            user.isAdmin();
            result = false;
        }};

        new StrictExpectations() {{
            request.setAttribute(SecurityInterceptor.SESSION, session);
        }};

        boolean result = tested.preHandle(request, response, handlerMethod);
        Assert.assertTrue(result);
    }

    @Test
    public void preHandle_RequiresAdminPermissionAndUserIsAdmin(@Mocked HttpServletRequest request,
                                                                @Mocked HttpServletResponse response,
                                                                @Mocked HandlerMethod handlerMethod,
                                                                @Mocked ApiSession session,
                                                                @Mocked User user) {


        new NonStrictExpectations() {{
            handlerMethod.getMethodAnnotation(SecuredEndpoint.class);
            result = null;

            handlerMethod.getBeanType();
            result = ProductController.class;

            request.getSession(false);
            result = session;

            session.getUser();
            result = user;

            user.isAdmin();
            result = true;
        }};

        new StrictExpectations() {{
            request.setAttribute(SecurityInterceptor.SESSION, session);
        }};

        boolean result = tested.preHandle(request, response, handlerMethod);
        Assert.assertTrue(result);
    }
}