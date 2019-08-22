package io.stayhungrystayfoolish.custom.ioc.converter;

/**
 * @Author: Created by bonismo@hotmail.com on 2019-08-22 16:14
 * @Description:
 * @Version: 1.0
 */
public class LongTypeConverter implements TypeConverter {

    @Override
    public Boolean isType(Class<?> clazz) {
        return clazz == Long.class || clazz == long.class;
    }

    @Override
    public Object convert(String source) {
        return Long.parseLong(source);
    }
}
