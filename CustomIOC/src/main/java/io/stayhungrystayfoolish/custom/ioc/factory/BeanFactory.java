package io.stayhungrystayfoolish.custom.ioc.factory;

/**
 * @Author: Created by bonismo@hotmail.com on 2019-08-22 10:57
 * @Description: 使用简单工厂设计模式，根据 name 获取实例
 * @Version: 1.0
 */
public interface BeanFactory {

    /**
     * 根据 bean name 获取实例
     * @param beanName beanName
     * @return 对象实例
     */
    Object getBean(String beanName);

    Object getBean(String beanName, String... args);
}
