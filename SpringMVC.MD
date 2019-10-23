# SpringMVC 源码基本流程

## Bean 生命周期

**Bean 创建整个步骤为：1. 创建实例 2. 注入属性 3. 初始化**

![Spring Bean 生命周期](https://github.com/StayHungryStayFoolish/SpringExercise/blob/master/Bean%20%E7%94%9F%E5%91%BD%E5%91%A8%E6%9C%9F.png?raw=true)


## Spring Bean 初始化两种方式

1.  实现 **InitializingBean** 接口
2.  通过反射调用 **bean** 标签中的 `init-method` 属性指定的方法

## Servlet 生命周期

1.  **init** `Servlet` 对象创建后调用
2.  **service** `Servlet` 对象被 `Http` 请求访问时调用
3.  **destory** `Servlet` 对象销毁前调用

```java
public interface Servlet {

    public void init(ServletConfig config) throws ServletException;

    public ServletConfig getServletConfig();
    
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException;
	
    public String getServletInfo();

    public void destroy();
}
```

## DispatcherServlet 类图结构

![DispatcherServlet ](https://github.com/StayHungryStayFoolish/SpringExercise/blob/master/Servlet.jpg?raw=true)

## DispatcherServlet 初始化流程

1.  
