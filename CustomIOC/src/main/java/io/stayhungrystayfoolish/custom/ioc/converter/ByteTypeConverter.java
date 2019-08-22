package io.stayhungrystayfoolish.custom.ioc.converter;

/**
 * @Author: Created by bonismo@hotmail.com on 2019-08-22 16:18
 * @Description:
 * @Version: 1.0
 */
public class ByteTypeConverter implements TypeConverter {

    @Override
    public Boolean isType(Class<?> clazz) {
        return clazz == Byte.class || clazz == byte.class;
    }

    @Override
    public Object convert(String source) {
        return Byte.parseByte(source);
    }


}
