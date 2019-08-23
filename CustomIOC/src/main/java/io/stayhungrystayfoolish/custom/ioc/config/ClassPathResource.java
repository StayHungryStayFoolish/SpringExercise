package io.stayhungrystayfoolish.custom.ioc.config;

import java.io.InputStream;

/**
 * @Author: Created by bonismo@hotmail.com on 2019-08-22 15:12
 * @Description: 简单处理 XML 路径。1.判断是否可读 2.获取 InputStream
 * @Version: 1.0
 */
public class ClassPathResource implements Resource {

    private String location;

    private static final String CLASSPATH = "classpath:";


    @Override
    public Boolean canRead(String location) {
        if (null == location || "".equals(location)) {
            return false;
        }
        // 将 location 注册到了 ClassPathResource 中
        if (location.startsWith(CLASSPATH)) {
            this.location = location;
            return true;
        }
        return false;
    }

    @Override
    public InputStream getInputStream() {
        if (null == location || "".equals(location)) {
            return null;
        }
        location = location.replace(CLASSPATH, "");
        return this.getClass().getClassLoader().getResourceAsStream(location);
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public static String getCLASSPATH() {
        return CLASSPATH;
    }
}
