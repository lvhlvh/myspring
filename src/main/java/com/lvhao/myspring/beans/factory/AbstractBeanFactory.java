package com.lvhao.myspring.beans.factory;

/**
 * 提供了对BeanFactory的默认实现, 并且继承了DefaultSingletonBeanRegistry
 */
public abstract class AbstractBeanFactory extends DefaultSingletonBeanRegistry
        implements BeanFactory {


    @Override
    public Object getBean(String name) {
        return doGetBean(name);
    }

    protected Object doGetBean(String beanName) {
        Object bean;

        // 1. 先尝试从三级缓存获取
        bean = getSingleton(beanName, true);
        if (bean != null) {
            return bean;
        }


        if (!(getBeanDefinition(beanName) instanceof AnnotatedBeanDefinition)) {
            throw new RuntimeException("can cast to AnnotatedBeanDefinition");
        }

        // 2. 一级缓存不存在, 尝试创建
        final AnnotatedBeanDefinition abd = (AnnotatedBeanDefinition) getBeanDefinition(beanName);
        if (abd.isSingleton()) {
            bean = getSingleton(beanName, () -> createBean(beanName, abd));
        } else if (abd.isPrototype()) {
            bean = createBean(beanName, abd);
        }


        return bean;
    }

    protected abstract boolean containsBeanDefinition(String beanName);

    /**
     * 获取beanName对应的bean definition
     */
    protected abstract BeanDefinition getBeanDefinition(String beanName);

    /**
     * 创建bean对象
     */
    protected abstract Object createBean(String beanName, BeanDefinition beanDefinition);

}
