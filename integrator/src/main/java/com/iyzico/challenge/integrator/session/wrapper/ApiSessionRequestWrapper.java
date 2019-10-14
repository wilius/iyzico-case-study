package com.iyzico.challenge.integrator.session.wrapper;

import com.iyzico.challenge.integrator.data.entity.User;
import com.iyzico.challenge.integrator.service.SessionService;
import com.iyzico.challenge.integrator.session.model.ApiSession;
import com.iyzico.challenge.integrator.session.model.UserSession;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Enumeration;

public final class ApiSessionRequestWrapper extends HttpServletRequestWrapper {
    private final static Logger log = LoggerFactory.getLogger(ApiSessionRequestWrapper.class);
    private final SessionService sessionService;
    private final HttpServletRequest request;
    private final HttpServletResponse response;

    private final String sessionKey;

    private ApiSession session;

    public ApiSessionRequestWrapper(SessionService sessionService, HttpServletRequest request, HttpServletResponse response) {
        super(request);

        this.request = request;
        this.response = response;
        this.sessionService = sessionService;
        this.sessionKey = getSessionKey();

        if (sessionKey != null) {
            UserSession userSession = sessionService.getSession(sessionKey);
            if (userSession != null) {
                session = new ApiSession(userSession, response, request.getServletContext(), false);
            }
        }
    }

    @Override
    public String getRequestedSessionId() {
        return sessionKey;
    }

    @Override
    @Deprecated
    public HttpSession getSession(boolean create) {
        if (create) {
            log.warn("Creating a new session with calling getSession function with parameter true is deprecated and no longer supported");
        }

        return session;
    }

    @Override
    public HttpSession getSession() {
        return session;
    }

    public void createSession(User user) {
        session = new ApiSession(sessionService.createNewSession(user), response, request.getServletContext(), true);
    }

    @Override
    public String changeSessionId() {
        if (session == null) {
            throw new IllegalStateException("No session associated with the request");
        }

        session.invalidate();
        createSession(session.getUser());
        return session.getId();
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return sessionKey != null && session != null && session.getId().equals(sessionKey);
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return false;
    }

    private String getSessionKey() {
        String sessionKey = null;

        Enumeration<String> sessionHeaders = request.getHeaders(ApiSession.HEADER_SESSION_KEY);
        while (sessionHeaders.hasMoreElements()) {
            String key = sessionHeaders.nextElement();
            if (!StringUtils.isEmpty(key)) {
                sessionKey = StringUtils.trim(key);
                break;
            }
        }

        if (StringUtils.isEmpty(sessionKey)) {

            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if (!ApiSession.HEADER_SESSION_KEY.equalsIgnoreCase(cookie.getName())) {
                        continue;
                    }

                    if (!StringUtils.isEmpty(cookie.getValue())) {
                        sessionKey = cookie.getValue();
                        break;
                    }
                }
            }
        }

        return sessionKey;
    }
}
