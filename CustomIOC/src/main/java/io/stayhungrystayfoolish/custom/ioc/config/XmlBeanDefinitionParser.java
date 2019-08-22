package io.stayhungrystayfoolish.custom.ioc.config;

import io.stayhungrystayfoolish.custom.ioc.util.DocumentReader;
import org.dom4j.Document;

import java.io.InputStream;

/**
 * @Author: Created by bonismo@hotmail.com on 2019-08-22 17:40
 * @Description: 根据 Resource 加载的 InputStream 解析 XML 信息封装为 BeanDefinition 最终加载到 BeanFactory
 * @Version: 1.0
 */
public class XmlBeanDefinitionParser {

    public void loadBeanDefinitions(Resource resource) {
        InputStream inputStream = resource.getInputStream();
        Document document = DocumentReader.createDocument(inputStream);
        XmlBeanDefinitionDocumentParser documentParser = new XmlBeanDefinitionDocumentParser();
        documentParser.loadBeanDefinitions(document.getRootElement());
    }
}
