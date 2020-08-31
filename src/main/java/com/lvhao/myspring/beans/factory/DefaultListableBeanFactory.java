package com.lvhao.myspring.beans.factory;

import com.lvhao.myspring.exception.NoSuchBeanDefinitionException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultListableBeanFactory extends AbstractAutowireCapableBeanFactory
        implements BeanDefinitionRegistry {

    private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();

    private final List<String> beanDefinitionNames = new ArrayList<>(256);

    @Override
    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) {
        this.beanDefinitionNames.add(beanName);
        this.beanDefinitionMap.put(beanName, beanDefinition);
    }

    @Override
    public BeanDefinition getBeanDefinition(String beanName) {
        BeanDefinition bd = this.beanDefinitionMap.get(beanName);
        if (bd == null) {
            throw new NoSuchBeanDefinitionException("bean definition of " + beanName
                    + " does not exist");
        }
        return bd;
    }

    @Override
    public boolean containsBeanDefinition(String beanName) {
        return this.beanDefinitionMap.containsKey(beanName);
    }

    @Override
    public List<String> getBeanDefinitionNames() {
        return new ArrayList<>(this.beanDefinitionNames);
    }


    @Override
    public Map<String, Object> findAutowireCandidate(Class<?> type, String beanName) {
        Map<String, Object> candidate = new HashMap<>();

        // TODO: 下面仅仅考虑了单例的情况
        for (String beanDefinitionName : this.beanDefinitionNames) {
            BeanDefinition bd = this.beanDefinitionMap.get(beanDefinitionName);
            Class<?> beanClass = bd.getBeanClass();
            boolean compatible = type.isAssignableFrom(beanClass);
            if (compatible) {
                Object val = containsSingleton(beanDefinitionName) ? getSingleton(beanDefinitionName) : beanClass;
                candidate.put(beanDefinitionName, val);
            }
        }
        return candidate;
    }

    /**
     * 容器启动时, 对单例对象进行创建和初始化
     */
    public void preInstantiateSingletons() {
        List<String> beanNames = new ArrayList<>(beanDefinitionNames);

        for (String beanName : beanNames) {
            BeanDefinition beanDefinition = getBeanDefinition(beanName);
            if (beanDefinition.isSingleton()) {
                getBean(beanName);
            }
        }
    }
}
