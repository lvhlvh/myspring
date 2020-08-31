package com.lvhao.myspring.beans.factory;

import com.lvhao.myspring.annotation.inject.Autowired;
import com.lvhao.myspring.exception.BeanCreationException;
import com.lvhao.myspring.util.ConstructorResolver;
import com.lvhao.myspring.util.MethodArguments;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractAutowireCapableBeanFactory extends AbstractBeanFactory
        implements AutowireCapableBeanFactory {

    /**
     * 是否允许循环依赖
     */
    private boolean allowCircularReferences = true;

    public AbstractAutowireCapableBeanFactory() {
    }

    @Override
    protected Object createBean(String beanName, BeanDefinition bd) {
        return doCreateBean(beanName, bd);
    }

    protected Object doCreateBean(String beanName, BeanDefinition bd) {
        // 1. 创建
        final Object bean = createBeanInstance(beanName, bd);

        // 2. 提早暴露到三级缓存
        boolean earlySingletonExposure = bd.isSingleton() && isSingletonCurrentlyInCreation(beanName);
        if (earlySingletonExposure) {
            addSingletonFactory(beanName, () -> bean);
        }

        // 3. 属性填充
        populateBean(beanName, bd, bean);

        // 4. 初始化
        Object retBean = initializeBean(bean);

        // 5. 处理销毁方法
        registerDisposableBeanIfNecessary(beanName, bean);

        return retBean;
    }

    private void registerDisposableBeanIfNecessary(String beanName, Object bean) {
        if (bean instanceof DisposableBean) {
            synchronized (this.disposableBeans) {
                this.disposableBeans.put(beanName, bean);
            }
        }
    }

    private Object initializeBean(Object bean) {
        if (bean instanceof InitializingBean) {
            try {
                ((InitializingBean) bean).afterPropertiesSet();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return bean;
    }

    protected Object createBeanInstance(String beanName, BeanDefinition bd) {
        Class<?> beanClass = bd.getBeanClass();

        Constructor<?>[] ctors = determineConstructors(beanClass, beanName);
        if (ctors != null) {
            return autowireConstructor(beanName, bd, ctors);
        }

        return instantiateBean(beanName, bd);
    }

    protected Object autowireConstructor(String beanName, BeanDefinition bd, Constructor<?>[] chosenCtors) {
        return new ConstructorResolver(this).autowireConstructor(beanName, bd, chosenCtors);
    }

    /**
     * 尝试调用默认构造器创建对象，不存在默认构造器会抛异常
     */
    protected Object instantiateBean(final String beanName, final BeanDefinition bd) {
        Class<?> beanClass = bd.getBeanClass();
        try {
            Constructor<?> defaultCons = beanClass.getDeclaredConstructor();
            defaultCons.setAccessible(true);
            return defaultCons.newInstance();
        } catch (Throwable e) {
            throw new BeanCreationException("Error in instantiating bean: " + beanName);
        }
    }

    /**
     * 顾名思义, 该方法返回candidate constructors (@Autowired + (可选) 无参构造器). (下面不考虑 @Lookup 注解)
     * <p>
     * - 如果不存在被@Autowired标注的构造器:
     * - 如果只有一个构造器, 且不是无参的, 则返回该构造器
     * - 如果只有一个构造器, 且是无参的, 则返回null
     * - 如果不止一个构造器, 则返回null
     * - 如果存在唯一一个@Autowired(required = true)的构造器, 则返回这个唯一的构造器
     * - 如果存在多个@Autowired(required = false)的构造器, 则返回这些构造器, 外加默认无参构造器
     * <p>
     * 总结起来, 该方法:
     * - 要么返回唯一一个被@Autowired(required = true)标注的构造器
     * - 要么返回若干个被@Autowired(required = false)标注的构造器  + 默认无参构造器(如果有的话)
     * - 要么返回唯一一个非无参构造器
     */
    protected Constructor<?>[] determineConstructors(Class<?> beanClass, String beanName) {
        Constructor<?>[] candidateConstructors;

        Constructor<?>[] rawCandidates = beanClass.getDeclaredConstructors();
        List<Constructor<?>> candidates = new ArrayList<>();
        // @Autowired(required = true)
        Constructor<?> requiredConstructor = null;
        // 无参构造器
        Constructor<?> defaultConstructor = null;

        for (Constructor<?> candidate : rawCandidates) {
            boolean hasAutowired = candidate.isAnnotationPresent(Autowired.class);
            if (hasAutowired) {
                Autowired autowired = candidate.getAnnotation(Autowired.class);
                // 获取required属性
                boolean required = autowired.required();
                if (required) {
                    // required只能有一个
                    if (!candidates.isEmpty()) {
                        throw new BeanCreationException("Error in creating bean: " + beanName);
                    }
                    requiredConstructor = candidate;
                }

                candidates.add(candidate);
            } else if (candidate.getParameterCount() == 0) {
                // 没加@Autowired, 但是是无参构造器
                defaultConstructor = candidate;
            }
        }


        if (!candidates.isEmpty()) {
            if (requiredConstructor == null) {
                if (defaultConstructor != null) {
                    candidates.add(defaultConstructor);
                }
            }
            candidateConstructors = candidates.toArray(new Constructor<?>[0]);
        } else if (rawCandidates.length == 1 && rawCandidates[0].getParameterCount() > 1) {
            candidateConstructors = new Constructor<?>[]{rawCandidates[0]};
        } else {
            candidateConstructors = new Constructor<?>[0];
        }

        return (candidateConstructors.length > 0 ? candidateConstructors : null);
    }

    /**
     * 对bean进行属性填充, 为了简单起见，这里仅仅调用被@Autowired标记的方法
     */
    protected void populateBean(String beanName, BeanDefinition bd, Object bean) {
        Class<?> beanClass = bean.getClass();

        List<Method> candidates = new ArrayList<>();

        // 寻找存在@Autowired注解的方法
        Method[] rawMethodCandidates = beanClass.getDeclaredMethods();
        for (Method candidate : rawMethodCandidates) {
            if (candidate.isAnnotationPresent(Autowired.class)) {
                candidates.add(candidate);
            }
        }

        // 遍历@Autowired注解标注的方法，进行依赖填充
        for (Method candidate : candidates) {
            MethodArguments arguments = null;
            // 尝试解析参数, 如果解析过程中抛出异常, 则说明存在不被满足的依赖, 跳过该方法
            try {
                arguments = resolveMethodArguments(candidate);
            } catch (BeanCreationException e) {
                continue;
            }

            Object[] methodArgumentValues = arguments.getMethodArgumentValues();
            candidate.setAccessible(true);
            try {
                candidate.invoke(bean, methodArgumentValues);
            } catch (Throwable e) {
                throw new BeanCreationException("Error in populating bean: " + beanName);
            }

        }
    }

    /**
     * 解析方法的参数
     */
    private MethodArguments resolveMethodArguments(Method candidate)
            throws BeanCreationException {
        List<MethodArguments.ArgumentHolder> methodArguments = new ArrayList<>();

        Parameter[] parameters = candidate.getParameters();
        int argIndex = 0;
        for (Parameter parameter : parameters) {
            String argName = parameter.getName();
            Class<?> argType = parameter.getType();
            Object argValue = null;

            Map<String, Object> autowireCandidates = findAutowireCandidate(argType, argName);
            if (autowireCandidates.isEmpty()) {
                throw new BeanCreationException("Error in resolve method dependency: " + candidate.getName());
            }
            // 只会循环一次
            for (String beanName : autowireCandidates.keySet()) {
                argValue = (autowireCandidates.get(beanName) instanceof Class) ?
                        getBean(beanName) : autowireCandidates.get(beanName);
            }

            methodArguments.add(new MethodArguments.ArgumentHolder(argIndex, argName, argType, argValue));
            argIndex++;
        }

        return new MethodArguments(methodArguments);
    }

    /**
     * 寻找bean工厂中满足参数条件的bean实例 (暂时仅考虑单例, 且假设不会有同类型不会重复, 因此返回的map中最多只有一个)
     */
    public abstract Map<String, Object> findAutowireCandidate(Class<?> type, String beanName);
}
