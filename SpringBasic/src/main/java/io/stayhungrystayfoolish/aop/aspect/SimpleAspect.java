package io.stayhungrystayfoolish.aop.aspect;

import io.stayhungrystayfoolish.aop.domain.User;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Modifier;

/**
 * @Author: Created by bonismo@hotmail.com on 2019-08-20 19:14
 * @Description:
 * @Version: 1.0
 */
@Aspect
@Component
public class SimpleAspect {

    private Logger logger = LoggerFactory.getLogger(SimpleAspect.class);

    /**
     * before 目标方法调用前，执行此 advice
     */
    @Before("execution(* io.stayhungrystayfoolish.aop.service..*.*(..))")
    private void xmlBeforeAdvice(JoinPoint joinPoint) {
        logger.info("Before Advice Do Something ...");
        System.out.println("目标方法名为: " + joinPoint.getSignature().getName());
        System.out.println("目标方法所属类的简单类名: " + joinPoint.getSignature().getDeclaringType().getSimpleName());
        System.out.println("目标方法所属类的类名: " + joinPoint.getSignature().getDeclaringTypeName());
        System.out.println("目标方法声明类型: " + Modifier.toString(joinPoint.getSignature().getModifiers()));
        //获取传入目标方法的参数
        Object[] args = joinPoint.getArgs();
        for (int i = 0; i < args.length; i++) {
            System.out.println("第 " + (i + 1) + " 个参数为: " + args[i]);
        }
        System.out.println("被代理的对象: " + joinPoint.getTarget());
        System.out.println("代理对象自己: " + joinPoint.getThis());
    }

    /**
     * around 目标方法调用前、调用后处理，执行此 advice（当抛出异常，立即退出，会转向 after advice，执行完转到 throwing advice）
     * <aop:around method="xmlAroundInterceptor" pointcut="execution(* io.stayhungrystayfoolish.aop.service..*.*(..)) throws "/>
     */
    @Around("execution(* io.stayhungrystayfoolish.aop.service..*.*(..))")
    private Object xmlAroundInterceptor(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        logger.info("Around Interceptor Do Something ...");
        Object result = null;
        try {
            System.out.println("目标方法名为: " + proceedingJoinPoint.getSignature().getName());
            // 正常执行核心方法
//             result = proceedingJoinPoint.proceed();
            // 人为传错参数，进入异常
            result = proceedingJoinPoint.proceed(new Object[]{"Lily ", 19});
        } catch (Throwable throwable) {
            // 重新传入正确参数
            result = proceedingJoinPoint.proceed(new Object[]{new User("Lily ", 19)});
            logger.error("方法执行异常后处理..." + result);
            throwable.printStackTrace();
        }
        logger.info("方法执行结果后...");
        return result;
    }

    /**
     * after 目标方法正常结束、异常都执行此 advice
     * <aop:after method="xmlAfterAdvice" pointcut="execution(* io.stayhungrystayfoolish.aop.service..*.*(..)) throws "/>
     */
    @After("execution(* io.stayhungrystayfoolish.aop.service..*.*(..))")
    private void xmlAfterAdvice(JoinPoint joinPoint) {
        logger.info("After Advice Do Something ...");
    }

    /**
     * after returning 目标方法调用正常结束，不管有无返回结果，执行此 advice
     */
    @AfterReturning("execution(* io.stayhungrystayfoolish.aop.service..*.*(..))")
    private void xmlAfterReturnAdvice(JoinPoint joinPoint) {
        logger.info("After Returning Advice Do Something ...");
    }

    /**
     * after throwing 目标方法执行异常退出，执行此 advice
     */
    @AfterThrowing(pointcut = "SimpleAspect.commonPointcut()", throwing = "throwable")
    private void xmlAfterThrowingAdvice(JoinPoint joinPoint, Throwable throwable) {
        logger.info("Throwable Message ..." + throwable.getMessage());
        logger.info("After Throwing Advice Do Something ...");
    }

    /**
     * 如果多个切入点一样，则使用该方式，或定义一个常量。
     */
    @Pointcut("execution(* io.stayhungrystayfoolish.aop.service..*.*(..))")
    private void commonPointcut() {

    }
}
