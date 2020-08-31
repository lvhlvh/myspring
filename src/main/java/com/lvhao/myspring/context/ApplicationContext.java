package com.lvhao.myspring.context;

import com.lvhao.myspring.beans.factory.BeanFactory;

/**
 * Application Context: 应该比BeanFactory多一些额外功能, 但暂时没用到
 */
public interface ApplicationContext extends BeanFactory {

    /**
     * 获取Bean工厂, 仿照Spring ApplicationContext的etAutowireCapableBeanFactory方法
     */
    BeanFactory getBeanFactory();
}
