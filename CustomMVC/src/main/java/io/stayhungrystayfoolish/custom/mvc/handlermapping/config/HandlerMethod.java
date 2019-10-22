package io.stayhungrystayfoolish.custom.mvc.handlermapping.config;

import java.lang.reflect.Method;

/**
 * @Author: Created by bonismo@hotmail.com on 2019-10-22 14:50
 * @Description: 封装注解请求的 Handler(业务中的 Controller)
 * @Version: 1.0
 */
public class HandlerMethod {

    /**
     * 最终使用  method.invoke() 实现 @Controller 注解内的  @RequestMapping 的方法
     */
    private Method method;

    /**
     * 业务中为 Controller
     */
    private Object handler;

    public HandlerMethod(Method method, Object handler) {
        this.method = method;
        this.handler = handler;
    }

    public Object getHandler() {
        return handler;
    }

    public void setHandler(Object handler) {
        this.handler = handler;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }
}
