package io.stayhungrystayfoolish.custom.ioc.aware;

import io.stayhungrystayfoolish.custom.ioc.factory.BeanFactory;

/**
 * @Author: Created by bonismo@hotmail.com on 2019-10-14 17:21
 * @Description:
 * @Version: 1.0
 */
public interface BeanFactoryAware extends Aware {

    /**
     * 设置 BeanFactory
     * @param beanFactory beanFactory
     */
    void setBeanFactory(BeanFactory beanFactory);
}
