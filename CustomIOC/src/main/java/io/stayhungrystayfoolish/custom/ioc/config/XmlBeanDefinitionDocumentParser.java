package io.stayhungrystayfoolish.custom.ioc.config;

import org.dom4j.Element;

import java.util.List;

/**
 * @Author: Created by bonismo@hotmail.com on 2019-08-22 18:25
 * @Description:
 * @Version: 1.0
 */
public class XmlBeanDefinitionDocumentParser {

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
            } else {
                parseCustomElement(element);
            }
        }
    }

    private void parseCustomElement(Element beanElement) {
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

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    private void parseDefaultElement(Element element) {
    }
}
