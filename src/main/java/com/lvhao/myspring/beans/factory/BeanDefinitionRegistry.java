package com.lvhao.myspring.beans.factory;

import java.util.List;

/**
 * 负责管理bean definition的registry
 */
public interface BeanDefinitionRegistry {

    void registerBeanDefinition(String beanName, BeanDefinition beanDefinition);

    BeanDefinition getBeanDefinition(String beanName);

    boolean containsBeanDefinition(String beanName);

    /**
     * 返回registry中所有的bean definition name
     * @return
     */
    List<String> getBeanDefinitionNames();
}
