package pro.chenggang.exercise.task_allocate_exercise.redislock;

/**
 * RedisLockTimeoutException
 * @author chenggang
 * @date 2019/04/27
 */
public class RedisLockTimeoutException extends RedisLockException {

    public RedisLockTimeoutException() {
    }

    public RedisLockTimeoutException(String message) {
        super(message);
    }

    public RedisLockTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

    public RedisLockTimeoutException(Throwable cause) {
        super(cause);
    }

    public RedisLockTimeoutException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
