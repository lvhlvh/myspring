package com.lvhao.myspring.util;

import com.lvhao.myspring.beans.factory.BeanDefinition;
import com.lvhao.myspring.beans.factory.BeanDefinitionRegistry;

/**
 * bean名称生成器
 */
public interface BeanNameGenerator {

    String generateBeanName(BeanDefinition definition, BeanDefinitionRegistry registry);
}
