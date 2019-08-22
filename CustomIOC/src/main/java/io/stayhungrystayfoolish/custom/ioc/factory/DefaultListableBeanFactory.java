package io.stayhungrystayfoolish.custom.ioc.factory;

import io.stayhungrystayfoolish.custom.ioc.config.BeanDefinition;
import io.stayhungrystayfoolish.custom.ioc.config.ClassPathResource;
import io.stayhungrystayfoolish.custom.ioc.config.Resource;
import io.stayhungrystayfoolish.custom.ioc.config.XmlBeanDefinitionParser;
import io.stayhungrystayfoolish.custom.ioc.converter.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: Created by bonismo@hotmail.com on 2019-08-22 11:51
 * @Description:
 * @Version: 1.0
 */
public class DefaultListableBeanFactory extends AbstractBeanFactory {

    /**
     * 存储根据 XML 读取信息封装的 BeanDefinition 的集合
     * Key: String beanName
     * Value: 封装的 Bean 信息
     */
    private Map<String, BeanDefinition> beanDefinitionMap = new HashMap<>();

    /**
     * 根据 BeanName 存储 Bean 实例的集合
     * Key: String BeanName
     * Value: Bean 实例
     */
    private Map<String, Object> singletonBeanMap = new HashMap<>();

    /**
     * 存储 XML 相关资源，如： InputStream，用于解析 XML 文件
     */
    private List<Resource> resources = new ArrayList<>();

    /**
     * 存储各种数据类型转换
     */
    private List<TypeConverter> typeConverters = new ArrayList<>();

    public DefaultListableBeanFactory(String location) {
        registerResources();
        registerTypeConverters();

        // 创建 XmlBeanDefinitionParser 对象，加载 BeanDefinition 信息到 BeanFactory
        XmlBeanDefinitionParser xmlBeanDefinitionParser = new XmlBeanDefinitionParser();

        Resource resource = getResource(location);

        if (null == resource) {
            throw new NullPointerException();
        }
    }

    /**
     * 注册资源类
     */
    private void registerResources() {
        resources.add(new ClassPathResource());
    }

    /**
     * 注册 String 和 基本数据类型转换器
     */
    private void registerTypeConverters() {
        typeConverters.add(new StringTypeConverter());
        typeConverters.add(new CharTypeConverter());
        typeConverters.add(new ByteTypeConverter());
        typeConverters.add(new IntegerTypeConverter());
        typeConverters.add(new LongTypeConverter());
        typeConverters.add(new ShortTypeConverter());
        typeConverters.add(new DoubleTypeConverter());
        typeConverters.add(new FloatTypeConverter());
        typeConverters.add(new BooleanTypeConverter());
    }

    private Resource getResource(String location) {
        for (Resource resource : resources) {
            // canRead() 将 location 注册到了 ClassPathResource 中
            if (resource.canRead(location)) {
                return resource;
            }
        }
        return null;
    }

    @Override
    public Object getBean(String beanName) {
        Object instance = singletonBeanMap.get(beanName);
        if (null != instance) {
            return instance;
        }
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        return super.getBean(beanName);
    }
}
