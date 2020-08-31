package com.lvhao.myspring.util;

import java.util.ArrayList;
import java.util.List;

public class MethodArguments {

    List<ArgumentHolder> methodArguments;

    public MethodArguments() {
        methodArguments = new ArrayList<>();
    }

    public MethodArguments(List<ArgumentHolder> methodArguments) {
        this.methodArguments = methodArguments;
    }

    public List<ArgumentHolder> getMethodArguments() {
        return methodArguments;
    }

    public Object[] getMethodArgumentValues() {
        Object[] values = new Object[methodArguments.size()];
        for (int i = 0; i < values.length; i++) {
            values[i] = methodArguments.get(i).value;
        }
        return values;
    }

    public static class ArgumentHolder {
        private int index;
        private String name;
        private Class<?> clazz;
        private Object value;

        public ArgumentHolder(int index, String name, Class<?> clazz, Object value) {
            this.index = index;
            this.name = name;
            this.clazz = clazz;
            this.value = value;
        }


    }
}
