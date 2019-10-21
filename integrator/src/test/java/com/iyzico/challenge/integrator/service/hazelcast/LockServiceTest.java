package com.iyzico.challenge.integrator.service.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.iyzico.challenge.integrator.data.entity.User;
import com.iyzico.challenge.integrator.dto.ErrorCode;
import com.iyzico.challenge.integrator.exception.BaseIntegratorException;
import com.iyzico.challenge.integrator.service.hazelcast.exception.CannotHoldTheLockException;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.StrictExpectations;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

@RunWith(JMockit.class)
public class LockServiceTest {
    @Tested
    private LockService tested;

    @Injectable
    private HazelcastInstance hazelcast;

    @Mocked
    private IMap<String, Object> lockMap;

    @Before
    public void setup() {
        tested = new LockService(hazelcast);
        Deencapsulation.setField(tested, lockMap);
    }

    @Test
    public void constructor() {
        new StrictExpectations() {{
            hazelcast.getMap("integrator.lock.map");
            result = lockMap;
        }};

        LockService lockService = new LockService(hazelcast);
        Assert.assertEquals(lockMap, Deencapsulation.getField(lockService, IMap.class));
    }

    @Test
    public void executeInProductLock() {
        long productId = Long.MAX_VALUE;
        String key = "product-" + productId;

        Object object = new Object();
        Callable<Object> callable = () -> object;
        new StrictExpectations(tested) {{
            tested.executeInLock(key, callable);
            result = object;
        }};

        Object result = tested.executeInProductLock(productId, callable);
        Assert.assertNotNull(result);
        Assert.assertEquals(object, result);
    }

    @Test
    public void getProductLock() throws InterruptedException {
        long productId = Long.MAX_VALUE;

        String key = "product-" + productId;

        BLock lock = tested.getProductLock(productId);

        new StrictExpectations() {{
            lockMap.tryLock(key, 1, TimeUnit.SECONDS);
            lockMap.forceUnlock(key);
            lockMap.isLocked(key);
            lockMap.lock(key);
        }};

        Assert.assertNotNull(lock);
        Assert.assertEquals(key, lock.getName());

        lock.tryLock(1, TimeUnit.SECONDS);
        lock.forceUnlock();
        lock.isLocked();
        lock.lock();
    }

    @Test
    public void executeInBasketLock() {
        long userId = Long.MAX_VALUE;
        User user = new User();
        user.setId(userId);
        String key = "basket-" + userId;

        Object object = new Object();
        Callable<Object> callable = () -> object;
        new StrictExpectations(tested) {{
            tested.executeInLock(key, callable);
            result = object;
        }};

        Object result = tested.executeInBasketLock(user, callable);
        Assert.assertNotNull(result);
        Assert.assertEquals(object, result);

    }

    @Test(expected = CannotHoldTheLockException.class)
    public void executeInLock_InterruptedExceptionDuringLock() throws InterruptedException {
        String lockKey = "lockKey";

        Callable<Void> callable = () -> null;
        new Expectations() {{
            lockMap.unlock(anyString);
            times = 0;
        }};

        new StrictExpectations() {{
            lockMap.tryLock(lockKey, 30, TimeUnit.SECONDS);
            result = new InterruptedException();
        }};

        tested.executeInLock(lockKey, callable);
    }

    @Test(expected = BaseIntegratorException.class)
    public void executeInLock_ThrownAKnownException() throws InterruptedException {
        String lockKey = "lockKey";

        Callable<Void> callable = () -> {
            throw new BaseIntegratorException("test") {

                @Override
                public ErrorCode getErrorCode() {
                    return null;
                }
            };
        };

        new StrictExpectations() {{
            lockMap.tryLock(lockKey, 30, TimeUnit.SECONDS);
            result = true;

            lockMap.unlock(lockKey);
        }};

        tested.executeInLock(lockKey, callable);
    }

    @Test(expected = BaseIntegratorException.class)
    public void executeInLock_ThrownAWrappedKnownException() throws InterruptedException {
        String lockKey = "lockKey";

        Callable<Void> callable = () -> {
            throw new RuntimeException(new BaseIntegratorException("test") {

                @Override
                public ErrorCode getErrorCode() {
                    return null;
                }
            });
        };

        new StrictExpectations() {{
            lockMap.tryLock(lockKey, 30, TimeUnit.SECONDS);
            result = true;

            lockMap.unlock(lockKey);
        }};

        tested.executeInLock(lockKey, callable);
    }

    @Test(expected = RuntimeException.class)
    public void executeInLock_ThrownARuntimeException() throws InterruptedException {
        String lockKey = "lockKey";

        Callable<Void> callable = () -> {
            throw new RuntimeException();
        };

        new StrictExpectations() {{
            lockMap.tryLock(lockKey, 30, TimeUnit.SECONDS);
            result = true;

            lockMap.unlock(lockKey);
        }};

        tested.executeInLock(lockKey, callable);
    }

    @Test(expected = CannotHoldTheLockException.class)
    public void executeInLock_CannotHoldLock() throws InterruptedException {
        String lockKey = "lockKey";

        Callable<Void> callable = () -> {
            throw new RuntimeException();
        };

        new Expectations() {{
            lockMap.unlock(anyString);
            times = 0;
        }};

        new StrictExpectations() {{
            lockMap.tryLock(lockKey, 30, TimeUnit.SECONDS);
            result = false;
        }};

        tested.executeInLock(lockKey, callable);
    }

    @Test
    public void executeInLock() throws InterruptedException {
        String lockKey = "lockKey";

        Object object = new Object();
        Callable<Object> callable = () -> object;

        new StrictExpectations() {{
            lockMap.tryLock(lockKey, 30, TimeUnit.SECONDS);
            result = true;

            lockMap.unlock(anyString);
        }};

        Object result = tested.executeInLock(lockKey, callable);
        Assert.assertNotNull(result);
        Assert.assertEquals(object, result);
    }
}