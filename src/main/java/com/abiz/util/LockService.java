package com.abiz.util;

import com.abiz.config.AppConstants;
import org.redisson.api.RCountDownLatch;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class LockService {

    @Resource
    RedissonClient redissonClient;

    public void notifyRequest(String requestId) {
        RCountDownLatch downLatch = redissonClient.getCountDownLatch(
                String.format("%s%s", AppConstants.API_LOCK_PREFIX, requestId));
        downLatch.countDown();
    }

    public BigDecimal acquireLockAndGetCredit(String user) {
        RLock lock = redissonClient.getLock(String.format("%s%s", AppConstants.USER_LOCK_PREFIX, user));
        lock.lock();
        double value = redissonClient.getAtomicDouble(String.format("%s%s", AppConstants.WALLET_PREFIX, user)).get();
        return new BigDecimal(value).setScale(3, RoundingMode.HALF_EVEN);
    }

    public void setCreditAndReleaseLock(String user, BigDecimal newAmount) {
        RLock lock = redissonClient.getLock(String.format("%s%s", AppConstants.USER_LOCK_PREFIX, user));
        redissonClient.getAtomicDouble(String.format("%s%s", AppConstants.WALLET_PREFIX, user)).set(newAmount.doubleValue());
        lock.unlock();
    }

    public BigDecimal getCredit(String user) {
        double value = redissonClient.getAtomicDouble(String.format("%s%s", AppConstants.WALLET_PREFIX, user)).get();
        return new BigDecimal(value).setScale(3, RoundingMode.HALF_EVEN);
    }

    public void releaseLock(String user) {
        RLock lock = redissonClient.getLock(String.format("%s%s", AppConstants.USER_LOCK_PREFIX, user));
        lock.unlock();
    }

    public void createApiRequestLock(String requestId, int size) {
        RCountDownLatch downLatch = redissonClient.getCountDownLatch(
                String.format("%s%s", AppConstants.API_LOCK_PREFIX, requestId));
        downLatch.trySetCount(size);
    }

    public void waitForApiRequest(String requestId) throws InterruptedException {
        RCountDownLatch downLatch = redissonClient.getCountDownLatch(
                String.format("%s%s", AppConstants.API_LOCK_PREFIX, requestId));
        downLatch.await();
        downLatch.delete();
    }
}
