# SpringMVC 源码基本流程

## Bean 生命周期

**Bean 创建整个步骤为：1. 创建实例 2. 注入属性 3. 初始化**

![Spring Bean 生命周期](https://github.com/StayHungryStayFoolish/SpringExercise/blob/master/Bean%20%E7%94%9F%E5%91%BD%E5%91%A8%E6%9C%9F.png?raw=true)

## Spring Bean 初始化

**方式：**

1.  实现 **InitializingBean** 接口注入属性后调用 `afterPropertiesSet` 方法。
2.  通过反射调用 **bean** 标签中的 `init-method` 属性指定的方法

**区别：**

1.  接口比 xml 配置效率高，但是 xml 配置消除了对 `Spring` 的依赖。

    实现 **InitializingBean** 接口，在初始化 `Bean` 时会执行 `afterPropertiesSet` 方法。

2.  初始化时，优先调用 **InitializingBean** 的 `afterPropertiesSet` 方法，然后再调用 `init-method` 属性指定的方法。

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

1.  **Tomcat** 启动时会调用 **Servlet** 的 `init` 方法，其次调用的子类 **GenericServlet** 的 `init` 方法，但是该方法是空实现所以必定由其子类覆写，**HttpServlet** 是抽象类，所以在 **HttpServletBean** 类中对该方法进行了覆写。

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

2.  **HttpServletBean#initServletBean** 模板方法由子类 **FrameworkServlet** 实现 `initServletBean` ，调用 `initWebApplicationContext` 方法刷新 SpringIoC 容器并初始化所有的 `Bean`，最终设置到 `ServletContex` 上下文中。

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

3.  **initWebApplicationContext** 调用 `configureAndRefreshWebApplicationContext` 刷新容器上下文，初始化所有的 `Bean`，将 `SrpingIoc` 容器设置到 `ServletContex` 与 `Tomcat` 容器进行关联，最终调用 `onRefresh` 模板方法由子类 **DispatcherServlet** 使用策略模式完成各种组件初始化。

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

4.  **configureAndRefreshWebApplicationContext** 配置并刷新容器

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

5.  **DispatcherServlet#onRefresh** 使用策略模式完成各种组件的装配

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

## DispatcherServlet 访问请求流程

1.  **Tomcat** 容器会调用 **Servlet** 的 `service` 方法`拦截 Http 请求`，其次调用的子类 **GenericServlet** 的 `service` 方法，，然后调用 **FrameworServlet** 的 `service` 方法。

    ```java
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    
    	HttpMethod httpMethod = HttpMethod.resolve(request.getMethod());
    	if (httpMethod == HttpMethod.PATCH || httpMethod == null) {
    		processRequest(request, response);
    	}
    	else {
    		super.service(request, response);
    	}
    }
    ```

     **FrameworServlet** 的 `service` 调用父类 **HttpServlet** 的 `service`。

    ```java
    @Override
    public void service(ServletRequest req, ServletResponse res)
          throws ServletException, IOException
    {
            HttpServletRequest  request;
            HttpServletResponse response;
            
            if (!(req instanceof HttpServletRequest &&
                    res instanceof HttpServletResponse)) {
                throw new ServletException("non-HTTP request or response");
            }
    
            request = (HttpServletRequest) req;
            response = (HttpServletResponse) res;
    		
    		// 调用 service，然后根据请求方式调用 doGet/doPost 等
            service(request, response);
        }
    }
    
    
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException
        {
            String method = req.getMethod();
    
            if (method.equals(METHOD_GET)) {
                long lastModified = getLastModified(req);
                if (lastModified == -1) {
                    // servlet doesn't support if-modified-since, no reason
                    // to go through further expensive logic
                    doGet(req, resp);
                } else {
                    long ifModifiedSince = req.getDateHeader(HEADER_IFMODSINCE);
                    if (ifModifiedSince < lastModified) {
                        // If the servlet mod time is later, call doGet()
                        // Round down to the nearest second for a proper compare
                        // A ifModifiedSince of -1 will always be less
                        maybeSetLastModified(resp, lastModified);
                        doGet(req, resp);
                    } else {
                        resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                    }
                }
    
            } else if (method.equals(METHOD_HEAD)) {
                long lastModified = getLastModified(req);
                maybeSetLastModified(resp, lastModified);
                doHead(req, resp);
    
            } else if (method.equals(METHOD_POST)) {
                doPost(req, resp);
                
            } else if (method.equals(METHOD_PUT)) {
                doPut(req, resp);
                
            } else if (method.equals(METHOD_DELETE)) {
                doDelete(req, resp);
                
            } else if (method.equals(METHOD_OPTIONS)) {
                doOptions(req,resp);
                
            } else if (method.equals(METHOD_TRACE)) {
                doTrace(req,resp);
                
            } else {
                //
                // Note that this means NO servlet supports whatever
                // method was requested, anywhere on this server.
                //
    
                String errMsg = lStrings.getString("http.method_not_implemented");
                Object[] errArgs = new Object[1];
                errArgs[0] = method;
                errMsg = MessageFormat.format(errMsg, errArgs);
                
                resp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, errMsg);
            }
        }
    ```

2.  **FrameworkServlet** 的 **doGet / doPost** 请求最终都会调用 `processRequest` 方法。

    ```java
    	@Override
    	protected final void doGet(HttpServletRequest request, HttpServletResponse response)
    			throws ServletException, IOException {
    
    		processRequest(request, response);
    	}
    
    	@Override
    	protected final void doPost(HttpServletRequest request, HttpServletResponse response)
    			throws ServletException, IOException {
    
    		processRequest(request, response);
    	}
    
    	@Override
    	protected final void doPut(HttpServletRequest request, HttpServletResponse response)
    			throws ServletException, IOException {
    
    		processRequest(request, response);
    	}
    
    	@Override
    	protected final void doDelete(HttpServletRequest request, HttpServletResponse response)
    			throws ServletException, IOException {
    
    		processRequest(request, response);
    	}
    ```

3.  **processRequest** 执行 `doService` 进去 **DispatcherServlet** 处理 `Http` 请求。

    ```java
    	protected final void processRequest(HttpServletRequest request, HttpServletResponse response)
    			throws ServletException, IOException {
    
    		long startTime = System.currentTimeMillis();
    		Throwable failureCause = null;
    
    		// 获取本地化、国际化上下文容器
    		LocaleContext previousLocaleContext = LocaleContextHolder.getLocaleContext();
    		// 1. 新建本地化、国际化上下文容器
    		LocaleContext localeContext = buildLocaleContext(request);
    
    		// 取得当前线程的 RequestAttributes 。默认为空。
    		RequestAttributes previousAttributes = RequestContextHolder.getRequestAttributes();
    		// 2. 创建 RequestAttributes
    		ServletRequestAttributes requestAttributes = buildRequestAttributes(request, response, previousAttributes);
    
    		WebAsyncManager asyncManager = WebAsyncUtils.getAsyncManager(request);
    		asyncManager.registerCallableInterceptor(FrameworkServlet.class.getName(), new RequestBindingInterceptor());
    
    		// 3. 初始化 ContextHolders
    		initContextHolders(request, localeContext, requestAttributes);
    
    		try {
    			// 4. 执行 Http 请求处理
                /**
    			 * @see DispatcherServlet#doService(HttpServletRequest, HttpServletResponse) 
    			 */
    			doService(request, response);
    		}
    		catch (ServletException | IOException ex) {
    			failureCause = ex;
    			throw ex;
    		}
    		catch (Throwable ex) {
    			failureCause = ex;
    			throw new NestedServletException("Request processing failed", ex);
    		}
    
    		finally {
    			// 5. 释放 ContextHolders
    			resetContextHolders(request, previousLocaleContext, previousAttributes);
    			if (requestAttributes != null) {
    				requestAttributes.requestCompleted();
    			}
    
    			if (logger.isDebugEnabled()) {
    				......
    			}
    			// 发布请求完成事件
    			publishRequestHandledEvent(request, response, startTime, failureCause);
    		}
    	}
    ```

4.  **DispatcherServlet** 执行 **doService** 保存请求属性并内部调用 `doDispatch` 处理 `Http` 请求。

    ```java
    	@Override
    	protected void doService(HttpServletRequest request, HttpServletResponse response) throws Exception {
    		if (logger.isDebugEnabled()) {
    			String resumed = WebAsyncUtils.getAsyncManager(request).hasConcurrentResult() ? " resumed" : "";
    			logger.debug("DispatcherServlet with name '" + getServletName() + "'" + resumed +
    					" processing " + request.getMethod() + " request for [" + getRequestUri(request) + "]");
    		}
    
    		// Keep a snapshot of the request attributes in case of an include,
    		// to be able to restore the original attributes after the include.
    		// 保存 HttpServletRequest 的请求属性快照
    		Map<String, Object> attributesSnapshot = null;
    		if (WebUtils.isIncludeRequest(request)) {
    			attributesSnapshot = new HashMap<>();
    			Enumeration<?> attrNames = request.getAttributeNames();
    			while (attrNames.hasMoreElements()) {
    				String attrName = (String) attrNames.nextElement();
    				if (this.cleanupAfterInclude || attrName.startsWith(DEFAULT_STRATEGIES_PREFIX)) {
    					attributesSnapshot.put(attrName, request.getAttribute(attrName));
    				}
    			}
    		}
    
    		// Make framework objects available to handlers and view objects.
    		// 将 DispatcherServlet 的 web 应用上下文(子IoC容器)、LocaleResolver、ThemeResolver等放入请求中，供 Handler、View 对象使用
    		request.setAttribute(WEB_APPLICATION_CONTEXT_ATTRIBUTE, getWebApplicationContext());
    		request.setAttribute(LOCALE_RESOLVER_ATTRIBUTE, this.localeResolver);
    		request.setAttribute(THEME_RESOLVER_ATTRIBUTE, this.themeResolver);
    		request.setAttribute(THEME_SOURCE_ATTRIBUTE, getThemeSource());
    
    		if (this.flashMapManager != null) {
    			FlashMap inputFlashMap = this.flashMapManager.retrieveAndUpdate(request, response);
    			if (inputFlashMap != null) {
    				request.setAttribute(INPUT_FLASH_MAP_ATTRIBUTE, Collections.unmodifiableMap(inputFlashMap));
    			}
    			request.setAttribute(OUTPUT_FLASH_MAP_ATTRIBUTE, new FlashMap());
    			request.setAttribute(FLASH_MAP_MANAGER_ATTRIBUTE, this.flashMapManager);
    		}
    
    		try {
    			// 真正进行 Http 请求处理
    			doDispatch(request, response);
    		}
    		finally {
    			if (!WebAsyncUtils.getAsyncManager(request).isConcurrentHandlingStarted()) {
    				// Restore the original attribute snapshot, in case of an include.
    				if (attributesSnapshot != null) {
    					// 还原请求属性
    					restoreAttributesAfterInclude(request, attributesSnapshot);
    				}
    			}
    		}
    	}
    ```

5.  **doDispatch** 真正处理 `Http` 请求，由 **HandlerAdapter** 子类 **AbstractHandlerMethodAdapter** 执行 `handl` 方法，最终通过反射调用方法执行请求的方法并处理返回结果，返回 `ModelAndView` 视图。

    ```java
    	protected void doDispatch(HttpServletRequest request, HttpServletResponse response) throws Exception {
    		HttpServletRequest processedRequest = request;
    		// Handler 执行链
    		HandlerExecutionChain mappedHandler = null;
    		boolean multipartRequestParsed = false;
    
    		WebAsyncManager asyncManager = WebAsyncUtils.getAsyncManager(request);
    
    		try {
    			ModelAndView mv = null;
    			Exception dispatchException = null;
    
    			try {
    				processedRequest = checkMultipart(request);
    				// 解析 MultipartHttpServletRequest 请求，检查是否包含文件等类型的数据
    				multipartRequestParsed = (processedRequest != request);
    
    				// Determine handler for the current request.
    				// 根据请求，从HandlerMapping 获取 HandlerExecutionChain
    				mappedHandler = getHandler(processedRequest);
    				if (mappedHandler == null) {
    					// 如果 HandlerExecutionChain 为 null，则没有能够进行处理的 Handler，返回 404
    					noHandlerFound(processedRequest, response);
    					return;
    				}
    
    				// Determine handler adapter for the current request.
    				// 根据查找到的 Handler，获取能够进行处理的 HandlerAdapter
    				// 获取 RequestMappingHandlerAdapter
    				HandlerAdapter ha = getHandlerAdapter(mappedHandler.getHandler());
    
    				// Process last-modified header, if supported by the handler.
    				// 判断自上次请求后是否有修改，没有修改直接返回响应
    				String method = request.getMethod();
    				boolean isGet = "GET".equals(method);
    				if (isGet || "HEAD".equals(method)) {
    					long lastModified = ha.getLastModified(request, mappedHandler.getHandler());
    					if (logger.isDebugEnabled()) {
    						logger.debug("Last-Modified value for [" + getRequestUri(request) + "] is: " + lastModified);
    					}
    					if (new ServletWebRequest(request, response).checkNotModified(lastModified) && isGet) {
    						// get请求，且未修改，直接返回
    						return;
    					}
    				}
    
    				// 按顺序依次执行 HandlerInterceptor 的 preHandle方法
    				// 如果任一 HandlerInterceptor 的 preHandle 方法没有通过，则不继续进行处理
    				// 1. 应用 HandlerInterceptor 的 preHandle 方法
    				if (!mappedHandler.applyPreHandle(processedRequest, response)) {
    					return;
    				}
    
    				// Actually invoke the handler.
    				// 2. 调用handler(通过 HandlerAdapter 执行查找到的 handler) 并返回视图
    				/**
    				 * 2.1 调用该类 handle 方法
    				 * @see org.springframework.web.servlet.mvc.method.AbstractHandlerMethodAdapter#handle(HttpServletRequest, HttpServletResponse, Object)
    				 *
    				 * 2.2 handler 内部调用 handleInternal 方法，最终调用 invokeHandlerMethod
    				 * @see org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter#handleInternal(HttpServletRequest, HttpServletResponse, HandlerMethod)
    				 *
    				 * 2.3 invokeHandlerMethod 构造 ServletInvocableHandlerMethod 对象并调用该对象的 invokeAndHandle，并最终返回视图结果
    				 * @see org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter#invokeHandlerMethod(HttpServletRequest, HttpServletResponse, HandlerMethod)
    				 *
    				 * 2.4 执行反射调用方法并返回处理返回值
    				 * @see org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod#invokeAndHandle(ServletWebRequest, ModelAndViewContainer, Object...)
    				 *
    				 *
    				 */
    				mv = ha.handle(processedRequest, response, mappedHandler.getHandler());
    
    				if (asyncManager.isConcurrentHandlingStarted()) {
    					return;
    				}
    
    				// 应用默认视图名
    				applyDefaultViewName(processedRequest, mv);
    				// 3. 逆序执行 HandlerInterceptor 的 postHandle 方法
    				mappedHandler.applyPostHandle(processedRequest, response, mv);
    			}
    			catch (Exception ex) {
    				dispatchException = ex;
    			}
    			catch (Throwable err) {
    				// As of 4.3, we're processing Errors thrown from handler methods as well,
    				// making them available for @ExceptionHandler methods and other scenarios.
    				dispatchException = new NestedServletException("Handler dispatch failed", err);
    			}
    			// 4. 视图渲染，如果有异常，则渲染异常页面
    			processDispatchResult(processedRequest, response, mappedHandler, mv, dispatchException);
    		}
    		catch (Exception ex) {
    			triggerAfterCompletion(processedRequest, response, mappedHandler, ex);
    		}
    		catch (Throwable err) {
    			triggerAfterCompletion(processedRequest, response, mappedHandler,
    					new NestedServletException("Handler processing failed", err));
    		}
    		finally {
    			if (asyncManager.isConcurrentHandlingStarted()) {
    				// Instead of postHandle and afterCompletion
    				if (mappedHandler != null) {
    					// 逆序执行所有 HandlerInterceptor 的 afterCompletion 方法
    					mappedHandler.applyAfterConcurrentHandlingStarted(processedRequest, response);
    				}
    			}
    			else {
    				// Clean up any resources used by a multipart request.
    				if (multipartRequestParsed) {
    					// 如果请求包含文件类型的数据，则进行相关清理工作
    					cleanupMultipart(processedRequest);
    				}
    			}
    		}
    	}
    ```

6.  **AbstractHandlerMethodAdapter** 的 **handle** 调用模板方法 `handleInternal` 由子类 **RequestMappingHandlerAdapter** 实现

    ```java
    	@Override
    	@Nullable
    	public final ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler)
    			throws Exception {
    
    		return handleInternal(request, response, (HandlerMethod) handler);
    	}
    
    
    	@Nullable
    	protected abstract ModelAndView handleInternal(HttpServletRequest request,
    			HttpServletResponse response, HandlerMethod handlerMethod) throws Exception;
    ```

7.  **RequestMappingHandlerAdapter** 的 **handleInternal** 方法内部调用 `invokeHandlerMethod` 方法，`invokeHandlerMethod` 方法内部通过构造 **ServletInvocableHandlerMethod** 对象调用该对象的 `invokeAndHandle` 方法

    ```java
    @Override
    	protected ModelAndView handleInternal(HttpServletRequest request,
    			HttpServletResponse response, HandlerMethod handlerMethod) throws Exception {
    
    		ModelAndView mav;
    		checkRequest(request);
    
    		// Execute invokeHandlerMethod in synchronized block if required.
    		if (this.synchronizeOnSession) {
    			HttpSession session = request.getSession(false);
    			if (session != null) {
    				Object mutex = WebUtils.getSessionMutex(session);
    				synchronized (mutex) {
    					// 该方法内部通过构造 ServletInvocableHandlerMethod 对象调用该对象的 invokeAndHandle 方法通过反射调用请求方法，并处理返回值
    					mav = invokeHandlerMethod(request, response, handlerMethod);
    				}
    			}
    			else {
    				// No HttpSession available -> no mutex necessary
    				// 该方法内部通过构造 ServletInvocableHandlerMethod 对象调用该对象的 invokeAndHandle 方法通过反射调用请求方法，并处理返回值
    				mav = invokeHandlerMethod(request, response, handlerMethod);
    			}
    		}
    		else {
    			// No synchronization on session demanded at all...
    			// 该方法内部通过构造 ServletInvocableHandlerMethod 对象调用该对象的 invokeAndHandle 方法通过反射调用请求方法，并处理返回值
    			mav = invokeHandlerMethod(request, response, handlerMethod);
    		}
    
    		if (!response.containsHeader(HEADER_CACHE_CONTROL)) {
    			if (getSessionAttributesHandler(handlerMethod).hasSessionAttributes()) {
    				applyCacheSeconds(response, this.cacheSecondsForSessionAttributeHandlers);
    			}
    			else {
    				prepareResponse(response);
    			}
    		}
    
    		return mav;
    	}
    ```

    ```java
    	@Nullable
    	protected ModelAndView invokeHandlerMethod(HttpServletRequest request,
    			HttpServletResponse response, HandlerMethod handlerMethod) throws Exception {
    
    		ServletWebRequest webRequest = new ServletWebRequest(request, response);
    		try {
    			WebDataBinderFactory binderFactory = getDataBinderFactory(handlerMethod);
    			ModelFactory modelFactory = getModelFactory(handlerMethod, binderFactory);
    
    			ServletInvocableHandlerMethod invocableMethod = createInvocableHandlerMethod(handlerMethod);
    			if (this.argumentResolvers != null) {
    				invocableMethod.setHandlerMethodArgumentResolvers(this.argumentResolvers);
    			}
    			if (this.returnValueHandlers != null) {
    				invocableMethod.setHandlerMethodReturnValueHandlers(this.returnValueHandlers);
    			}
    			invocableMethod.setDataBinderFactory(binderFactory);
    			invocableMethod.setParameterNameDiscoverer(this.parameterNameDiscoverer);
    
    			ModelAndViewContainer mavContainer = new ModelAndViewContainer();
    			mavContainer.addAllAttributes(RequestContextUtils.getInputFlashMap(request));
    			modelFactory.initModel(webRequest, mavContainer, invocableMethod);
    			mavContainer.setIgnoreDefaultModelOnRedirect(this.ignoreDefaultModelOnRedirect);
    
    			AsyncWebRequest asyncWebRequest = WebAsyncUtils.createAsyncWebRequest(request, response);
    			asyncWebRequest.setTimeout(this.asyncRequestTimeout);
    
    			WebAsyncManager asyncManager = WebAsyncUtils.getAsyncManager(request);
    			asyncManager.setTaskExecutor(this.taskExecutor);
    			asyncManager.setAsyncWebRequest(asyncWebRequest);
    			asyncManager.registerCallableInterceptors(this.callableInterceptors);
    			asyncManager.registerDeferredResultInterceptors(this.deferredResultInterceptors);
    
    			if (asyncManager.hasConcurrentResult()) {
    				Object result = asyncManager.getConcurrentResult();
    				mavContainer = (ModelAndViewContainer) asyncManager.getConcurrentResultContext()[0];
    				asyncManager.clearConcurrentResult();
    				if (logger.isDebugEnabled()) {
    					logger.debug("Found concurrent result value [" + result + "]");
    				}
    				invocableMethod = invocableMethod.wrapConcurrentResult(result);
    			}
    
    			// 调用 HandlerMethod 并返回响应，设置是否需要视图解析
    			/**
    			 * 通过反射调用请求方法，并处理返回值
    			 * @see org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod#invokeAndHandle(ServletWebRequest, ModelAndViewContainer, Object...)
    			 */
    			invocableMethod.invokeAndHandle(webRequest, mavContainer);
    			if (asyncManager.isConcurrentHandlingStarted()) {
    				return null;
    			}
    
    			// 获取视图解析器
    			return getModelAndView(mavContainer, modelFactory, webRequest);
    		}
    		finally {
    			webRequest.requestCompleted();
    		}
    	}
    ```

8.  **ServletInvocableHandlerMethod** 的 **invokeAndHandle** 内部调用 `invokeForRequest` 使用反射处理 `Http` 的 url 对应的方法获取返回值并处理。

    ```java
    	public void invokeAndHandle(ServletWebRequest webRequest, ModelAndViewContainer mavContainer,
    			Object... providedArgs) throws Exception {
    
    		/**
    		 * 调用反射处理请求
    		 * @see InvocableHandlerMethod#invokeForRequest(NativeWebRequest, ModelAndViewContainer, Object...)
    		 * 执行反射调用
    		 * @see InvocableHandlerMethod#doInvoke(Object...)
    		 */
    		Object returnValue = invokeForRequest(webRequest, mavContainer, providedArgs);
    		setResponseStatus(webRequest);
    
    		if (returnValue == null) {
    			if (isRequestNotModified(webRequest) || getResponseStatus() != null || mavContainer.isRequestHandled()) {
    				// 请求已处理，不需要视图解析
    				mavContainer.setRequestHandled(true);
    				return;
    			}
    		}
    		else if (StringUtils.hasText(getResponseStatusReason())) {
    			mavContainer.setRequestHandled(true);
    			return;
    		}
    
    		mavContainer.setRequestHandled(false);
    		Assert.state(this.returnValueHandlers != null, "No return value handlers");
    		try {
    			// 处理返回值
    			this.returnValueHandlers.handleReturnValue(
    					returnValue, getReturnValueType(returnValue), mavContainer, webRequest);
    		}
    		catch (Exception ex) {
    			if (logger.isTraceEnabled()) {
    				logger.trace(getReturnValueHandlingErrorMessage("Error handling return value", returnValue), ex);
    			}
    			throw ex;
    		}
    	}
    ```

9.  从第 5 - 9 步分别是处理 `Http` 请求并返回 `ModelAndView` 视图结果，在 `doDispatch` 方法中最后通过 **processDispatchResult** 方法对视图进行了渲染，渲染由  `render` 处理。

    ```java
    private void processDispatchResult(HttpServletRequest request, HttpServletResponse response,
    			@Nullable HandlerExecutionChain mappedHandler, @Nullable ModelAndView mv,
    			@Nullable Exception exception) throws Exception {
    
    		boolean errorView = false;
    
    		// 判断 HandlerMapping、HandlerAdapter 处理时的异常是否为空
    		if (exception != null) {
    			// 上述两个组件处理时的异常不为空
    			// 如果为 ModelAndViewDefiningException 异常，则获取一个异常视图
    			if (exception instanceof ModelAndViewDefiningException) {
    				logger.debug("ModelAndViewDefiningException encountered", exception);
    				mv = ((ModelAndViewDefiningException) exception).getModelAndView();
    			}
    			else {
    				Object handler = (mappedHandler != null ? mappedHandler.getHandler() : null);
    				mv = processHandlerException(request, response, handler, exception);
    				errorView = (mv != null);
    			}
    		}
    
    		// Did the handler return a view to render?
    		// 判断 mv 是否为空，不管是正常的 ModelAndView 还是异常的 ModelAndView，只要存在 mv 就进行视图渲染
    		if (mv != null && !mv.wasCleared()) {
    			// 视图渲染
    			render(mv, request, response);
    			if (errorView) {
    				WebUtils.clearErrorRequestAttributes(request);
    			}
    		}
    		else {
    			// 否则记录无视图
    			if (logger.isDebugEnabled()) {
    				logger.debug("Null ModelAndView returned to DispatcherServlet with name '" + getServletName() +
    						"': assuming HandlerAdapter completed request handling");
    			}
    		}
    
    		if (WebAsyncUtils.getAsyncManager(request).isConcurrentHandlingStarted()) {
    			// Concurrent handling started during a forward
    			return;
    		}
    
    		if (mappedHandler != null) {
    			// 执行 HandlerInterceptor 的 afterCompletion 方法
    			mappedHandler.triggerAfterCompletion(request, response, null);
    		}
    	}
    ```

10.  **render** 负责渲染视图，内部调用 `resolveViewName` 根据对应的视图名称解析对应的视图

     ```java
     	protected void render(ModelAndView mv, HttpServletRequest request, HttpServletResponse response) throws Exception {
     		// Determine locale for request and apply it to the response.
     		Locale locale =
     				(this.localeResolver != null ? this.localeResolver.resolveLocale(request) : request.getLocale());
     		// 设置 Locale
     		response.setLocale(locale);
     
     		View view;
     		String viewName = mv.getViewName();
     		if (viewName != null) {
     			// We need to resolve the view name.
     			// 使用 ViewResolver 解析视图遍历所有 ViewResolver 类，根据视图名称解析对应的视图 View 默认情况下，得到InternalResourceView对象。
     			view = resolveViewName(viewName, mv.getModelInternal(), locale, request);
     			if (view == null) {
     				throw new ServletException("Could not resolve view with name '" + mv.getViewName() +
     						"' in servlet with name '" + getServletName() + "'");
     			}
     		}
     		else {
     			// No need to lookup: the ModelAndView object contains the actual View object.
     			view = mv.getView();
     			if (view == null) {
     				throw new ServletException("ModelAndView [" + mv + "] neither contains a view name nor a " +
     						"View object in servlet with name '" + getServletName() + "'");
     			}
     		}
     
     		// Delegate to the View object for rendering.
     		if (logger.isDebugEnabled()) {
     			logger.debug("Rendering view [" + view + "] in DispatcherServlet with name '" + getServletName() + "'");
     		}
     		try {
     			if (mv.getStatus() != null) {
     				// 设置 Http 响应状态码
     				response.setStatus(mv.getStatus().value());
     			}
     			view.render(mv.getModelInternal(), request, response);
     		}
     		catch (Exception ex) {
     			if (logger.isDebugEnabled()) {
     				logger.debug("Error rendering view [" + view + "] in DispatcherServlet with name '" +
     						getServletName() + "'", ex);
     			}
     			throw ex;
     		}
     	}
     ```

