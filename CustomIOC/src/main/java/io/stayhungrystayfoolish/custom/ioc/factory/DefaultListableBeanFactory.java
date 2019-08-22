package io.stayhungrystayfoolish.custom.ioc.factory;

import io.stayhungrystayfoolish.custom.ioc.config.BeanDefinition;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: Created by bonismo@hotmail.com on 2019-08-22 11:51
 * @Description:
 * @Version: 1.0
 */
public class DefaultListableBeanFactory extends AbstractBeanFactory {

    /**
     * 存储根据 XML 读取信息封装的 BeanDefinition 的集合
     * Key: String beanName
     * Value: 封装的 Bean 信息
     */
    private Map<String, BeanDefinition> beanDefinitionMap = new HashMap<>();

    /**
     * 根据 BeanName 存储 Bean 实例的集合
     * Key: String BeanName
     * Value: Bean 实例
     */
    private Map<String, Object> singletonBeanMap = new HashMap<>();


    public DefaultListableBeanFactory(String location) {

    }


    @Override
    public Object getBean(String beanName) {
        Object instance = singletonBeanMap.get(beanName);
        if (null != instance) {
            return instance;
        }
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        return super.getBean(beanName);
    }
}
