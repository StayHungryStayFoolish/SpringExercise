package io.stayhungrystayfoolish.custom.ioc.factory;

import io.stayhungrystayfoolish.custom.ioc.config.BeanDefinition;

import java.util.List;
import java.util.Map;

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

    /**
     * 根据 bean name 获取实例
     * @param beanName beanName
     * @param args 参数
     * @return 对象实例
     */
    Object getBean(String beanName, String... args);

    /**
     * 根据指定 bean 类型，获取对应的类型和子类型对应的bean实例
     * @param clazz clazz
     * @return Bean 实例集合
     */
    public <T> List<T> getBeansByType(Class<T> clazz);

    /**
     * 根据指定 bean 类型，获取对应的类型和子类型对应的bean名称
     * @param type type
     * @return Bean 名称结合
     */
    public List<String> getBeanNamesByType(Class<?> type);

    /**
     * 获取 BeanDefinition 集合
     * @return BeanDefinition 集合
     */
    public Map<String, BeanDefinition> getBeanDefinitions();
}
