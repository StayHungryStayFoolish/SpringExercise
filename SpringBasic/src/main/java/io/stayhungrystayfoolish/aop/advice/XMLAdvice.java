package io.stayhungrystayfoolish.aop.advice;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @Author: bonismo@hotmail.com
 * @Description:
 * @Date: 2019-08-18 16:59
 * @Version: V1.0
 * @Link: https://blog.csdn.net/f641385712/article/details/88975543
 */
public class XMLAdvice {

    private Logger logger = LoggerFactory.getLogger(XMLAdvice.class);

    /**
     * 方法调用前执行
     */
    private void beforeAdvice(JoinPoint joinpoint) {
        logger.info("Before Advice Do Something ...");
    }

    /**
     * 方法调用前、调用后执行
     */
    private void aroundInterceptor(ProceedingJoinPoint proceedingJoinPoint) {
        proceedingJoinPoint.getSignature();
        logger.info("Around Interceptor Do Something ...");
    }

    /**
     * 方法调用后执行（正常执行结束或者异常退出）
     * @param joinPoint
     */
    private void afterAdvice(JoinPoint joinPoint) {
        logger.info("After Advice Do Something ...");
    }

    /**
     * 方法调用后执行（正常执行结束，无论是否有返回值，均执行）
     * @param joinPoint
     */
    private void returnAdvice(JoinPoint joinPoint) {
        logger.info("Return Advice Do Something ...");
    }

    /**
     * 方法调用异常退出后执行（处理错误信息）
     * @param joinPoint
     * @param throwable
     */
    private void throwingAdvice(JoinPoint joinPoint, Throwable throwable) {
        logger.info("Throwing Advice Do Something ...");
    }
}
