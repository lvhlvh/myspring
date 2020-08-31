package com.lvhao.myspring.beans.factory;

public interface BeanDefinition {
    /**
     * 单例作用域
     */
    String SCOPE_SINGLETON = "singleton";

    /**
     * 原型作用域
     */
    String SCOPE_PROTOTYPE = "prototype";


    /**
     * 设置bean的Class对象
     */
    void setBeanClass(Class<?> beanClass);

    /**
     * 获取bean的Class对象
     */
    Class<?> getBeanClass();

    /**
     * 设置bean的Class对象的名称
     */
    void setBeanClassName(String beanClassName);

    /**
     * 获取bean的Class对象的名称
     */
    String getBeanClassName();

    /**
     * 设置bean的作用域
     */
    void setScope(String scope);

    /**
     * 获取bean的作用域
     */
    String getScope();

    /**
     * 是单例作用域吗?
     */
    boolean isSingleton();

    /**
     * 是原型作用域吗?
     */
    boolean isPrototype();

}
