MySpring 基于配置的IoC容器

本项目是一个模拟Spring的IoC容器，容器采用的配置方式为 **JavaConfig+注解**，本项目实现的IoC容器目前支持以下功能：

- 使用`@Configuration`注解声明配置类。
- 可以通过`@Component`配置被扫描的类，并通过`@Scope`注解配置作用域，目前支持`SINGLETON`和`PROTOTYPE`两种作用域。
- 在配置类上可以配置`@ComponentScan`注解，用于扫描指定路径下被`@Component`注解标注的类。扫描过程中，会注册bean definition，如果是单例作用域的类在容器初始化时还会创建其对象放入缓存。
  - 例如`@ComponentScan(basePackages = "com.demo")`会