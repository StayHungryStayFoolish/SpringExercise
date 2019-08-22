package io.stayhungrystayfoolish.custom.ioc.config;

/**
 * @Author: Created by bonismo@hotmail.com on 2019-08-22 12:17
 * @Description: 存储 XML <bean> 标签内的信息
 * @Version: 1.0
 */
public class PropertyValue {

    /**
     * 属性名
     */
    private String name;
    /**
     * 属性值和属性类型的封装对象
     */
    private Object value;

    public PropertyValue(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
