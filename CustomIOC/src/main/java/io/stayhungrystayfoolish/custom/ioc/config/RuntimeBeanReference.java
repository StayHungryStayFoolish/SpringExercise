package io.stayhungrystayfoolish.custom.ioc.config;

/**
 * @Author: bonismo@hotmail.com
 * @Description:
 * @Date: 2019-08-22 23:49
 * @Version: V1.0
 */
public class RuntimeBeanReference {

    private String ref;

    public RuntimeBeanReference(String ref) {
        this.ref = ref;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }
}
