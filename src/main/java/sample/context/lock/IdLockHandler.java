package sample.context.lock;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

import javax.inject.Singleton;

import lombok.Value;
import sample.InvocationException;

/**
 * The lock of the ID unit.
 * low: It is simple and targets only the ID lock of the account unit here.
 * low: You take the pessimistic lock by "for update" demand on a lock table of DB,
 * but usually do it to memory lock because it is a sample.
 */
@Singleton
public class IdLockHandler {

    private ConcurrentMap<Serializable, ReentrantReadWriteLock> lockMap = new ConcurrentHashMap<>();

    public void call(Serializable id, LockType lockType, final Runnable command) {
        call(id, lockType, () -> {
            command.run();
            return true;
        });
    }

    public <T> T call(Serializable id, LockType lockType, final Supplier<T> callable) {
        if (lockType.isWrite()) {
            writeLock(id);
        } else {
            readLock(id);
        }
        try {
            return callable.get();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new InvocationException("error.Exception", e);
        } finally {
            unlock(id);
        }
    }

    public void writeLock(final Serializable id) {
        Optional.of(id).ifPresent((v) -> {
            idLock(v).writeLock().lock();
        });
    }

    private ReentrantReadWriteLock idLock(final Serializable id) {
        return lockMap.computeIfAbsent(id, v -> new ReentrantReadWriteLock());
    }

    public void readLock(final Serializable id) {
        Optional.of(id).ifPresent((v) -> {
            idLock(v).readLock().lock();
        });
    }

    public void unlock(final Serializable id) {
        Optional.of(id).ifPresent((v) -> {
            ReentrantReadWriteLock idLock = idLock(v);
            if (idLock.isWriteLockedByCurrentThread()) {
                idLock.writeLock().unlock();
            } else {
                idLock.readLock().unlock();
            }
        });
    }

    public static enum LockType {
        Read,
        Write;

        public boolean isRead() {
            return !isWrite();
        }

        public boolean isWrite() {
            return this == Write;
        }
    }

    @Value
    public static class IdLockPair {
        private Serializable id;
        private LockType lockType;
    }
    
}
