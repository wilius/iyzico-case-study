package com.iyzico.challenge.integrator.service;

import com.iyzico.challenge.integrator.data.entity.User;
import com.iyzico.challenge.integrator.data.service.UserService;
import com.iyzico.challenge.integrator.exception.UserNotFoundException;
import com.iyzico.challenge.integrator.session.model.UserSession;
import com.iyzico.challenge.integrator.util.IntegrationStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class SessionService {
    private static final Logger log = LoggerFactory.getLogger(SessionService.class);
    private static final byte[] USER_ID_KEY = "__userId".getBytes(StandardCharsets.UTF_8);
    private static final byte[] CREATED_KEY = "__created".getBytes(StandardCharsets.UTF_8);
    private static final byte[] LAST_LOGIN_KEY = "__last".getBytes(StandardCharsets.UTF_8);

    private static final Period sessionPeriod = Period.parse("P1D");
    private static final long sessionTTL = getTtlSeconds();
    private final RedisTemplate<String, String> redis;
    private final UserService userService;

    @Autowired
    public SessionService(RedisTemplate<String, String> redis,
                          UserService userService) {
        this.redis = redis;
        this.userService = userService;
    }

    private static long getTtlSeconds() {
        LocalDateTime now = LocalDateTime.now();
        return now.until(sessionPeriod.addTo(now), ChronoUnit.SECONDS);
    }

    public UserSession getSession(String sessionKey) {
        final String redisSessionKey = key(sessionKey);
        log.trace("Getting session from redis with key : {}", redisSessionKey);

        final byte[] rskBuf = redisSessionKey.getBytes(StandardCharsets.UTF_8);
        List<Object> redisResult = redis.executePipelined((RedisCallback<Object>) connection -> {
            connection.hGet(rskBuf, USER_ID_KEY);
            connection.hGet(rskBuf, CREATED_KEY);
            connection.hGet(rskBuf, LAST_LOGIN_KEY);
            connection.hSet(rskBuf, LAST_LOGIN_KEY, LocalDateTime.now().toString().getBytes(StandardCharsets.UTF_8));
            connection.expire(rskBuf, getTtlSeconds());
            return null;
        });

        if (redisResult.get(0) == null) {
            log.trace("Session with key '{}' not found", redisSessionKey);
            return null;
        }

        log.trace("Redis result for key : {}, {}", redisSessionKey, redisResult);
        String userIdAsString = (String) redisResult.get(0);
        int userId;

        try {
            userId = Integer.parseInt(userIdAsString);
        } catch (Exception e) {
            log.warn("Unexpected exception during the parse of userId {}", userIdAsString, e);
            return null;
        }

        LocalDateTime createdAt = LocalDateTime.parse((String) redisResult.get(1));
        LocalDateTime lastLogin = LocalDateTime.parse((String) redisResult.get(2));
        return createUserSession(sessionKey, userId, createdAt, lastLogin);
    }

    public UserSession createNewSession(User user) {
        String redisSessionKey;
        String sessionKey;
        do {
            sessionKey = IntegrationStringUtils.generate(32);
            redisSessionKey = key(sessionKey);
        } while (redis.hasKey(redisSessionKey));

        log.debug("redisSessionKey created as {} ", redisSessionKey);

        long userId = user.getId();
        LocalDateTime createdAt = LocalDateTime.now();
        UserSession session = createUserSession(sessionKey, userId, createdAt, createdAt);
        String lastSessionKey = user.getLastSessionKey();
        userService.markAsLoggedIn(user, sessionKey);

        byte[] keyAsBytes = redisSessionKey.getBytes(StandardCharsets.UTF_8);
        byte[] createdAtBytes = createdAt.toString().getBytes(StandardCharsets.UTF_8);
        byte[] userIdBytes = Long.toString(userId).getBytes(StandardCharsets.UTF_8);
        byte[] delAsBytes = !StringUtils.isEmpty(lastSessionKey) ? key(lastSessionKey).getBytes(StandardCharsets.UTF_8) : null;

        redis.executePipelined((RedisCallback<Object>) connection -> {
            connection.hSet(keyAsBytes, USER_ID_KEY, userIdBytes);
            connection.hSet(keyAsBytes, CREATED_KEY, createdAtBytes);
            connection.hSet(keyAsBytes, LAST_LOGIN_KEY, createdAtBytes);
            connection.expire(keyAsBytes, sessionTTL);
            if (delAsBytes != null) {
                connection.del(delAsBytes);
            }

            return null;
        });

        return session;
    }

    private UserSession createUserSession(String key, long userId, LocalDateTime createdAt, LocalDateTime lastLogin) {
        try {
            return new UserSession(this, key, userService.getById(userId), createdAt, lastLogin);
        } catch (UserNotFoundException e) {
            deleteSession(key);
            return null;
        }
    }

    public void setSessionValue(String sessionKey, String key, String value) {
        redis.opsForHash().put(key(sessionKey), key, value);
    }

    public String getSessionValue(String sessionKey, String key) {
        return (String) redis.opsForHash().get(key(sessionKey), key);
    }

    public void deleteSession(String sessionKey) {
        if (!StringUtils.isEmpty(sessionKey)) {
            log.trace("Deleting session from redis. Key : {}", sessionKey);
            redis.delete(key(sessionKey));
        }
    }

    public void deleteSessionValue(String sessionKey, String key) {
        redis.opsForHash().delete(key(sessionKey), key);
    }

    private String key(String key) {
        return String.format("S:%s", key);
    }
}
