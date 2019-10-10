package com.iyzico.challenge.integrator.service.hazelcast;

import java.util.concurrent.TimeUnit;

public interface BLock {

    boolean isLocked();

    String getName();

    void lock();

    void unlock();

    void forceUnlock();

    boolean tryLock(long time, TimeUnit unit) throws InterruptedException;
}

