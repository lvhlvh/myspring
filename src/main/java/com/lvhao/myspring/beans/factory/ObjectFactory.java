package com.lvhao.myspring.beans.factory;

@FunctionalInterface
public interface ObjectFactory<T> {

    T getObject();
}
