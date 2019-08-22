package io.stayhungrystayfoolish.custom.ioc.converter;

/**
 * @Author: Created by bonismo@hotmail.com on 2019-08-22 16:15
 * @Description:
 * @Version: 1.0
 */
public class DoubleTypeConverter implements TypeConverter {

    @Override
    public Boolean isType(Class<?> clazz) {
        return clazz == Double.class || clazz == double.class;
    }

    @Override
    public Object convert(String source) {
        return Double.parseDouble(source);
    }
}
