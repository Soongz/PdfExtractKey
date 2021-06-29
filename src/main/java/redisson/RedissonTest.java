package redisson;

import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Description: tess4j
 *
 * @author Soong
 */
public class RedissonTest {
    public static void main(String[] args) throws Exception {
        Config config = new Config();
        config.useClusterServers()
                .addNodeAddress("redis://10.122.137.12:6381")
                .addNodeAddress("redis://10.122.137.12:6382")
                .addNodeAddress("redis://10.122.137.12:6383")
                .setPassword("gbOrm3Rdvow7lznOiZ");

        RedissonClient revision = Redisson.create(config);
        semaphoreLock(revision);
    }

    public static void normalLock(RedissonClient revision) throws Exception {
        RLock lock = revision.getLock("anyLock");
        lock.lock();
        lock.tryLock(100, 1000, TimeUnit.MICROSECONDS);
        System.out.println(1);
        lock.unlock();
    }

    public static void fairLock(RedissonClient revision) {
        final RLock fairLock = revision.getFairLock("fairLock");
        fairLock.lock();
        fairLock.lock(1000, TimeUnit.MICROSECONDS);
        fairLock.unlock();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        fairLock.unlock();
    }

    public static void multiLock(RedissonClient revision) {
        RLock lock1 = revision.getLock("anyLock1");
        RLock lock2 = revision.getLock("anyLock2");
        RLock lock3 = revision.getLock("anyLock3");
        final RLock multilock = revision.getMultiLock(lock1, lock2, lock3);
    }

    public static void semaphoreLock(RedissonClient revision) throws Exception {
        final RSemaphore semaphore = revision.getSemaphore("semaphore");
        semaphore.trySetPermits(3);
        for (int i = 0; i < 10; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        System.out.println(new Date() + "：线程[" + Thread.currentThread().getName() + "]尝试获取Semaphore锁");
                        semaphore.acquire();
                        System.out.println(new Date() + "：线程[" + Thread.currentThread().getName() + "]成功获取到了Semaphore锁，开始工作");
                        Thread.sleep(1000);
                        semaphore.release();
                        System.out.println(new Date() + "：线程[" + Thread.currentThread().getName() + "]释放Semaphore锁");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

}
