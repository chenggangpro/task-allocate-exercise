package pro.chenggang.exercise.task_allocate_exercise.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author chenggang
 * @date 2019/04/27
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface RedisLock {

    /**
     * 锁key,默认为类名（不包含包名）_方法命
     * @return
     */
    String key() default "default";

    /**
     * 表达式,支持Spel
     * @return
     */
    String[] value() default {};

    /**
     * 是否等待超时，若为false表示一直等待
     * @return
     */
    boolean waitingForTimeout() default false;

    /**
     * 是否快速失败
     * @return
     */
    boolean failFast() default true;

    /**
     * 等待的时长
     * @return
     */
    long timeout() default 1000;

    /**
     * 超时单位
     * @return
     */
    TimeUnit timeoutUnit() default TimeUnit.MILLISECONDS;

}
