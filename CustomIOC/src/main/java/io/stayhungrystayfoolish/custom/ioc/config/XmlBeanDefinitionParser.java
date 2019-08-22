package io.stayhungrystayfoolish.custom.ioc.config;

import io.stayhungrystayfoolish.custom.ioc.factory.DefaultListableBeanFactory;
import io.stayhungrystayfoolish.custom.ioc.util.DocumentReader;
import org.dom4j.Document;

import java.io.InputStream;

/**
 * @Author: Created by bonismo@hotmail.com on 2019-08-22 17:40
 * @Description: 根据 Resource 加载的 InputStream 解析 XML 信息封装为 BeanDefinition 最终加载到 BeanFactory
 * @Version: 1.0
 */
public class XmlBeanDefinitionParser {

    /**
     * 加载 XML 资源，最终由 XmlBeanDefinitionDocumentParser 解析封装进 BeanDefinition 中，并注册到 DefaultListableFactory 中。
     * @param beanFactory DefaultListableFactory
     * @param resource resource（获取 xml 路径的 IO ）
     */
    public void loadBeanDefinitions(DefaultListableBeanFactory beanFactory, Resource resource) {
        InputStream inputStream = resource.getInputStream();
        Document document = DocumentReader.createDocument(inputStream);
        XmlBeanDefinitionDocumentParser documentParser = new XmlBeanDefinitionDocumentParser(beanFactory);
        documentParser.loadBeanDefinitions(document.getRootElement());
    }
}
