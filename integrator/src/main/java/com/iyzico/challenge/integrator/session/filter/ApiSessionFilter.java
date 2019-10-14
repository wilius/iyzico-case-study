package com.iyzico.challenge.integrator.session.filter;

import com.iyzico.challenge.integrator.service.SessionService;
import com.iyzico.challenge.integrator.session.wrapper.ApiSessionRequestWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class ApiSessionFilter extends GenericFilterBean {
    private final static Logger log = LoggerFactory.getLogger(ApiSessionFilter.class);

    private final SessionService sessionService;

    @Autowired
    public ApiSessionFilter(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse httpRes = (HttpServletResponse) response;
        ServletRequest finalRequest = new ApiSessionRequestWrapper(sessionService, (HttpServletRequest) request, httpRes);
        chain.doFilter(finalRequest, response);
    }
}