package io.stayhungrystayfoolish.custom.mvc.adapter.iface;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Author: bonismo@hotmail.com
 * @Description:
 * @Date: 2019-10-17 15:58
 * @Version: V1.0
 */
public interface HandlerAdapter {

    /**
     * 校验 Handler 是否支持
     */
    boolean support(Object handler);

    /**
     * 处理请求
     */
    void handleRequest(Object handler, HttpServletRequest request, HttpServletResponse response);
}
