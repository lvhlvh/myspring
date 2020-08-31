package com.lvhao.myspring.beans.factory;

/**
 * 具有自动装配功能的bean工厂
 */
public interface AutowireCapableBeanFactory {
    int AUTOWIRE_NO = 0;

    int AUTOWIRE_BY_NAME = 1;

    int AUTOWIRE_BY_TYPE = 2;

    int AUTOWIRE_CONSTRUCTOR = 3;

}
