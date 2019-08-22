package io.stayhungrystayfoolish.custom.ioc.converter;

/**
 * @Author: Created by bonismo@hotmail.com on 2019-08-22 15:51
 * @Description:
 * @Version: 1.0
 */
public class IntegerTypeConverter implements TypeConverter {

    @Override
    public Boolean isType(Class<?> clazz) {
        return clazz == Integer.class || clazz == int.class;
    }

    @Override
    public Object convert(String source) {
        return Integer.parseInt(source);
    }
}
