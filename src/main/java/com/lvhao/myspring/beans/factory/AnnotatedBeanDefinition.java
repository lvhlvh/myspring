package com.lvhao.myspring.beans.factory;

public class AnnotatedBeanDefinition implements BeanDefinition {

    /**
     * 默认的作用域: SINGLETON
     */
    public static final String SCOPE_DEFAULT = BeanDefinition.SCOPE_SINGLETON;

    private Class<?> beanClass;

    private String beanClassName;

    private String scope = SCOPE_DEFAULT;

    public AnnotatedBeanDefinition() {
    }

    public AnnotatedBeanDefinition(Class<?> beanClass) {
        this.beanClass = beanClass;
    }

    public void setBeanClass(Class<?> beanClass) {
        this.beanClass = beanClass;
    }

    public Class<?> getBeanClass() {
        return beanClass;
    }

    public void setBeanClassName(String beanClassName) {
        this.beanClassName = beanClassName;
    }

    public String getBeanClassName() {
        return beanClassName;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getScope() {
        return scope;
    }

    public boolean isSingleton() {
        return scope.equals(BeanDefinition.SCOPE_SINGLETON);
    }

    public boolean isPrototype() {
        return scope.equalsIgnoreCase(BeanDefinition.SCOPE_PROTOTYPE);
    }
}
