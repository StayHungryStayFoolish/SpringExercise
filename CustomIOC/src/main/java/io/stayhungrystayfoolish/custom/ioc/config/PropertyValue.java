package io.stayhungrystayfoolish.custom.ioc.config;

/**
 * @Author: Created by bonismo@hotmail.com on 2019-08-22 12:17
 * @Description: 存储 XML <bean> 标签内的信息
 * @Version: 1.0
 */
public class PropertyValue {

    private String name;
    private String value;

    public PropertyValue(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
