package com.iyzico.challenge.integrator.service.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.iyzico.challenge.integrator.service.hazelcast.exception.CannotHoldTheLockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

@Service
public class LockService {
    private final Logger logger = LoggerFactory.getLogger(LockService.class);

    private final IMap<String, Object> lockMap;

    public LockService(HazelcastInstance hazelcast) {
        lockMap = hazelcast.getMap("integrator.lock.map");
    }

    public <T> T executeInLock(String key, Callable<T> task) {
        boolean locked = false;
        BLock lock = null;
        try {
            lock = getLock(key);
            logger.trace("Trying to get lock for key '{}'", key);
            if (lock.tryLock(30, TimeUnit.SECONDS)) {
                logger.trace("Lock hold for key '{}'", key);
                locked = true;
                return task.call();
            }
        } catch (Throwable e) {
            if (locked) {
                lock.unlock();
            }
            throw new CannotHoldTheLockException(e);
        }

        logger.trace("Cannot hold the lock for key '{}'", key);
        throw new CannotHoldTheLockException(String.format("Unable to acquire lock with key %s in the maximum wait time.", key));
    }

    public void executeInLockWithoutResult(String key, Runnable task) {
        executeInLock(key, () -> {
            task.run();
            return null;
        });
    }

    public BLock getLock(String lockName) {
        return new BLock() {
            @Override
            public boolean isLocked() {
                return lockMap.isLocked(lockName);
            }

            @Override
            public String getName() {
                return lockName;
            }

            @Override
            public void forceUnlock() {
                lockMap.forceUnlock(lockName);
            }

            @Override
            public void lock() {
                lockMap.lock(lockName);
            }

            @Override
            public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
                return lockMap.tryLock(lockName, time, unit);
            }

            @Override
            public void unlock() {
                lockMap.unlock(lockName);
            }
        };
    }
}
