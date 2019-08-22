package io.stayhungrystayfoolish.custom.ioc.converter;

/**
 * @Author: Created by bonismo@hotmail.com on 2019-08-22 15:38
 * @Description:
 * @Version: 1.0
 */
public interface TypeConverter {

    /**
     * 判断类型
     *
     * @param clazz 8种基本类型
     * @return true / false
     */
    Boolean isType(Class<?> clazz);

    /**
     * 根据 <property> 标签内的 value 转换成对应类型的值。
     * 该方法用来加载 bean 时，根据 xml 配置信息赋值给 bean 属性
     *
     * @param source <value> 标签内字符
     * @return 对应类型的值，例： String "18" -> Integer 18
     */
    Object convert(String source);
}
