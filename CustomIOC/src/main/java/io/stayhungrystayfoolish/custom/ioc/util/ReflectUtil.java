package io.stayhungrystayfoolish.custom.ioc.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @Author: Created by bonismo@hotmail.com on 2019-08-22 20:07
 * @Description: 反射工具类
 * @Version: 1.0
 */
public class ReflectUtil {

    /**
     * 根据类名、入参参数生成类实例
     *
     * @param beanClassName 类名
     * @param args          入参参数
     * @return 类实例
     */
    public static Object generateClassInstance(String beanClassName, Object... args) {
        try {
            // 根据类型获取类
            Class<?> clazz = Class.forName(beanClassName);
            // 获取构造器列表
            Constructor[] constructors = clazz.getDeclaredConstructors();
            // 传入构造器参数数量
            int paramCount = 0;
            if (null != args) {
                paramCount = args.length;
            }
            for (Constructor constructor : constructors) {
                // 获取构造器参数数组
                Class<?>[] constructorParameterTypes = constructor.getParameterTypes();
                // 构造器参数数量
                int constructorParamCount = constructorParameterTypes.length;
                // 传入参数 与 构造器参数类型是否一致
                boolean paramTypeCoincide = false;
                // 当传入参数数量与构造器参数数量相等时，进入判断，按次序判断参数类型是否一致
                // 当入参参数不等于 0 且构造器参数也不等于 0 时才进入判断，有一个等于 0 时直接生成实例
                if (0 != paramCount && 0 != constructorParamCount) {
                    if ((paramCount == constructorParamCount)) {
                        // 遍历构造器参数类型
                        for (Class<?> constructorParameterType : constructorParameterTypes) {
                            // 遍历入参参数
                            for (Object arg : args) {
                                // 如果入参参数类型与构造器参数类型一致，设为 true
                                paramTypeCoincide = constructorParameterType == arg.getClass();
                            }
                        }
                        // 当参数类型一致时，通过构造器生成实例
                        if (paramTypeCoincide) {
                            return constructor.newInstance(args);
                        }
                    }
                } else {
                    return constructor.newInstance(null);
                }
            }
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据类实例、属性名将属性值注入
     *
     * @param beanInstance 类实例
     * @param fieldName    属性名
     * @param fieldValue   属性值
     */
    public static void setFiledValueByFiledName(Object beanInstance, String fieldName, Object fieldValue) {
        try {
            Class<?> clazz = beanInstance.getClass();
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(beanInstance, fieldValue);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据类名、属性名获取属性类型
     *
     * @param beanClassName 类名
     * @param fieldName     属性名
     * @return 属性类型
     */
    public static Class<?> getFileTypeByFileName(String beanClassName, String fieldName) {
        try {
            Class<?> clazz = Class.forName(beanClassName);
            Field field = clazz.getDeclaredField(fieldName);
            return field.getType();
        } catch (ClassNotFoundException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据类实例、方法名调用该方法
     *
     * @param beanInstance 类实例
     * @param methodName   方法名
     */
    public static void invokeMethod(Object beanInstance, String methodName) {
        try {
            Class<?> clazz = beanInstance.getClass();
            Method method = clazz.getDeclaredMethod(methodName);
            method.setAccessible(true);
            method.invoke(beanInstance);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}


