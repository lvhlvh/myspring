package com.lvhao.myspring.util;

import com.lvhao.myspring.annotation.context.ComponentScan;
import com.lvhao.myspring.annotation.context.Scope;
import com.lvhao.myspring.annotation.stereotype.Component;
import com.lvhao.myspring.annotation.stereotype.Configuration;
import com.lvhao.myspring.beans.factory.AnnotatedBeanDefinition;
import com.lvhao.myspring.beans.factory.BeanDefinition;
import com.lvhao.myspring.beans.factory.BeanDefinitionRegistry;

import java.util.List;
import java.util.Set;

/**
 * 该类负责解析被注解标记的类，将其转化为BeanDefinition, 以及
 * 注册BeanDefinition到BeanFactory
 */
public class AnnotatedBeanDefinitionReader {

    private final BeanDefinitionRegistry registry;

    private BeanNameGenerator beanNameGenerator = new AnnotationBeanNameGenerator();

    public AnnotatedBeanDefinitionReader(BeanDefinitionRegistry registry) {
        this.registry = registry;
    }

    /**
     * 注册一个Bean，从它的注解上面获取相关的元信息
     */
    public void register(Class<?> beanClass) {
        AnnotatedBeanDefinition bd = new AnnotatedBeanDefinition(beanClass);

        // 生成 beanName
        String beanName = beanNameGenerator.generateBeanName(bd, registry);
        // 获取scope信息
        parseScopeMetadata(beanClass, bd);

        BeanDefinitionReaderUtils.registerBeanDefinition(beanName, bd, registry);
    }

    /**
     * 注册一个或多个Component Bean，从它们的注解上面获取相关的元信息
     */
    public void register(Class<?>... componentClasses) {
        for (Class<?> componentClass : componentClasses) {
            register(componentClass);
        }
    }

    /**
     * 解析配置类的@ComponentScan, 扫描@ComponentScan路径下的bean
     */
    public void scan() {
        List<String> bdNames = registry.getBeanDefinitionNames();
        for (String bdName : bdNames) {
            BeanDefinition bd = registry.getBeanDefinition(bdName);
            Class<?> clazz = bd.getBeanClass();
            if (clazz.isAnnotationPresent(Configuration.class)) {
                ComponentScan[] componentScans = clazz.getAnnotationsByType(ComponentScan.class);
                if (componentScans.length > 0) {
                    ComponentScan componentScan = componentScans[0];
                    String[] basePackages = componentScan.basePackages();
                    for (String basePackage : basePackages) {
                        doScan(basePackage);
                    }
                }
            }
        }
    }

    private void doScan(String basePackage) {
        if (StringUtils.isEmpty(basePackage))
            return;

        basePackage = basePackage.trim();
        Set<Class<?>> classes = ClassUtils.scanClasses(basePackage);
        // 只将被@Component注解标注的类加入bean registry
        for (Class<?> clazz : classes) {
            if (clazz.isAnnotationPresent(Component.class)) {
                AnnotatedBeanDefinition abd = new AnnotatedBeanDefinition(clazz);
                String beanName = beanNameGenerator.generateBeanName(abd, registry);
                if (registry.containsBeanDefinition(beanName)) {
                    continue;
                }
                parseScopeMetadata(clazz, abd);
                BeanDefinitionReaderUtils.registerBeanDefinition(beanName, abd, registry);
            }
        }
    }

    /**
     * 从注解中解析scope信息
     */
    public void parseScopeMetadata(Class<?> beanClass, BeanDefinition beanDefinition) {
        if (beanClass.isAnnotationPresent(Scope.class)) {
            Scope annotationsByType = beanClass.getAnnotationsByType(Scope.class)[0];
            if (annotationsByType.value().equals(BeanDefinition.SCOPE_SINGLETON)) {
                beanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);
            } else if (annotationsByType.value().equals(BeanDefinition.SCOPE_PROTOTYPE)) {
                beanDefinition.setScope(BeanDefinition.SCOPE_PROTOTYPE);
            }
        }
    }
}
