package io.stayhungrystayfoolish.custom.ioc.converter;

/**
 * @Author: Created by bonismo@hotmail.com on 2019-08-22 16:16
 * @Description:
 * @Version: 1.0
 */
public class FloatTypeConverter implements TypeConverter {

    @Override
    public Boolean isType(Class<?> clazz) {
        return clazz == Float.class || clazz == float.class;
    }

    @Override
    public Object convert(String source) {
        return Float.parseFloat(source);
    }
}
