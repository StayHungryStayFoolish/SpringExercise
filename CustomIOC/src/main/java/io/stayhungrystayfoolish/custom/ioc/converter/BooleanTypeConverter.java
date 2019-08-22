package io.stayhungrystayfoolish.custom.ioc.converter;

/**
 * @Author: Created by bonismo@hotmail.com on 2019-08-22 15:59
 * @Description:
 * @Version: 1.0
 */
public class BooleanTypeConverter implements TypeConverter {


    @Override
    public Boolean isType(Class<?> clazz) {
        return clazz == Boolean.class || clazz == boolean.class;
    }

    @Override
    public Object convert(String source) {
        return Boolean.parseBoolean(source);
    }
}
