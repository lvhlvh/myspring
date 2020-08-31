package com.lvhao.myspring.beans.factory;

import com.lvhao.myspring.exception.BeanCurrentlyInCreationException;
import com.lvhao.myspring.util.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultSingletonBeanRegistry implements SingletonBeanRegistry {

    /**
     * 当前正在创建中的单例对象
     */
    private final Set<String> singletonsCurrentlyInCreation =
            Collections.newSetFromMap(new ConcurrentHashMap<>(16));

    /**
     * 一级缓存，存放完备的bean对象
     */
    private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>();

    /**
     * 二级缓存，存放提前暴露出来的bean对象
     */
    private final Map<String, Object> earlySingletonObjects = new HashMap<>();

    /**
     * 三级缓存，存放暴露非完备bean对象的ObjectFactory
     */
    private final Map<String, ObjectFactory<?>> singletonObjectFactories = new HashMap<>();

    /**
     * 存放实现了DisposableBean接口的bean
     */
    protected final Map<String, Object> disposableBeans = new HashMap<>();


    @Override
    public Object getSingleton(String beanName) {
        return getSingleton(beanName, true);
    }

    @Override
    public boolean containsSingleton(String beanName) {
        return this.singletonObjects.containsKey(beanName);
    }

    /**
     * 获取单例对象: 依次尝试从一、二、三级缓存获取
     */
    public Object getSingleton(String beanName, boolean allowEarlyReference) {
        Object singletonObject = this.singletonObjects.get(beanName);
        if (singletonObject == null && isSingletonCurrentlyInCreation(beanName)) {
            synchronized (this.singletonObjects) {
                singletonObject = this.earlySingletonObjects.get(beanName);
                if (singletonObject == null && allowEarlyReference) {
                    ObjectFactory<?> singletonFactory = singletonObjectFactories.get(beanName);
                    if (singletonFactory != null) {
                        singletonObject = singletonFactory.getObject();
                        this.earlySingletonObjects.put(beanName, singletonObject);
                        this.singletonObjectFactories.remove(beanName);
                    }
                }
            }
        }

        return singletonObject;
    }

    /**
     * 创建单例对象: 核心逻辑在调用singletonFactory参数的getObject方法
     */
    public Object getSingleton(String beanName, ObjectFactory<?> singletonFactory) {
        synchronized (this.singletonObjects) {
            // 1. 先尝试从一级缓存获取
            Object singletonObject = this.singletonObjects.get(beanName);
            // 2. 一级缓存不存在, 尝试手动创建
            if (singletonObject == null) {
                // 2.1 创建前的前置操作
                beforeSingletonCreation(beanName);

                // 2.2 创建
                singletonObject = singletonFactory.getObject();

                // 2.3 添加到一级缓存, 从二/三级缓存移除
                addSingleton(beanName, singletonObject);
            }

            return singletonObject;
        }
    }

    public void addSingleton(String beanName, Object singletonObject) {
        synchronized (this.singletonObjects) {
            this.singletonObjects.put(beanName, singletonObject);
            this.singletonObjectFactories.remove(beanName);
            this.earlySingletonObjects.remove(beanName);
        }
    }

    private void beforeSingletonCreation(String beanName) {
        if (!this.singletonsCurrentlyInCreation.add(beanName)) {
            throw new BeanCurrentlyInCreationException("Error in creating bean, possibly circular reference: " + beanName);
        }
    }

    /**
     * 创建单例对象前的前置操作: 将bean对象标记为已创建
     */
    public boolean isSingletonCurrentlyInCreation(String beanName) {
        return singletonsCurrentlyInCreation.contains(beanName);
    }

    protected void addSingletonFactory(String beanName, ObjectFactory<?> singletonFactory) {
        synchronized (this.singletonObjects) {
            if (!this.singletonObjects.containsKey(beanName)) {
                this.singletonObjectFactories.put(beanName, singletonFactory);
                this.earlySingletonObjects.remove(beanName);
            }
        }
    }

    public void destroySingletons() {
        String[] disposableBeanNames;
        synchronized (this.disposableBeans) {
            disposableBeanNames = StringUtils.toStringArray(disposableBeans.keySet());
        }

        for (int i = disposableBeanNames.length - 1; i >= 0; i--) {
            destroySingleton(disposableBeanNames[i]);
        }
    }

    public void destroySingleton(String beanName) {
        // 从单例缓存中移除
        removeSingleton(beanName);

        // 从disposableBeans中移除
        Object disposableBean;
        synchronized (this.disposableBeans) {
            disposableBean = this.disposableBeans.remove(beanName);
        }

        // 调用destroy方法
        destroyBean(disposableBean, beanName);
    }

    protected void destroyBean(Object bean, String beanName) {
        try {
            ((DisposableBean) bean).destroy();
        } catch (Exception e) {
            throw new RuntimeException("destroy bean " + beanName + "failed");
        }
    }

    protected void removeSingleton(String beanName) {
        synchronized (this.singletonObjects) {
            this.singletonObjects.remove(beanName);
            this.singletonObjectFactories.remove(beanName);
            this.earlySingletonObjects.remove(beanName);
        }
    }
}
