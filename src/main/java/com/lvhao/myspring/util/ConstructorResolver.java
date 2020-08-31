package com.lvhao.myspring.util;

import com.lvhao.myspring.beans.factory.AbstractAutowireCapableBeanFactory;
import com.lvhao.myspring.beans.factory.AutowireCapableBeanFactory;
import com.lvhao.myspring.beans.factory.BeanDefinition;
import com.lvhao.myspring.beans.factory.DefaultListableBeanFactory;
import com.lvhao.myspring.exception.BeanCreationException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 负责构造器及其参数的解析
 */
public class ConstructorResolver {

    private final AbstractAutowireCapableBeanFactory beanFactory;

    public ConstructorResolver(AbstractAutowireCapableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    /**
     * 选取何时的构造器创建bean对象并返回
     */
    public Object autowireConstructor(String beanName, BeanDefinition bd, Constructor<?>[] chosenCtors) {
        // TODO: 完善逻辑

        Constructor<?>[] candidates = chosenCtors;
        // candidates为null的情况(在本项目中, 参数chosenCtors不可能是null)
        if (candidates == null) {
            Class<?> beanClass = bd.getBeanClass();
            candidates = beanClass.getDeclaredConstructors();
        }

        // ------- 只有一个构造器且是无参的, 不需要参数解析 ---------------

        if (candidates.length == 1 && candidates[0].getParameterCount() == 0) {
            try {
                candidates[0].setAccessible(true);
                return candidates[0].newInstance();
            } catch (Throwable e) {
                throw new BeanCreationException("autowireConstructor(): Error in invoking no-arg constructor for bean " + beanName);
            }
        }


        // ---------- 需要参数解析 ------------

        Object bean = null;

        // 对构造器进行排序: 先访问权限, 再参数个数
        AutowireUtils.sortConstructors(candidates);

        for (Constructor<?> candidate : candidates) {
            MethodArguments arguments = null;
            // 尝试获取构造器参数的依赖, 若存在不被满足的依赖, 则跳过该构造器
            try {
                arguments = resolveConstructorArguments(candidate);
            } catch (BeanCreationException e) {
                continue;
            }

            Object[] argumentValues = arguments.getMethodArgumentValues();
            candidate.setAccessible(true);
            try {
                bean = candidate.newInstance(argumentValues);
                break;
            } catch (Throwable e) {
                throw new BeanCreationException("Error in instantiating bean: " + beanName);
            }
        }

        if (bean == null) {
            throw new BeanCreationException("No proper constructors can be invoked: " + beanName);
        }

        return bean;
    }

    private MethodArguments resolveConstructorArguments(Constructor<?> candidate)
            throws BeanCreationException {
        List<MethodArguments.ArgumentHolder> methodArguments = new ArrayList<>();

        Parameter[] parameters = candidate.getParameters();
        int argIndex = 0;
        for (Parameter parameter : parameters) {
            String argName = parameter.getName();
            Class<?> argType = parameter.getType();
            Object argValue = null;

            Map<String, Object> autowireCandidates = this.beanFactory.findAutowireCandidate(argType, argName);
            if (autowireCandidates.isEmpty()) {
                throw new BeanCreationException("Error in resolve method dependency: " + candidate.getName());
            }
            // 只会循环一次
            for (String beanName : autowireCandidates.keySet()) {
                argValue = (autowireCandidates.get(beanName) instanceof Class) ?
                        this.beanFactory.getBean(beanName) : autowireCandidates.get(beanName);
            }

            methodArguments.add(new MethodArguments.ArgumentHolder(argIndex, argName, argType, argValue));
            argIndex++;
        }

        return new MethodArguments(methodArguments);
    }

}