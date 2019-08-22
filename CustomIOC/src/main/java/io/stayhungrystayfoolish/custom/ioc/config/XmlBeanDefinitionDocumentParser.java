package io.stayhungrystayfoolish.custom.ioc.config;

import io.stayhungrystayfoolish.custom.ioc.factory.DefaultListableBeanFactory;
import io.stayhungrystayfoolish.custom.ioc.util.ReflectUtil;
import org.dom4j.Element;

import java.util.List;

/**
 * @Author: Created by bonismo@hotmail.com on 2019-08-22 18:25
 * @Description:
 * @Version: 1.0
 */
public class XmlBeanDefinitionDocumentParser {

    private DefaultListableBeanFactory beanFactory;

    public XmlBeanDefinitionDocumentParser(DefaultListableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    /**
     * 解析 XML 标签信息
     *
     * @param rootElement <beans>
     */
    public void loadBeanDefinitions(Element rootElement) {
        List<Element> elementList = rootElement.elements();
        for (Element element : elementList) {
            String name = element.getName();
            if ("bean".equalsIgnoreCase(name)) {
                parseDefaultElement(element);
            }
        }
    }

    private void parseDefaultElement(Element beanElement) {
        if (null == beanElement) {
            return;
        }
        try {
            // 定义 bean name
            String beanName = null;
            // bean id 属性
            String id = beanElement.attributeValue("id");
            // bean name 属性
            String name = beanElement.attributeValue("name");
            // bean class 属性
            String clazz = beanElement.attributeValue("class");
            // bean init-method 属性
            String initMethod = beanElement.attributeValue("init-method");
            // 根据 class 全路径获取 Class 对象
            Class<?> clazzType = Class.forName(clazz);
            // 根据 id 或 name 获取 beanName
            beanName = id == null ? name : id;
            // 再次确定 beanName
            beanName = beanName == null ? clazzType.getSimpleName() : beanName;
            // 创建 BeanDefinition 对象
            BeanDefinition beanDefinition = new BeanDefinition(beanName, clazz);
            beanDefinition.setInitMethod(initMethod);
            // 解析 <property> 标签列，封装到 BeanDefinition 中
            // beanElement.elements() 获取当前标签的子元素
            List<Element> propertyValues = beanElement.elements();
            for (Element propertyValue : propertyValues) {
                parsePropertyElement(beanDefinition, propertyValue);
            }
            registerBeanDefinition(beanName, beanDefinition);

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    /**
     * 调用 DefaultListableFactory 注册 bean 信息到 Map 集合
     * @param beanName bean name
     * @param beanDefinition xml <bean> 标签内信息的封装类
     */
    private void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) {
        this.beanFactory.registerBeanDefinition(beanName, beanDefinition);
    }

    /**
     * 解析 <bean> 标签内的 <property name="" , value =""> / <property name="" , ref =""> 标签集
     * @param beanDefinition xml 信息封装类
     * @param propertyValue <property> 标签集
     */
    private void parsePropertyElement(BeanDefinition beanDefinition, Element propertyValue) {
        if (null == propertyValue) {
            return;
        }
        String name = propertyValue.attributeValue("name");
        String value = propertyValue.attributeValue("value");
        String ref = propertyValue.attributeValue("ref");

        // 如果 value 和 ref 都有值，则配置错误，直接返回
        if (("".equals(value)) && ("".equals(ref))) {
            return;
        }

        // 创建属性值对象，进行封装
        PropertyValue pv = null;

        if (null != value && !"".equals(value)) {
            // 创建属性类型、属性值得封装类 TypeStringValue
            TypeStringValue typeStringValue = new TypeStringValue(value);
            // 根据类名、属性名获取属性类型
            Class<?> targetType = ReflectUtil.getFileTypeByFileName(beanDefinition.getBeanClassName(), name);
            typeStringValue.setTargetType(targetType);
            pv = new PropertyValue(name, typeStringValue);
            // 根据 Bean 对象的属性名和属性值封装到 BeanDefinition
            beanDefinition.addPropertyValues(pv);
        } else if (null != ref && !"".equals(ref)) {
            RuntimeBeanReference reference = new RuntimeBeanReference(ref);
            // ref 目前作为 String 存储，再 DefaultListableBeanFactory.getBean() 时会通过反射获取该类的实例
            pv = new PropertyValue(name, ref);
            beanDefinition.addPropertyValues(pv);
        }
    }
}
