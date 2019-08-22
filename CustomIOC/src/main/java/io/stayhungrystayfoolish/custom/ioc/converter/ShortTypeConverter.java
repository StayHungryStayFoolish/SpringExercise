package io.stayhungrystayfoolish.custom.ioc.converter;

/**
 * @Author: Created by bonismo@hotmail.com on 2019-08-22 16:24
 * @Description:
 * @Version: 1.0
 */
public class ShortTypeConverter implements TypeConverter {

    @Override
    public Boolean isType(Class<?> clazz) {
        return clazz == Short.class || clazz == short.class;
    }

    @Override
    public Object convert(String source) {
        return Short.parseShort(source);
    }
}
