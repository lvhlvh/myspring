package com.lvhao.myspring.util;

import com.lvhao.myspring.beans.factory.AnnotatedBeanDefinition;
import com.lvhao.myspring.beans.factory.BeanDefinition;
import com.lvhao.myspring.beans.factory.BeanDefinitionRegistry;
import com.lvhao.myspring.annotation.stereotype.Component;

/**
 * 根据类注解信息生成bean name
 */
public class AnnotationBeanNameGenerator implements BeanNameGenerator {

    @Override
    public String generateBeanName(BeanDefinition bd, BeanDefinitionRegistry registry) {
        if (!(bd instanceof AnnotatedBeanDefinition)) {
            throw new IllegalArgumentException("bd必须是AnnotatedBeanDefinition类型!");
        }

        return determineBeanNameFromAnnotation((AnnotatedBeanDefinition) bd);
    }

    /**
     * 根据@Component注解的值生成bean name (去掉前后空格), 若不存在@Component注解, 默认
     * 使用类名首字母小写。
     */
    private String determineBeanNameFromAnnotation(AnnotatedBeanDefinition abd) {
        Class<?> beanClass = abd.getBeanClass();

        String beanName = null;
        if (beanClass.isAnnotationPresent(Component.class)) {
            Component component = beanClass.getAnnotationsByType(Component.class)[0];
            if (!StringUtils.isEmpty(component.value())) {
                beanName = component.value().trim();
            }
        }

        if (StringUtils.isEmpty(beanName)) {
            beanName = StringUtils.firstLetterLowerCase(beanClass.getSimpleName());
        }

        return beanName;
    }
}
