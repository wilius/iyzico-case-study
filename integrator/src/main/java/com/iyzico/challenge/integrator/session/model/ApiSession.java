package com.iyzico.challenge.integrator.session.model;

import com.iyzico.challenge.integrator.data.entity.User;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Enumeration;

public class ApiSession implements HttpSession {
    public final static String HEADER_SESSION_KEY = "Session";
    private final static String HEADER_SESSION_SET = "Set-Session";
    private final static String HEADER_SESSION_DEL = "Del-Session";

    private final static DeprecatedHttpSessionContext sessionContext = new DeprecatedHttpSessionContext();

    private final ServletContext servletContext;
    private final HttpServletResponse response;
    private final UserSession userSession;

    public ApiSession(UserSession userSession, HttpServletResponse response, ServletContext servletContext, boolean setSessionHeader) {
        this.userSession = userSession;
        this.response = response;
        this.servletContext = servletContext;

        if (setSessionHeader) {
            response.addHeader(HEADER_SESSION_SET, userSession.getSessionKey());
            response.addCookie(createSessionCookie());
        }
    }

    @Override
    public long getCreationTime() {
        return userSession.getCreatedDate().atZone(ZoneId.systemDefault()).toEpochSecond();
    }

    @Override
    public String getId() {
        return userSession.getSessionKey();
    }

    public User getUser() {
        return userSession.getUser();
    }

    @Override
    public long getLastAccessedTime() {
        LocalDateTime lastActivity = userSession.getLastLoginDate();

        if (lastActivity == null) {
            return 0;
        }

        return lastActivity.atZone(ZoneId.systemDefault()).toEpochSecond();
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return null;
    }

    @Override
    public Object getAttribute(String name) {
        return userSession.getValue(name);
    }

    @Override
    public void setAttribute(String name, Object value) {
        if (!(value instanceof String)) {
            throw new IllegalArgumentException("The type of a session value must be string.");
        }

        userSession.setValue(name, (String) value);
    }

    @Override
    public void removeAttribute(String name) {
        userSession.deleteValue(name);
    }

    @Override
    public ServletContext getServletContext() {
        return servletContext;
    }

    @Override
    public int getMaxInactiveInterval() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void setMaxInactiveInterval(int interval) {
    }

    @Override
    @Deprecated
    public HttpSessionContext getSessionContext() {
        return sessionContext;
    }

    @Override
    @Deprecated
    public Object getValue(String name) {
        return getAttribute(name);
    }

    @Override
    @Deprecated
    public String[] getValueNames() {
        Enumeration<String> attributeNames = getAttributeNames();

        ArrayList<String> valueNames = new ArrayList<>();

        while (attributeNames.hasMoreElements()) {
            valueNames.add(attributeNames.nextElement());
        }

        return valueNames.toArray(new String[0]);
    }

    @Override
    @Deprecated
    public void putValue(String name, Object value) {
        setAttribute(name, value);
    }

    @Override
    @Deprecated
    public void removeValue(String name) {
        removeAttribute(name);
    }

    @Override
    public void invalidate() {
        response.addHeader(ApiSession.HEADER_SESSION_DEL, userSession.getSessionKey());
        Cookie cookie = createSessionCookie();
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        userSession.invalidate();
    }

    @Override
    public boolean isNew() {
        return userSession.getLastLoginDate() == null;
    }

    public UserSession getUserSession() {
        return userSession;
    }

    private Cookie createSessionCookie() {
        Cookie sessionCookie = new Cookie(HEADER_SESSION_KEY, userSession.getSessionKey());
        sessionCookie.setPath("/");
        return sessionCookie;
    }

    private static class DeprecatedHttpSessionContext implements HttpSessionContext {
        @Override
        public HttpSession getSession(String sessionId) {
            return null;
        }

        @Override
        public Enumeration<String> getIds() {
            return null;
        }
    }
}