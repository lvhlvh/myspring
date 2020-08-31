MySpring 基于配置的IoC容器



## 要点

本项目是一个模拟Spring的IoC容器，容器采用的配置方式为 **JavaConfig+注解**，目前支持如下功能：

- 使用`@Configuration`注解声明**配置类**
- 在配置类上可以配置`@ComponentScan`注解，用于扫描指定路径下被`@Component`注解标注的类。扫描过程中，会注册bean definition，如果是单例作用域的类在ApplicationContext初始化时还会创建其对象放入缓存
- 可以通过`@Component`配置被**扫描**的类，可以使用`@Component`注解的`value`属性指定bean名称，不指定默认是类名首字母小写。
- 可以通过`@Scope`注解配置**作用域**，目前支持`SINGLETON`和`PROTOTYPE`两种作用域
- 可以通过`@Autowired`进行构造器和setter方法的**自动注入**，并且模拟Spring使用**三级缓存**解决了setter注入的**循环依赖问题**；暂不支持field注入
- 模拟了**Spring Bean生命周期**中的部分环节，包括模拟了`InitializingBean`和`DisposableBean`接口的初始化和销毁方法
- 采用**原子变量和CAS**确保了ApplicationContext只能被刷新(refresh)一次

## 用法

以Intellij IDEA为例，将`myspring-1.0.jar`放入项目的lib目录下，然后在jar包上点击右键选择"Add as Library"即可。

<img src="https://raw.githubusercontent.com/lvhlvh/pictures/master/img/20200831170248.png" style="zoom:67%;" />

参考：[【IDEA】向IntelliJ IDEA创建的项目导入Jar包的两种方式](https://blog.csdn.net/qq_26525215/article/details/53239123)

## 示例

### 1. 构造器注入

Component类：

```java
package com.lvhao.t1.dao;

import com.lvhao.myspring.annotation.stereotype.Component;

@Component
public class UserDao {}
```

```java
package com.lvhao.t1.service;

import com.lvhao.myspring.annotation.inject.Autowired;
import com.lvhao.myspring.annotation.stereotype.Component;
import com.lvhao.t1.dao.UserDao;

@Component
public class UserService {
    private UserDao userDao;

    public UserService() {}

    @Autowired
    public UserService(UserDao userDao) { this.userDao = userDao;}

    public UserDao getUserDao() { return userDao;}
}
```

配置类：

```java
package com.lvhao.t1;

import com.lvhao.myspring.annotation.context.ComponentScan;
import com.lvhao.myspring.annotation.stereotype.Configuration;
import com.lvhao.myspring.context.AnnotationConfigApplicationContext;
import com.lvhao.t1.service.UserService;

@Configuration
@ComponentScan(basePackages = "com.lvhao.t1")
public class AppConfig {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext
                = new AnnotationConfigApplicationContext(AppConfig.class);
        UserService userService = (UserService) applicationContext.getBean("userService");
        System.out.println(userService);
        System.out.println(userService.getUserDao());
    }
}
```

输出结果：

<img src="https://raw.githubusercontent.com/lvhlvh/pictures/master/img/20200831170756.png" style="zoom:80%;" />

### 2. setter注入—循环依赖

Component类：

> `UserDao` 和 `UserService` 之间构成了循环依赖

```java
package com.lvhao.t2.dao;

import com.lvhao.myspring.annotation.inject.Autowired;
import com.lvhao.myspring.annotation.stereotype.Component;
import com.lvhao.t2.service.UserService;

@Component
public class UserDao {
    private UserService userService;

    public UserDao() { }

    public UserService getUserService() { return userService; }

    @Autowired
    public void setUserService(UserService userService) { this.userService = userService; }
}
```

```java
package com.lvhao.t2.service;

import com.lvhao.myspring.annotation.inject.Autowired;
import com.lvhao.myspring.annotation.stereotype.Component;
import com.lvhao.t2.dao.UserDao;

@Component
public class UserService {
    private UserDao userDao;

    public UserService() { }

    public UserDao getUserDao() { return userDao; }

    @Autowired
    public void setUserDao(UserDao userDao) { this.userDao = userDao; }
}
```

配置类：

```java
package com.lvhao.t2;

import com.lvhao.myspring.annotation.context.ComponentScan;
import com.lvhao.myspring.annotation.stereotype.Configuration;
import com.lvhao.myspring.context.AnnotationConfigApplicationContext;
import com.lvhao.t2.dao.UserDao;
import com.lvhao.t2.service.UserService;

@Configuration
@ComponentScan(basePackages = "com.lvhao.t2")
public class AppConfig {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext
                = new AnnotationConfigApplicationContext(AppConfig.class);
        UserDao userDao = (UserDao) applicationContext.getBean("userDao");
        UserService userService = (UserService) applicationContext.getBean("userService");
        System.out.println(userDao.getUserService());
        System.out.println(userService.getUserDao());
    }
}
```

输出结果：

<img src="https://raw.githubusercontent.com/lvhlvh/pictures/master/img/20200831171747.png" style="zoom:80%;" />

### 3. 生命周期

该IoC容器模拟了Spring中的`InitializingBean`和`DisposableBean`接口，提供了初始化和销毁回调。

Component类：

```java
package com.lvhao.t3.entity;

import com.lvhao.myspring.annotation.stereotype.Component;

@Component
public class Address {}
```

```java
package com.lvhao.t3.entity;

import com.lvhao.myspring.annotation.inject.Autowired;
import com.lvhao.myspring.annotation.stereotype.Component;
import com.lvhao.myspring.beans.factory.DisposableBean;
import com.lvhao.myspring.beans.factory.InitializingBean;

@Component
public class Person implements InitializingBean, DisposableBean {
    private Address name;

    public Person() {
        System.out.println("constructor...");
    }

    public Address getName() {
        return name;
    }

    @Autowired
    public void setName(Address name) {
        System.out.println("setName...");
        this.name = name;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("afterPropertiesSet...");
    }

    @Override
    public void destroy() throws Exception {
        System.out.println("destroy...");
    }
}
```

配置类：

```java
package com.lvhao.t3;

import com.lvhao.myspring.annotation.context.ComponentScan;
import com.lvhao.myspring.annotation.stereotype.Configuration;
import com.lvhao.myspring.context.AnnotationConfigApplicationContext;

@Configuration
@ComponentScan(basePackages = "com.lvhao.t3")
public class AppConfig {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext
                = new AnnotationConfigApplicationContext(AppConfig.class);
        applicationContext.close();
    }
}
```

输出结果：

<img src="https://raw.githubusercontent.com/lvhlvh/pictures/master/img/20200831172447.png" style="zoom:80%;" />![](https://raw.githubusercontent.com/lvhlvh/pictures/master/img/20200831180139.png)

### 4. 作用域

Component类：

```java
package com.lvhao.t4.entity;

import com.lvhao.myspring.annotation.stereotype.Component;

@Component
public class SingletonBean {
}
```

```java
package com.lvhao.t4.entity;

import com.lvhao.myspring.annotation.context.Scope;
import com.lvhao.myspring.annotation.stereotype.Component;

@Component
@Scope("prototype")
public class PrototypeBean {
}
```

配置类：

```java
package com.lvhao.t4;

import com.lvhao.myspring.annotation.context.ComponentScan;
import com.lvhao.myspring.annotation.stereotype.Configuration;
import com.lvhao.myspring.context.AnnotationConfigApplicationContext;

@Configuration
@ComponentScan(basePackages = "com.lvhao.t4")
public class AppConfig {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext
                = new AnnotationConfigApplicationContext(AppConfig.class);
        System.out.println(applicationContext.getBean("singletonBean"));
        System.out.println(applicationContext.getBean("singletonBean"));
        System.out.println("------------------------------");
        System.out.println(applicationContext.getBean("prototypeBean"));
        System.out.println(applicationContext.getBean("prototypeBean"));
    }
}
```

输出结果：

![](https://raw.githubusercontent.com/lvhlvh/pictures/master/img/20200831180139.png)

