package io.stayhungrystayfoolish.custom.ioc.config;

/**
 * @Author: Created by bonismo@hotmail.com on 2019-08-22 19:57
 * @Description: 封装 XML 中 property 的 value 信息，用以 依赖注入进 BeanDefinition
 * @Version: 1.0
 */
public class TypeStringValue {

    /**
     * 以 String 形式存储的 value (基本数据类型)
     */
    private String value;

    /**
     * value 的 Class 属性，该属性会使用反射，将 value 的值注入进 BeanDefinition 中
     */
    private Class<?> targetType;


    public TypeStringValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Class<?> getTargetType() {
        return targetType;
    }

    public void setTargetType(Class<?> targetType) {
        this.targetType = targetType;
    }
}
