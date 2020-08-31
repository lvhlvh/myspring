package com.lvhao.myspring.context;

import com.lvhao.myspring.beans.factory.*;
import com.lvhao.myspring.util.AnnotatedBeanDefinitionReader;

import java.util.List;

public class AnnotationConfigApplicationContext extends AbstractApplicationContext
        implements BeanDefinitionRegistry {

    private DefaultListableBeanFactory beanFactory;

    private AnnotatedBeanDefinitionReader reader;

    public AnnotationConfigApplicationContext() {
        beanFactory = new DefaultListableBeanFactory();
        reader = new AnnotatedBeanDefinitionReader(this);
    }

    /**
     * 传入并解析配置类 (@Configuration 和 @Component)
     */
    public AnnotationConfigApplicationContext(Class<?>... componentClasses) {
        this();
        register(componentClasses);
        refresh();
    }

    public void register(Class<?>... componentClasses) {
        this.reader.register(componentClasses);
    }

    public void refresh() {
        synchronized (this.startupShutdownMonitor) {
            // 1. check initialized
            checkAlreadyRefreshed();

            // 2. 解析@Configuration类
            reader.scan();

            // 3. 初始化单例对象
            beanFactory.preInstantiateSingletons();
        }
    }

    public void close() {
        synchronized (this.startupShutdownMonitor) {
            ((DefaultSingletonBeanRegistry) getBeanFactory()).destroySingletons();
        }
    }


    private void checkAlreadyRefreshed() {
        if (!this.refreshed.compareAndSet(false, true)) {
            throw new IllegalStateException("ApplicationContext cannot be refreshed multiple times");
        }
    }

    public void scan() {
        reader.scan();
    }


    @Override
    public BeanFactory getBeanFactory() {
        return this.beanFactory;
    }

    @Override
    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) {
        this.beanFactory.registerBeanDefinition(beanName, beanDefinition);
    }

    @Override
    public BeanDefinition getBeanDefinition(String beanName) {
        return this.beanFactory.getBeanDefinition(beanName);
    }

    @Override
    public boolean containsBeanDefinition(String beanName) {
        return this.beanFactory.containsBeanDefinition(beanName);
    }

    @Override
    public List<String> getBeanDefinitionNames() {
        return this.beanFactory.getBeanDefinitionNames();
    }

}
