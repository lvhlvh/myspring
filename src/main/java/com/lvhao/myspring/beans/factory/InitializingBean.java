package com.lvhao.myspring.beans.factory;

public interface InitializingBean {

    void afterPropertiesSet() throws Exception;
}
