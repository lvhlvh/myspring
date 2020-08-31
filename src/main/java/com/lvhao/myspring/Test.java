package com.lvhao.myspring;

import java.lang.reflect.Constructor;
import java.net.URL;

class Apple {
    private String color;

    public Apple(String color) {
        this.color = color;
    }
}

public class Test {
    public static void main(String[] args) {
       ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL url = classLoader.getResource("com.lvhao.myspring.test.t1.entity".replace(".", "/"));
        System.out.println(url.getPath());
    }

}
