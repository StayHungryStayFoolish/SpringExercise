package io.stayhungrystayfoolish.custom.ioc.util;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

import java.io.InputStream;
import java.io.Reader;

/**
 * @Author: Created by bonismo@hotmail.com on 2019-08-22 18:05
 * @Description: 使用 dom4j 插件读取配置 xml 文件
 * @Version: 1.0
 */
public class DocumentReader {

    /**
     * 使用字节流读取文件
     * @param in inputStream
     * @return Document
     */
    public static Document createDocument(InputStream in) {
        Document document = null;
        try {
            SAXReader reader = new SAXReader();
            document = reader.read(in);
            return document;
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 使用字符流读取文件
     * @param in reader
     * @return Document
     */
    public static Document createDocument(Reader in) {
        Document document = null;
        try {
            SAXReader reader = new SAXReader();
            document = reader.read(in);
            return document;
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return null;
    }
}
