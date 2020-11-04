package pro.chenggang.exercise.task_allocate_exercise.redislock;

/**
 * RedisLockException
 * @author chenggang
 * @date 2019/04/27
 */
public class RedisLockException extends RuntimeException {

    public RedisLockException() {
    }

    public RedisLockException(String message) {
        super(message);
    }

    public RedisLockException(String message, Throwable cause) {
        super(message, cause);
    }

    public RedisLockException(Throwable cause) {
        super(cause);
    }

    public RedisLockException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
