package io.stayhungrystayfoolish.custom.ioc.config;

import java.io.InputStream;

/**
 * @Author: Created by bonismo@hotmail.com on 2019-08-22 15:01
 * @Description: 封装一个 InputStream 用于 XML 解析使用
 * @Version: 1.0
 */
public interface Resource {

    /**
     * 判断配置文件是否可读
     * @param location XML 配置文件
     * @return true / false
     */
    Boolean canRead(String location);

    /**
     * 封装一个 InputStream
     * @return InputStream
     */
    InputStream getInputStream();
}
