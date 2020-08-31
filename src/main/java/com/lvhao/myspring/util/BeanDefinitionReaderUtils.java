package com.lvhao.myspring.util;

import com.lvhao.myspring.beans.factory.BeanDefinitionRegistry;
import com.lvhao.myspring.beans.factory.BeanDefinition;

/**
 * 注册bean definition的统一接口
 */
public class BeanDefinitionReaderUtils {

    public static void registerBeanDefinition(String beanName, BeanDefinition beanDefinition, BeanDefinitionRegistry registry) {
        registry.registerBeanDefinition(beanName, beanDefinition);
    }
}
