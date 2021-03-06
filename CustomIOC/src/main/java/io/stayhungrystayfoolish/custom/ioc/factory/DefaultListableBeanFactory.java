package io.stayhungrystayfoolish.custom.ioc.factory;

import io.stayhungrystayfoolish.custom.ioc.aware.Aware;
import io.stayhungrystayfoolish.custom.ioc.aware.BeanFactoryAware;
import io.stayhungrystayfoolish.custom.ioc.config.*;
import io.stayhungrystayfoolish.custom.ioc.converter.*;
import io.stayhungrystayfoolish.custom.ioc.util.ReflectUtil;

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

    /**
     * 构造参数，通过读取 xml 文件加载 BeanDefinition
     */
    public DefaultListableBeanFactory(String location) {
        registerResources();
        registerTypeConverters();

        // 创建 XmlBeanDefinitionParser 对象，加载 BeanDefinition 信息到 BeanFactory
        XmlBeanDefinitionParser xmlBeanDefinitionParser = new XmlBeanDefinitionParser();

        Resource resource = getResource(location);

        if (null == resource) {
            throw new NullPointerException();
        }
        // 执行加载 xml IO 流
        xmlBeanDefinitionParser.loadBeanDefinitions(this, resource);
    }

    @Override
    public Object getBean(String beanName) {
        Object instance = singletonBeanMap.get(beanName);
        if (null != instance) {
            return instance;
        }
        // 如果单例不存在于工程，则根据 beanName 获取 bean 信息
        BeanDefinition beanDefinition = this.getBeanDefinitionMap().get(beanName);
        String beanClassName = beanDefinition.getBeanClassName();
        // 使用无参构造
        instance = createBeanInstance(beanClassName, null);
        // 注入属性 （DI）
        setInstanceProperty(instance, beanDefinition);
        // 初始化
        initInstance(instance, beanDefinition);
        // 将生成的最终实例存储到 SingletonMap 中
        registerSingletonBean(beanName, instance);
        return instance;
    }

    /**
     * 如果使用有参构造创建 bean，则需要使用 BeanDefinition 中增加一个属性存储标签
     * <constructor-arg index="0" value="Hello Bean Factory!"/>
     * 然后解析 xml 时封装该类，并置于 BeanDefinition 的属性。此模块不再实现。
     */
    @Override
    public Object getBean(String beanName, String... args) {
        return super.getBean(beanName, args);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> List<T> getBeansByType(Class<T> clazz) {
        List<T> list = new ArrayList<>();
        try {
            for (BeanDefinition beanDefinition : beanDefinitionMap.values()) {
                Class<?> clz = Class.forName(beanDefinition.getBeanClassName());
                if (clazz.isAssignableFrom(clz)) {
                    String beanName = beanDefinition.getBeanName();
                    list.add((T) getBean(beanName));
                }
            }
            return list;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<String> getBeanNamesByType(Class<?> type) {
        try {
            List<String> result = new ArrayList<>();
            for (String beanName : beanDefinitionMap.keySet()) {
                BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
                Class<?> clazz = Class.forName(beanDefinition.getBeanClassName());
                if (type.isAssignableFrom(clazz)) {
                    result.add(beanName);
                }
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Map<String, BeanDefinition> getBeanDefinitions() {
        return beanDefinitionMap;
    }

    /**
     * 根据类名（全路径）、构造参数创建类实例
     *
     * @param beanName 类名（全路径）
     * @param args     构造参数
     * @return 类实例
     */
    private Object createBeanInstance(String beanName, Object... args) {
        return ReflectUtil.generateClassInstance(beanName, args);
    }

    /**
     * 根据实例、xml bean 信息封装类进行属性注入（DI）
     *
     * @param instance       bean 实例
     * @param beanDefinition xml bean 封装类
     */
    private void setInstanceProperty(Object instance, BeanDefinition beanDefinition) {
        // 获取属性名、（属性值 + 属性类型）的封装对象
        List<PropertyValue> propertyValues = beanDefinition.getPropertyValues();
        for (PropertyValue propertyValue : propertyValues) {
            String name = propertyValue.getName();
            // value 由两种类型： TypeStringValue 和 RuntimeBeanReference
            Object value = propertyValue.getValue();
            // 声明注入值
            Object injectValue = null;
            if (value instanceof TypeStringValue) {
                TypeStringValue typeStringValue = (TypeStringValue) value;
                String stringValue = typeStringValue.getValue();
                // 获取属性值得类型
                Class<?> targetType = typeStringValue.getTargetType();
                for (TypeConverter converter : typeConverters) {
                    if (converter.isType(targetType)) {
                        injectValue = converter.convert(stringValue);
                    }
                }
            } else if (value instanceof RuntimeBeanReference) {
                RuntimeBeanReference reference = (RuntimeBeanReference) value;
                String ref = reference.getRef();
                injectValue = getBean(ref);
            }
            // 使用反射注入 bean 属性
            ReflectUtil.setFiledValueByFiledName(instance, name, injectValue);
        }
    }

    /**
     * 根据实例、xml bean 信息封装类调用 init-method 方法
     *
     * @param instance       bean 实例
     * @param beanDefinition xml bean 封装类
     */
    private void initInstance(Object instance, BeanDefinition beanDefinition) {
        // 判断 aware 是不是 instance 实例的接口
        // 操作 Aware 接口
        if (instance instanceof Aware) {
            if (instance instanceof BeanFactoryAware) {
                ((BeanFactoryAware) instance).setBeanFactory(this);
            }
        }

        String initMethod = beanDefinition.getInitMethod();
        if (initMethod == null || "".equals(initMethod)) {
            return;
        }
        ReflectUtil.invokeMethod(instance, initMethod);
    }

    /**
     * 将 beanName 和其对应的 BeanDefinition 存入 BeanDefinitionMap 中
     *
     * @param beanName       beanName (id、name、className 三者其中一个)
     * @param beanDefinition xml 文件中 <bean> 标签信息封装类
     */
    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) {
        this.beanDefinitionMap.put(beanName, beanDefinition);
    }

    /**
     * 将 beanName 和其对应的 instance 存入 SingletonMap 中
     *
     * @param beanName beanName (id、name、className 三者其中一个)
     * @param instance bean instance
     */
    public void registerSingletonBean(String beanName, Object instance) {
        this.singletonBeanMap.put(beanName, instance);
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

    public Map<String, BeanDefinition> getBeanDefinitionMap() {
        return beanDefinitionMap;
    }
}
