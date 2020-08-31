package com.lvhao.myspring.context;

import com.lvhao.myspring.beans.factory.BeanFactory;
import com.lvhao.myspring.exception.UnSupportOperationException;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 提供了对ApplicationContext接口的默认实现
 */
public abstract class AbstractApplicationContext implements ApplicationContext {

    protected final Object startupShutdownMonitor = new Object();

    protected final AtomicBoolean refreshed = new AtomicBoolean(false);

    @Override
    public abstract BeanFactory getBeanFactory() throws UnSupportOperationException;

    @Override
    public Object getBean(String name) {
        return this.getBeanFactory().getBean(name);
    }


}
