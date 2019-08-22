package io.stayhungrystayfoolish.custom.ioc.config;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: Created by bonismo@hotmail.com on 2019-08-22 10:57
 * @Description: 封装 XML <bean> 标签信息
 * @Version: 1.0
 */
public class BeanDefinition {

    private String beanName;
    private String beanClassName;
    private String initMethod;

    private List<PropertyValue> propertyValues = new ArrayList<>();

    public BeanDefinition(String beanName, String beanClassName) {
        this.beanName = beanName;
        this.beanClassName = beanClassName;
    }

    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public String getBeanClassName() {
        return beanClassName;
    }

    public void setBeanClassName(String beanClassName) {
        this.beanClassName = beanClassName;
    }

    public String getInitMethod() {
        return initMethod;
    }

    public void setInitMethod(String initMethod) {
        this.initMethod = initMethod;
    }

    public List<PropertyValue> getPropertyValues() {
        return propertyValues;
    }

    public void setPropertyValues(List<PropertyValue> propertyValues) {
        this.propertyValues = propertyValues;
    }
}
