package io.stayhungrystayfoolish.custom.ioc.converter;

/**
 * @Author: Created by bonismo@hotmail.com on 2019-08-22 16:19
 * @Description:
 * @Version: 1.0
 */
public class CharTypeConverter implements TypeConverter {

    @Override
    public Boolean isType(Class<?> clazz) {
        return clazz == Character.class || clazz == char.class;
    }

    @Override
    public Object convert(String source) {
        if (source.length() == 1) {
            return source.charAt(0);
        }
        return source.toCharArray();
    }
}
