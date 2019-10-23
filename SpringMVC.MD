# SpringMVC 源码基本流程

## Bean 生命周期

**Bean 创建整个步骤为：1. 创建实例 2. 注入属性 3. 初始化**

![Spring Bean 生命周期](https://github.com/StayHungryStayFoolish/SpringExercise/blob/master/Bean%20%E7%94%9F%E5%91%BD%E5%91%A8%E6%9C%9F.png?raw=true)

## Spring Bean 初始化

**方式：**

1. 实现 **InitializingBean** 接口注入属性后调用 `afterPropertiesSet` 方法。
2. 通过反射调用 **bean** 标签中的 `init-method` 属性指定的方法

**区别：**

1. 接口比 xml 配置效率高，但是 xml 配置消除了对 `Spring` 的依赖。

   实现 **InitializingBean** 接口，在初始化 `Bean` 时会执行 `afterPropertiesSet` 方法。

2. 初始化时，优先调用 **InitializingBean** 的 `afterPropertiesSet` 方法，然后再调用 `init-method` 属性指定的方法。

**源码逻辑：**

```java
	protected void invokeInitMethods(String beanName, final Object bean, @Nullable RootBeanDefinition mbd)
			throws Throwable {
		// 判断是否实现了 InitializingBean 接口
		boolean isInitializingBean = (bean instanceof InitializingBean);
		if (isInitializingBean && (mbd == null || !mbd.isExternallyManagedInitMethod("afterPropertiesSet"))) {
			if (logger.isDebugEnabled()) {
				logger.debug("Invoking afterPropertiesSet() on bean with name '" + beanName + "'");
			}
			if (System.getSecurityManager() != null) {
				try {
					AccessController.doPrivileged((PrivilegedExceptionAction<Object>) () -> {
						/**
						 * 实现 InitializingBean 接口，调用该方法 
						 * @see InitializingBean#afterPropertiesSet() 
						 */
						((InitializingBean) bean).afterPropertiesSet();
						return null;
					}, getAccessControlContext());
				}
				catch (PrivilegedActionException pae) {
					throw pae.getException();
				}
			}
			else {
				/**
				 * 实现 InitializingBean 接口，调用该方法 
				 * @see InitializingBean#afterPropertiesSet()
				 */
				((InitializingBean) bean).afterPropertiesSet();
			}
		}
		
		// 然后调用 xml 标签中的 init-method 方法
		if (mbd != null && bean.getClass() != NullBean.class) {
			// 获取 init-method 指定的方法
			String initMethodName = mbd.getInitMethodName();
			if (StringUtils.hasLength(initMethodName) &&
					!(isInitializingBean && "afterPropertiesSet".equals(initMethodName)) &&
					!mbd.isExternallyManagedInitMethod(initMethodName)) {
				// 最终通过反射调用该方法
				invokeCustomInitMethod(beanName, bean, mbd);
			}
		}
	}
```



## Servlet 生命周期

1. **init** `Servlet` 对象创建后调用
2. **service** `Servlet` 对象被 `Http` 请求访问时调用
3. **destory** `Servlet` 对象销毁前调用

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

1. **Tomcat** 启动时会调用 **Servlet** 的 `init` 方法，其次调用的子类 **GenericServlet** 的 `init` 方法，但是该方法是空实现所以必定由其子类覆写，**HttpServlet** 是抽象类，所以在 **HttpServletBean** 类中对该方法进行了覆写。

   **GenericServlet#init**

   ```java
   public abstract class GenericServlet implements Servlet,ServletConfig,java.io.Serializable {
    
       public void init(ServletConfig config) throws ServletException {
   			this.config = config;
   			this.init();
       }
     
       // 该方法空实现，所以由 HttpServletBean 来实现
       public void init() throws ServletException {
   
       }  
   }  
   ```

   **HttpServletBean#init** 查找 `web.xml` 文件并设置 `DispatcherServlet` 属性并构造对象，调用 `initServletBean` 进行初始化

   ```java
   public abstract class HttpServletBean extends HttpServlet implements EnvironmentCapable, EnvironmentAware {
     
   	@Override
   	public final void init() throws ServletException {
   		if (logger.isDebugEnabled()) {
   			logger.debug("Initializing servlet '" + getServletName() + "'");
   		}
   
   		// Set bean properties from init parameters.
   		/**
   		 * 封装持有 PropertyValue 的对象，该 ServletConfigPropertyValues 对象会在构造参数中使用 ServletConfig 对象找到 web.xml 文件中
   		 * DispatcherServlet 的 init-param 参数设置到 private final List<PropertyValue> propertyValueList; 属性中，设置到 DispatcherServlet 中
   		 * @see org.springframework.beans.PropertyValues
   		 *
   		 * web.xml 文件配置
   		 *
   		 * <servlet>
   		 *   	<servlet-name>dispatcher</servlet-name>
   		 *   	<servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
   		 *   	<load-on-startup>1</load-on-startup>
   		 *   	<init-param>
   		 *    		 <param-name>contextConfigLocation</param-name>
   		 *    		 <param-value>classpath:springmvc.xml</param-value>
   		 *  	 </init-param>
   		 * </servlet>
   		 *
   		 * <servlet-mapping>
   		 *   	<servlet-name>dispatcher</servlet-name>
   		 *   	<url-pattern>/</url-pattern>
   		 * </servlet-mapping>
   		 */
   		PropertyValues pvs = new ServletConfigPropertyValues(getServletConfig(), this.requiredProperties);
   		if (!pvs.isEmpty()) {
   			try {
   				// 使用 BeanWrapper 构造 DispatcherServlet
   				BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(this);
   				ResourceLoader resourceLoader = new ServletContextResourceLoader(getServletContext());
   				bw.registerCustomEditor(Resource.class, new ResourceEditor(resourceLoader, getEnvironment()));
   				initBeanWrapper(bw);
   				// 将 ServletConfigPropertyValues 对象父类的 List<PropertyValue> propertyValueList 并设置 DispatcherServlet 属性
   				bw.setPropertyValues(pvs, true);
   			}
   			catch (BeansException ex) {
   				if (logger.isErrorEnabled()) {
   					logger.error("Failed to set bean properties on servlet '" + getServletName() + "'", ex);
   				}
   				throw ex;
   			}
   		}
   
   		// Let subclasses do whatever initialization they like.
   		/**
   		 * 默认空实现，由子类实现
   		 * @see FrameworkServlet#initServletBean()
   		 */
   		initServletBean();
   
   		if (logger.isDebugEnabled()) {
   			logger.debug("Servlet '" + getServletName() + "' configured successfully");
   		}
   	}
   }
   ```

2. **HttpServletBean#initServletBean** 模板方法由子类 **FrameworkServlet** 实现 `initServletBean` ，调用 `initWebApplicationContext` 方法刷新 SpringIoC 容器并初始化所有的 `Bean`，最终设置到 `ServletContex` 上下文中。

   ```java
   	@Override
   	protected final void initServletBean() throws ServletException {
   		getServletContext().log("Initializing Spring FrameworkServlet '" + getServletName() + "'");
   		if (this.logger.isInfoEnabled()) {
   			this.logger.info("FrameworkServlet '" + getServletName() + "': initialization started");
   		}
   		long startTime = System.currentTimeMillis();
   
   		try {
   			// 初始化 WebApplicationContext 对象属性
   			this.webApplicationContext = initWebApplicationContext();
   			initFrameworkServlet();
   		}
   		catch (ServletException | RuntimeException ex) {
   			this.logger.error("Context initialization failed", ex);
   			throw ex;
   		}
   
   		if (this.logger.isInfoEnabled()) {
   			long elapsedTime = System.currentTimeMillis() - startTime;
   			this.logger.info("FrameworkServlet '" + getServletName() + "': initialization completed in " +
   					elapsedTime + " ms");
   		}
   	}
   ```

3. **initWebApplicationContext** 调用 `configureAndRefreshWebApplicationContext` 刷新容器上下文，初始化所有的 `Bean`，将 `SrpingIoc` 容器设置到 `ServletContex` 与 `Tomcat` 容器进行关联，最终调用 `onRefresh` 模板方法由子类 **DispatcherServlet** 使用策略模式完成各种组件初始化。

   ```java
   	protected WebApplicationContext initWebApplicationContext() {
   		// 获取 SpringIoC 根容器
   		WebApplicationContext rootContext =
   				WebApplicationContextUtils.getWebApplicationContext(getServletContext());
   		WebApplicationContext wac = null;
   
   		if (this.webApplicationContext != null) {
   			// A context instance was injected at construction time -> use it
   			wac = this.webApplicationContext;
   			if (wac instanceof ConfigurableWebApplicationContext) {
   				ConfigurableWebApplicationContext cwac = (ConfigurableWebApplicationContext) wac;
   				if (!cwac.isActive()) {
   					// The context has not yet been refreshed -> provide services such as
   					// setting the parent context, setting the application context id, etc
   					if (cwac.getParent() == null) {
   						// The context instance was injected without an explicit parent -> set
   						// the root application context (if any; may be null) as the parent
   						cwac.setParent(rootContext);
   					}
   					/**
   					 * 配置 DispatcherServlet 属性完成初始化并最终刷新 SpringIoC 容器，初始化所有的 Bean
   					 * @see AbstractApplicationContext#refresh()
   					 */
   					configureAndRefreshWebApplicationContext(cwac);
   				}
   			}
   		}
   		if (wac == null) {
   			// No context instance was injected at construction time -> see if one
   			// has been registered in the servlet context. If one exists, it is assumed
   			// that the parent context (if any) has already been set and that the
   			// user has performed any initialization such as setting the context id
   			// 当 webApplicationContext 已经存在 ServletContext 时，通过配置在 servlet 中的 contextAttribute 参数获取，该属性一般步设置
   			wac = findWebApplicationContext();
   		}
   		if (wac == null) {
   			// No context instance is defined for this servlet -> create a local one
   			// 如果 webApplicationContext 还没有创建，则创建一个
   			/**
   			 * 最终也会调用该方法
   			 * @see #configureAndRefreshWebApplicationContext
   			 */
   			wac = createWebApplicationContext(rootContext);
   		}
   
   		if (!this.refreshEventReceived) {
   			// Either the context is not a ConfigurableApplicationContext with refresh
   			// support or the context injected at construction time had already been
   			// refreshed -> trigger initial onRefresh manually here.
   			/**
   			 * 当 ContextRefresh 事件没有触发时调用此方法，模版方法，由子类 DispatcherServlet 使用策略模式完成各种组件装配
   			 * @see DispatcherServlet#onRefresh(ApplicationContext)
   			 */
   			onRefresh(wac);
   		}
   
   		if (this.publishContext) {
   			// Publish the context as a servlet context attribute.
   			String attrName = getServletContextAttributeName();
   			// 将新建的 SpringIoC 容器设置到 ServletContext 中
   			getServletContext().setAttribute(attrName, wac);
   			if (this.logger.isDebugEnabled()) {
   				this.logger.debug("Published WebApplicationContext of servlet '" + getServletName() +
   						"' as ServletContext attribute with name [" + attrName + "]");
   			}
   		}
   
   		return wac;
   	}
   ```

4. **configureAndRefreshWebApplicationContext** 配置并刷新容器

   ```java
   	protected void configureAndRefreshWebApplicationContext(ConfigurableWebApplicationContext wac) {
   		if (ObjectUtils.identityToString(wac).equals(wac.getId())) {
   			// The application context id is still set to its original default value
   			// -> assign a more useful id based on available information
   			if (this.contextId != null) {
   				wac.setId(this.contextId);
   			}
   			else {
   				// Generate default id...
   				wac.setId(ConfigurableWebApplicationContext.APPLICATION_CONTEXT_ID_PREFIX +
   						ObjectUtils.getDisplayString(getServletContext().getContextPath()) + '/' + getServletName());
   			}
   		}
   
   		wac.setServletContext(getServletContext());
   		wac.setServletConfig(getServletConfig());
   		wac.setNamespace(getNamespace());
   		wac.addApplicationListener(new SourceFilteringListener(wac, new ContextRefreshListener()));
   
   		// The wac environment's #initPropertySources will be called in any case when the context
   		// is refreshed; do it eagerly here to ensure servlet property sources are in place for
   		// use in any post-processing or initialization that occurs below prior to #refresh
   		ConfigurableEnvironment env = wac.getEnvironment();
   		if (env instanceof ConfigurableWebEnvironment) {
   			((ConfigurableWebEnvironment) env).initPropertySources(getServletContext(), getServletConfig());
   		}
   
   		postProcessWebApplicationContext(wac);
   		applyInitializers(wac);
   		/**
   		 * 刷新 SpringIoC 容器，初始化所有的 Bean
   		 * @see AbstractApplicationContext#refresh()
   		 */
   		wac.refresh();
   	}
   ```

5. **DispatcherServlet#onRefresh** 使用策略模式完成各种组件的装配

   ```java
   	@Override
   	protected void onRefresh(ApplicationContext context) {
   		// 采用策略模式初始化各种对象
   		initStrategies(context);
   	}
   
   	protected void initStrategies(ApplicationContext context) {
   		// 初始多部件解析器（处理文件上传）
   		initMultipartResolver(context);
   		// 初始国际化解析器
   		initLocaleResolver(context);
   		// 初始主题解析器
   		initThemeResolver(context);
   		// 初始处理器映射器
   		initHandlerMappings(context);
   		// 初始处理器适配器（处理请求调用 Controller 类）
   		initHandlerAdapters(context);
   		// 初始异常解析器
   		initHandlerExceptionResolvers(context);
   		// 初始视图转换器
   		initRequestToViewNameTranslator(context);
   		// 初始视图解析器（将请求直接转换为逻辑视图名）
   		initViewResolvers(context);
   		// 初始存储管理器（提供请求存储属性）
   		initFlashMapManager(context);
   	}
   
   ```

   

