package com.iyzico.challenge.integrator.service;

import com.iyzico.challenge.integrator.data.entity.User;
import com.iyzico.challenge.integrator.data.service.UserService;
import com.iyzico.challenge.integrator.exception.CannotCreateSessionException;
import com.iyzico.challenge.integrator.session.model.UserSession;
import com.iyzico.challenge.integrator.util.IntegrationStringUtils;
import mockit.Deencapsulation;
import mockit.Delegate;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.StrictExpectations;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

@RunWith(JMockit.class)
public class SessionServiceTest {
    @Tested
    private SessionService tested;

    private RedisTemplate<String, String> redis;

    @Injectable
    private UserService userService;

    private RedisConnection connection;

    @Before
    public void setup() {
        connection = createRedisConnectionMock();
        redis = createRedisMock(connection, null);
        tested = new SessionService(redis, userService);
    }

    @Test
    public void getSession_NoSessionFoundInRedis() {
        RedisCallback<Object> callback = x -> null;

        String sessionKey = "sessionKey";
        byte[] qualifiedSessionKey = "S:sessionKey".getBytes(StandardCharsets.UTF_8);
        LocalDateTime now = LocalDateTime.now();
        byte[] nowAsBytes = now.toString().getBytes(StandardCharsets.UTF_8);
        int ttl = 24 * 60 * 60;

        List<Object> redisResult = Arrays.asList(null, null, null);
        Deencapsulation.setField(tested, createRedisMock(connection, redisResult));

        new NonStrictExpectations(LocalDateTime.class) {{
            LocalDateTime.now();
            result = now;
        }};

        new StrictExpectations(callback) {{
            redis.executePipelined(withInstanceLike(callback));

            connection.hGet(qualifiedSessionKey, SessionService.USER_ID_KEY);
            connection.hGet(qualifiedSessionKey, SessionService.CREATED_KEY);
            connection.hGet(qualifiedSessionKey, SessionService.LAST_LOGIN_KEY);
            connection.hSet(qualifiedSessionKey, SessionService.LAST_LOGIN_KEY, nowAsBytes);
            connection.expire(qualifiedSessionKey, ttl);
        }};

        UserSession result = tested.getSession(sessionKey);
        Assert.assertNull(result);
    }

    @Test
    public void getSession_UserIdNotInstanceOfAInteger() {
        RedisCallback<Object> callback = x -> null;

        String sessionKey = "sessionKey";
        byte[] qualifiedSessionKey = "S:sessionKey".getBytes(StandardCharsets.UTF_8);
        LocalDateTime now = LocalDateTime.now();
        byte[] nowAsBytes = now.toString().getBytes(StandardCharsets.UTF_8);
        int ttl = 24 * 60 * 60;

        List<Object> redisResult = Arrays.asList("test", null, null);
        Deencapsulation.setField(tested, createRedisMock(connection, redisResult));

        new NonStrictExpectations(LocalDateTime.class) {{
            LocalDateTime.now();
            result = now;
        }};

        new StrictExpectations(callback) {{
            redis.executePipelined(withInstanceLike(callback));

            connection.hGet(qualifiedSessionKey, SessionService.USER_ID_KEY);
            connection.hGet(qualifiedSessionKey, SessionService.CREATED_KEY);
            connection.hGet(qualifiedSessionKey, SessionService.LAST_LOGIN_KEY);
            connection.hSet(qualifiedSessionKey, SessionService.LAST_LOGIN_KEY, nowAsBytes);
            connection.expire(qualifiedSessionKey, ttl);
        }};

        UserSession result = tested.getSession(sessionKey);
        Assert.assertNull(result);
    }

    @Test
    public void getSession_CreatedAtFieldNotParsed() {
        RedisCallback<Object> callback = x -> null;

        String sessionKey = "sessionKey";
        byte[] qualifiedSessionKey = "S:sessionKey".getBytes(StandardCharsets.UTF_8);
        LocalDateTime now = LocalDateTime.now();
        byte[] nowAsBytes = now.toString().getBytes(StandardCharsets.UTF_8);
        int ttl = 24 * 60 * 60;

        List<Object> redisResult = Arrays.asList("1", "test", null);
        Deencapsulation.setField(tested, createRedisMock(connection, redisResult));

        new NonStrictExpectations(LocalDateTime.class) {{
            LocalDateTime.now();
            result = now;
        }};

        new StrictExpectations(callback) {{
            redis.executePipelined(withInstanceLike(callback));

            connection.hGet(qualifiedSessionKey, SessionService.USER_ID_KEY);
            connection.hGet(qualifiedSessionKey, SessionService.CREATED_KEY);
            connection.hGet(qualifiedSessionKey, SessionService.LAST_LOGIN_KEY);
            connection.hSet(qualifiedSessionKey, SessionService.LAST_LOGIN_KEY, nowAsBytes);
            connection.expire(qualifiedSessionKey, ttl);
        }};

        UserSession result = tested.getSession(sessionKey);
        Assert.assertNull(result);
    }

    @Test
    public void getSession_LastLoginFieldNotParsed() {
        RedisCallback<Object> callback = x -> null;

        String sessionKey = "sessionKey";
        byte[] qualifiedSessionKey = "S:sessionKey".getBytes(StandardCharsets.UTF_8);
        LocalDateTime now = LocalDateTime.now();
        String nowAsString = now.toString();
        byte[] nowAsBytes = nowAsString.getBytes(StandardCharsets.UTF_8);
        int ttl = 24 * 60 * 60;

        List<Object> redisResult = Arrays.asList("1", nowAsString, "test");
        Deencapsulation.setField(tested, createRedisMock(connection, redisResult));

        new NonStrictExpectations(LocalDateTime.class) {{
            LocalDateTime.now();
            result = now;
        }};

        new StrictExpectations(callback) {{
            redis.executePipelined(withInstanceLike(callback));

            connection.hGet(qualifiedSessionKey, SessionService.USER_ID_KEY);
            connection.hGet(qualifiedSessionKey, SessionService.CREATED_KEY);
            connection.hGet(qualifiedSessionKey, SessionService.LAST_LOGIN_KEY);
            connection.hSet(qualifiedSessionKey, SessionService.LAST_LOGIN_KEY, nowAsBytes);
            connection.expire(qualifiedSessionKey, ttl);
        }};

        UserSession result = tested.getSession(sessionKey);
        Assert.assertNull(result);
    }

    @Test
    public void getSession() {
        RedisCallback<Object> callback = x -> null;

        String sessionKey = "sessionKey";
        byte[] qualifiedSessionKey = "S:sessionKey".getBytes(StandardCharsets.UTF_8);
        LocalDateTime now = LocalDateTime.now();
        String nowAsString = now.toString();
        byte[] nowAsBytes = nowAsString.getBytes(StandardCharsets.UTF_8);
        int ttl = 24 * 60 * 60;

        List<Object> redisResult = Arrays.asList("1", nowAsString, nowAsString);
        Deencapsulation.setField(tested, createRedisMock(connection, redisResult));

        new NonStrictExpectations(LocalDateTime.class) {{
            LocalDateTime.now();
            result = now;
        }};

        new StrictExpectations(callback) {{
            redis.executePipelined(withInstanceLike(callback));

            connection.hGet(qualifiedSessionKey, SessionService.USER_ID_KEY);
            connection.hGet(qualifiedSessionKey, SessionService.CREATED_KEY);
            connection.hGet(qualifiedSessionKey, SessionService.LAST_LOGIN_KEY);
            connection.hSet(qualifiedSessionKey, SessionService.LAST_LOGIN_KEY, nowAsBytes);
            connection.expire(qualifiedSessionKey, ttl);
        }};

        UserSession result = tested.getSession(sessionKey);
        Assert.assertNotNull(result);
        Assert.assertEquals(sessionKey, result.getSessionKey());
        Assert.assertEquals(now, result.getCreatedDate());
        Assert.assertEquals(now, result.getLastLoginDate());
    }

    @Test(expected = CannotCreateSessionException.class)
    public void createNewSession_GeneratedSessionKeysNotUnique(@Mocked User user) {
        Deencapsulation.setField(tested, createRedisMock(connection, null, () -> true));
        tested.createNewSession(user);
    }

    @Test
    public void createNewSession_UserHasNoPreviousSession(@Mocked User user) {
        Deencapsulation.setField(tested, createRedisMock(connection, null, () -> false));
        long userId = 1;
        LocalDateTime now = LocalDateTime.now();
        new NonStrictExpectations(LocalDateTime.class) {{
            user.getId();
            result = userId;

            LocalDateTime.now();
            result = now;

            user.getLastSessionKey();
            result = null;
        }};

        int ttl = 24 * 60 * 60;
        String sessionKey = "sessionKey";
        byte[] qualifiedSessionKey = String.format("S:%s", sessionKey).getBytes(StandardCharsets.UTF_8);
        RedisCallback<Object> callback = x -> null;
        byte[] nowAsBytes = now.toString().getBytes(StandardCharsets.UTF_8);
        byte[] createdAtBytes = now.toString().getBytes(StandardCharsets.UTF_8);
        byte[] userIdBytes = Long.toString(userId).getBytes(StandardCharsets.UTF_8);
        new StrictExpectations(IntegrationStringUtils.class) {{
            IntegrationStringUtils.generate(32);
            result = sessionKey;

            userService.markAsLoggedIn(user, sessionKey);

            redis.executePipelined(withInstanceLike(callback));

            connection.hSet(qualifiedSessionKey, SessionService.USER_ID_KEY, userIdBytes);
            connection.hSet(qualifiedSessionKey, SessionService.CREATED_KEY, createdAtBytes);
            connection.hSet(qualifiedSessionKey, SessionService.LAST_LOGIN_KEY, nowAsBytes);
            connection.expire(qualifiedSessionKey, ttl);
        }};

        new Expectations() {{
            connection.del(with(new Delegate<byte[]>() {
                public boolean matches() {
                    throw new RuntimeException("Unexpected invocation to del");
                }
            }));
        }};

        UserSession result = tested.createNewSession(user);
        Assert.assertNotNull(result);
        Assert.assertEquals(now, result.getCreatedDate());
        Assert.assertEquals(now, result.getLastLoginDate());
        Assert.assertEquals(sessionKey, result.getSessionKey());
        Assert.assertEquals(user, result.getUser());
    }

    @Test
    public void createNewSession(@Mocked User user) {
        Deencapsulation.setField(tested, createRedisMock(connection, null, () -> false));
        String lastSessionKey = "lastSessionKey";
        byte[] lastSessionKeyAsBytes = lastSessionKey.getBytes(StandardCharsets.UTF_8);
        long userId = 1;
        LocalDateTime now = LocalDateTime.now();
        new NonStrictExpectations(LocalDateTime.class) {{
            user.getId();
            result = userId;

            LocalDateTime.now();
            result = now;

            user.getLastSessionKey();
            result = lastSessionKey;
        }};

        int ttl = 24 * 60 * 60;
        String sessionKey = "sessionKey";
        byte[] qualifiedSessionKey = String.format("S:%s", sessionKey).getBytes(StandardCharsets.UTF_8);
        RedisCallback<Object> callback = x -> null;
        byte[] nowAsBytes = now.toString().getBytes(StandardCharsets.UTF_8);
        byte[] createdAtBytes = now.toString().getBytes(StandardCharsets.UTF_8);
        byte[] userIdBytes = Long.toString(userId).getBytes(StandardCharsets.UTF_8);
        new StrictExpectations(IntegrationStringUtils.class) {{
            IntegrationStringUtils.generate(32);
            result = sessionKey;

            userService.markAsLoggedIn(user, sessionKey);

            redis.executePipelined(withInstanceLike(callback));

            connection.hSet(qualifiedSessionKey, SessionService.USER_ID_KEY, userIdBytes);
            connection.hSet(qualifiedSessionKey, SessionService.CREATED_KEY, createdAtBytes);
            connection.hSet(qualifiedSessionKey, SessionService.LAST_LOGIN_KEY, nowAsBytes);
            connection.expire(qualifiedSessionKey, ttl);
            connection.del(lastSessionKeyAsBytes);
        }};

        UserSession result = tested.createNewSession(user);
        Assert.assertNotNull(result);
        Assert.assertEquals(now, result.getCreatedDate());
        Assert.assertEquals(now, result.getLastLoginDate());
        Assert.assertEquals(sessionKey, result.getSessionKey());
        Assert.assertEquals(user, result.getUser());
    }

    @Test
    public void setSessionValue(@Mocked HashOperations<String, Object, Object> hashOperations) {
        String sessionKey = "sessionKey";
        String key = "key";
        String value = "value";

        RedisTemplate<String, String> redis = new MockUp<RedisTemplate<String, String>>() {
            @Mock
            public <HK, HV, K> HashOperations<K, HK, HV> opsForHash() {
                return (HashOperations<K, HK, HV>) hashOperations;
            }
        }.getMockInstance();

        Deencapsulation.setField(tested, redis);

        new StrictExpectations() {{
            hashOperations.put("S:" + sessionKey, key, value);
        }};

        tested.setSessionValue(sessionKey, key, value);
    }

    @Test
    public void getSessionValue(@Mocked HashOperations<String, Object, Object> hashOperations) {
        String sessionKey = "sessionKey";
        String key = "key";

        RedisTemplate<String, String> redis = new MockUp<RedisTemplate<String, String>>() {
            @Mock
            public <HK, HV, K> HashOperations<K, HK, HV> opsForHash() {
                return (HashOperations<K, HK, HV>) hashOperations;
            }
        }.getMockInstance();

        Deencapsulation.setField(tested, redis);

        new StrictExpectations() {{
            hashOperations.get("S:" + sessionKey, key);
        }};

        tested.getSessionValue(sessionKey, key);
    }

    @Test
    public void deleteSession_Null() {
        String sessionKey = null;
        RedisTemplate<String, String> redis = new MockUp<RedisTemplate<String, String>>() {
            @Mock
            public <K> Boolean delete(K key) {
                throw new RuntimeException();
            }
        }.getMockInstance();

        Deencapsulation.setField(tested, redis);
        tested.deleteSession(sessionKey);
    }

    @Test
    public void deleteSession() {
        String sessionKey = "sessionKey";
        AtomicBoolean called = new AtomicBoolean(false);
        RedisTemplate<String, String> redis = new MockUp<RedisTemplate<String, String>>() {
            @Mock
            public <K> Boolean delete(K key) {
                called.set(true);
                return true;
            }
        }.getMockInstance();

        Deencapsulation.setField(tested, redis);
        tested.deleteSession(sessionKey);
        Assert.assertTrue(called.get());
    }

    @Test
    public void deleteSessionValue(@Mocked HashOperations<String, Object, Object> hashOperations) {
        String sessionKey = "sessionKey";
        String value = "value";

        RedisTemplate<String, String> redis = new MockUp<RedisTemplate<String, String>>() {
            @Mock
            public <HK, HV, K> HashOperations<K, HK, HV> opsForHash() {
                return (HashOperations<K, HK, HV>) hashOperations;
            }
        }.getMockInstance();

        Deencapsulation.setField(tested, redis);

        new StrictExpectations() {{
            hashOperations.delete("S:" + sessionKey, value);
        }};

        tested.deleteSessionValue(sessionKey, value);
    }

    public RedisConnection createRedisConnectionMock() {
        return new MockUp<RedisConnection>() {
            @Mock
            public byte[] hGet(byte[] key, byte[] field) {
                return null;
            }

            @Mock
            public Boolean hSet(byte[] var1, byte[] var2, byte[] var3) {
                return true;
            }

            @Mock
            public Boolean expire(byte[] var1, long var2) {
                return true;
            }
        }.getMockInstance();

    }

    private RedisTemplate<String, String> createRedisMock(RedisConnection connection, List<Object> returnValue) {
        return createRedisMock(connection, returnValue, null);
    }

    private RedisTemplate<String, String> createRedisMock(RedisConnection connection, List<Object> returnValue, Callable<Boolean> callable) {
        return new MockUp<RedisTemplate<String, String>>() {
            @Mock
            public <K> Boolean hasKey(K key) {
                if (callable == null) {
                    return false;
                }

                try {
                    return callable.call();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            @Mock
            public List<Object> executePipelined(RedisCallback<?> action) {
                action.doInRedis(connection);
                return returnValue;
            }

        }.getMockInstance();
    }
}