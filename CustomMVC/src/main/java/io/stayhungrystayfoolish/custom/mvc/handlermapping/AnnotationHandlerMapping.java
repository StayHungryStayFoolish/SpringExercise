package io.stayhungrystayfoolish.custom.mvc.handlermapping;

import io.stayhungrystayfoolish.custom.ioc.aware.BeanFactoryAware;
import io.stayhungrystayfoolish.custom.ioc.config.BeanDefinition;
import io.stayhungrystayfoolish.custom.ioc.factory.BeanFactory;
import io.stayhungrystayfoolish.custom.mvc.annotation.Controller;
import io.stayhungrystayfoolish.custom.mvc.annotation.RequestMapping;
import io.stayhungrystayfoolish.custom.mvc.handlermapping.config.HandlerMethod;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: Created by bonismo@hotmail.com on 2019-10-22 16:36
 * @Description:
 * @Version: 1.0  继承 BeanFactoryAware 取得 BeanFactory ，
 * 从 IoC 容器获取 BeanDefinition 将带有 @Controller 和 @RequestMapping 的类和方法与注解上的 uri 建立映射关系
 * 最终可以取出 uri 对应的 Method 调用 method.invoke()
 */
public class AnnotationHandlerMapping implements HandlerMapping, BeanFactoryAware {

    private BeanFactory beanFactory;

    /**
     * 带有 @Controller 和 @RequestMapping 的类和方法与注解上的 uri 建立映射关系
     */
    private Map<String, HandlerMethod> annotationHandlerMethodMap = new HashMap<>();


    public void init() {
        try {
            // 获取 IoC 容器所有 BeanName
            List<String> beanNames = beanFactory.getBeanNamesByType(Object.class);
            for (String beanName : beanNames) {
                Map<String, BeanDefinition> beanDefinitionMap = beanFactory.getBeanDefinitions();
                BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
                String beanClassName = beanDefinition.getBeanClassName();
                Class<?> clazz = Class.forName(beanClassName);
                // 判断是否带有  @Controller 或者 @RequestMapping 的注解
                if (isHandler(clazz)) {
                    Method[] methods = clazz.getMethods();
                    for (Method method : methods) {
                        // 如果该方法带有 @RequestMapping 注解
                        if (method.isAnnotationPresent(RequestMapping.class)) {
                            RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
                            // 获取该方法注解上的 url
                            String url = requestMapping.value();
                            Object handler = beanFactory.getBean(beanName);
                            HandlerMethod handlerMethod = new HandlerMethod(method, handler);
                            // 建立 url 和 HandlerMethod 映射关系
                            // 因为注解只有 @RequestMapping 并没实现具体请求方法类型，所以此处没有问题。
                            // 如果是 RESTFul 风格，此处需要优化。
                            // TODO: 2019-10-22 相同的 url，按照不同的请求方式建立映射
                            annotationHandlerMethodMap.put(url, handlerMethod);
                        }
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 通过 Aware 接口注入 BeanFactory
     * 该方法在初始化 Instance 时会判断是否有 Aware 标记
     *
     * @param beanFactory beanFactory
     * @see io.stayhungrystayfoolish.custom.ioc.factory.DefaultListableBeanFactory#initInstance(Object, BeanDefinition)
     */
    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    /**
     * 根据 Url 获取 init 方法中的 HandlerMethod 对象
     *
     * @param request
     * @return
     */
    @Override
    public Object getHandler(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return annotationHandlerMethodMap.get(uri);
    }

    private Boolean isHandler(Class<?> clazz) {
        return clazz.isAnnotationPresent(Controller.class) || clazz.isAnnotationPresent(RequestMapping.class);
    }
}
