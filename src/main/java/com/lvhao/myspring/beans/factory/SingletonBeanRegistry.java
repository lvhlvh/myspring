package com.lvhao.myspring.beans.factory;

/**
 * 单例bean工厂
 */
public interface SingletonBeanRegistry {

    /**
     * 根据beanName获取单例bean对象
     */
    Object getSingleton(String beanName);

    /**
     * bean工厂中是否包含beanName对应的单例对象
     */
    boolean containsSingleton(String beanName);
}
