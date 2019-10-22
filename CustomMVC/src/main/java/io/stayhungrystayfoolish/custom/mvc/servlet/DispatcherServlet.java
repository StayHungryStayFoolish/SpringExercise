package io.stayhungrystayfoolish.custom.mvc.servlet;

import io.stayhungrystayfoolish.custom.ioc.factory.BeanFactory;
import io.stayhungrystayfoolish.custom.ioc.factory.DefaultListableBeanFactory;
import io.stayhungrystayfoolish.custom.mvc.adapter.iface.HandlerAdapter;
import io.stayhungrystayfoolish.custom.mvc.handlermapping.HandlerMapping;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: bonismo@hotmail.com
 * @Description:
 * @Date: 2019-10-18 02:05
 * @Version: V1.0
 */
public class DispatcherServlet extends AbstractHttpServlet {

    private List<HandlerMapping> handlerMappings = new ArrayList<>();
    private List<HandlerAdapter> handlerAdapters = new ArrayList<>();

    @Override
    public void init(ServletConfig config) throws ServletException {
        String contextConfig = config.getInitParameter("contextConfigLocation");
        // 创建 IoC 容器
        BeanFactory beanFactory = new DefaultListableBeanFactory(contextConfig);
        // 一次性创建所有 Bean
        beanFactory.getBeansByType(Object.class);
        handlerMappings = beanFactory.getBeansByType(HandlerMapping.class);
        handlerAdapters = beanFactory.getBeansByType(HandlerAdapter.class);
    }

    @Override
    public void doDispatch(HttpServletRequest req, HttpServletResponse resp) {
        // 获取处理器
        Object handler = getHandler(req);
        if (null == handler) {
            return;
        }
        // 获取处理器适配器
        HandlerAdapter adapter = getAdapter(handler);
        if (null == adapter) {
            return;
        }

        adapter.handleRequest(handler, req, resp);
    }

    private Object getHandler(HttpServletRequest req) {
        for (HandlerMapping handlerMapping : handlerMappings) {
            Object handler = handlerMapping.getHandler(req);
            if (null != handler) {
                return handler;
            }
        }
        return null;
    }

    private HandlerAdapter getAdapter(Object handler) {
        for (HandlerAdapter adapter : handlerAdapters) {
            if (adapter.support(handler)) {
                return adapter;
            }
        }
        return null;
    }
}
