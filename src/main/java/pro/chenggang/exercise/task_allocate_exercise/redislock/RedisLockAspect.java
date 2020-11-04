package pro.chenggang.exercise.task_allocate_exercise.redislock;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.integration.redis.util.RedisLockRegistry;
import pro.chenggang.exercise.task_allocate_exercise.annotation.RedisLock;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

/**
 * RedisLockAspect
 * @author chenggang
 * @date 2019/04/27
 */
@Slf4j
@Aspect
public class RedisLockAspect {

    private final RedisLockRegistry redisLockRegistry;

    private SpelExpressionParser expressionParser = new SpelExpressionParser();

    private ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    public RedisLockAspect(RedisLockRegistry redisLockRegistry) {
        this.redisLockRegistry = redisLockRegistry;
    }

    @Around("@annotation(pro.chenggang.exercise.task_allocate_exercise.annotation.RedisLock)&& @annotation(redisLock)")
    public Object lockRequest(ProceedingJoinPoint joinPoint, RedisLock redisLock) throws Throwable {
        String lockKey = redisLock.key();
        Object[] args = joinPoint.getArgs();
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();
        String[] expressionValues = redisLock.value();
        if(ArrayUtils.isNotEmpty(expressionValues) && !StringUtils.isAnyBlank(expressionValues)){
            Object target = joinPoint.getTarget();
            String spelKey = getSpelKey(target,method,args,expressionValues);
            lockKey += spelKey;
        }
        if(StringUtils.isBlank(lockKey)){
            lockKey = method.getDeclaringClass().getSimpleName() + "_" + method.getName();
        }
        log.info("[RedisLock]LockKey:{}", lockKey);
        Lock lock = null;
        try{
            lock = redisLockRegistry.obtain(lockKey);
            boolean waitingForTimeout = redisLock.waitingForTimeout();
            boolean failFast = redisLock.failFast();
            if(!waitingForTimeout && failFast){
                boolean lockOnce = lock.tryLock();
                if(!lockOnce) {
                    log.info("[RedisLock](只获取锁一次)尝试上锁失败，LockKey:{}", lockKey);
                    throw new RedisLockException("[RedisLock](只获取锁一次)尝试上锁失败，LockKey:" + lockKey);
                }
                log.debug("[RedisLock](只获取锁一次)上锁成功，LockKey:{}",lockKey);
            } else if(waitingForTimeout){
                long timeout = redisLock.timeout();
                TimeUnit timeoutUnit = redisLock.timeoutUnit();
                boolean lockTimeout = lock.tryLock(timeout, timeoutUnit);
                if(!lockTimeout){
                    log.info("[RedisLock](等待获取锁)尝试上锁失败，LockKey:{}，超时时间:{}，超时时间单位:{}",lockKey,timeout,timeoutUnit);
                    throw new RedisLockTimeoutException("[RedisLock](获取锁超时)尝试上锁失败，LockKey:"+lockKey+",超时时间:"+timeout+",超时时间单位"+timeoutUnit);
                }
                log.debug("[RedisLock](等待获取锁)上锁成功，LockKey:{}",lockKey);
            }else{
                log.debug("[RedisLock](永久等待上锁,Redis锁默认60s)，LockKey:{}",lockKey);
                lock.lock();
                log.debug("[RedisLock](永久等待上锁,Redis锁默认60s)上锁成功，LockKey:{}",lockKey);
            }
            return joinPoint.proceed();
        }finally{
            if(Objects.nonNull(lock)){
                try {
                    lock.unlock();
                    log.info("[RedisLock]UnLock,LockKey:{}", lockKey);
                } catch (Exception e) {
                    log.error("[RedisLock]解锁异常:{},LockKey:{},异常信息:{}",lockKey,ExceptionUtils.getStackTrace(e));
                }
            }
        }
    }

    /**
     * get Lock SpEL key
     * @param target
     * @param method
     * @param args
     * @param expressionValues
     * @return
     */
    private String getSpelKey(Object target, Method method, Object[] args,String[] expressionValues){
        StringBuilder tempBuilder;
        try{
            tempBuilder = new StringBuilder();
            EvaluationContext methodBasedEvaluationContext = new MethodBasedEvaluationContext(target,method,args,parameterNameDiscoverer);
            Expression parseExpression;
            String parseExpressionValue;
            for(String expression : expressionValues){
                parseExpression = expressionParser.parseExpression(expression);
                parseExpressionValue = parseExpression.getValue(methodBasedEvaluationContext, String.class);
                tempBuilder.append(":").append(parseExpressionValue);
            }
        }catch (Exception e){
            tempBuilder = new StringBuilder();
            for(String expression : expressionValues){
                tempBuilder.append(":").append(expression);
            }
            log.error("RedisLock]Get Spel Key Error,Default Spel Key :{},Exception:{} ",tempBuilder.toString(), ExceptionUtils.getStackTrace(e));
        }
        return tempBuilder.toString();
    }
}
