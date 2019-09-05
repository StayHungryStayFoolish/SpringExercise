# Spring IoC 基础流程分析及手写(附带 AOP、MyBatis 简单应用)

## 项目模块说明

- **CustomIoC** 实现 Spring BeanFactory 的创建解析基本功能。
- **CustomIoCExercise** 测试 CustomIoC 功能。
- **spring-framework-5.0.7.RELEASE** Spring 官方 5.0.7 源码，部分流程增加中文注释
- **SpringBasic** 采用 XML 方式配置 MyBatis、AOP  
- **SpringMybatisAnnotation** 采用注解方式配置 MyBatis  

## Spring IoC 容器加载过程

### Spring IoC 入口

- 基于 BeanFactory 加载 Bean 的入口

  ```java
   DefaultListableBeanFactory bf = new XmlBeanFactory(new ClassPathResource("applicationContext.xml"));
  ```

- 基于 ApplicationContext 加载 Bean 的入口

  ```java
   ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
  ```

  

#### 本文主要以 ApplicationContext 为主，分析 Spring IoC 容器加载过程



1. **ClassPathXmlApplicationContext** 构造方法

   ```java
   	public ClassPathXmlApplicationContext(String configLocation) throws BeansException {
   		this(new String[] {configLocation}, true, null);
   	}
   ```

2. 内部调用最终构造方法，刷新 Spring 容器（删除原有容器，创建新容器）

   ```java
   	public ClassPathXmlApplicationContext(
   			String[] configLocations, boolean refresh, @Nullable ApplicationContext parent)
   			throws BeansException {
   
   		super(parent);
   		// 目的为了创建 Resource 资源对象
   		setConfigLocations(configLocations);
   		if (refresh) {
   			// 加载、解析 BeanDefinition
   			// 创建 DefaultListableBeanFactory
   			// 调用高级父类 AbstractApplicationContext#refresh() 方法
   			refresh();
   		}
   	}
   ```

3. 调用超类 **AbstractApplicationContext**，进入重要方法 **refresh()** ，该方法主要方法是第二步**obtainFreshBeanFactory()**，其余的均为辅助流程。

   ```java
   @Override
   public void refresh() throws BeansException, IllegalStateException {
   		synchronized (this.startupShutdownMonitor) {
   			// Prepare this context for refreshing.
   			// 1. 准备上下文，刷新预处理
         //（记录开始时间、活动状态、验证必须属性是否可解析、以及执行属性源的任何初始化）
   			prepareRefresh();
   
   			// 2.1 创建 IOC 容器，DefaultListableBeanFactory
   			// 2.2 加载解析 XML 文件（存储到 Document 中）
   			// 2.3 读取 Document，并完成 BeanDefinition 加载与注册
   			// 具体看 AbstractRefreshableApplicationContext.refreshBeanFactory(); 方法
   			ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();
   
   			// 3. 对 IOC 容器预处理（设置公共属性）
   			prepareBeanFactory(beanFactory);
   
   			try {
   				// 4. 上下文处理（设置 request/session scopes)
   				postProcessBeanFactory(beanFactory);
   
   				// 5. 调用 BeanFactoryPostProcessor 后置处理器处理 BeanDefinition （针对 PriorityOrdered、Order其他等处理）
   				invokeBeanFactoryPostProcessors(beanFactory);
   
   				// 6. 注册 BeanPostProcessor，根据优先级将 PriorityOrdered、Ordered、其他进行实例化、排序、注册
   				registerBeanPostProcessors(beanFactory);
   
   				// 7. 初始化消息源（例如：国际化 i18n 等）
   				initMessageSource();
   
   				// 8. 初始化应用事件广播器
   				initApplicationEventMulticaster();
   
   				// 9. 模板钩子方法，初始化一些框架的 Bean
   				onRefresh();
   
   				// 10. 注册监听器
   				registerListeners();
   
   				// 11. 注册剩下的 Singleton Bean（非懒加载方式）
   				// 注意事项：(non-lazy-init) Bean 的 IOC、DI、AOP 都是发生在此步骤，关键方法 preInstantiateSingletons()
   				finishBeanFactoryInitialization(beanFactory);
           
   				// 12. 完成刷新时，需要发布对应的事件
   				finishRefresh();
   			} catch (BeansException ex) {
   					....
   			} finally {
   					...
   			}
   		}
   	}
   ```

4. **AbstractApplicationContext** 类的 **obtainFreshBeanFactory()** 完成 BeanFactory 的初始化过程

   ```java
   	protected ConfigurableListableBeanFactory obtainFreshBeanFactory() {
   		// 因为最初的 ClassPathXmlApplicationContext 的父类是 AbstractRefreshableApplicationContext
   		// 所以具体实现由 AbstractRefreshableApplicationContext.refreshBeanFactory()
       // 创建工厂并初始化，解析 xml 文件加载 BeanDefinition，并最终完成解析与注册
   		refreshBeanFactory();
   		ConfigurableListableBeanFactory beanFactory = getBeanFactory();
   		if (logger.isDebugEnabled()) {
   			logger.debug("Bean factory for " + getDisplayName() + ": " + beanFactory);
   		}
   		return beanFactory;
   	}
   ```

5. **AbstractRefreshableApplicationContext**  类的  **refreshBeanFactory()** 主要创建 DefaultListableBeanFactory 工厂，并加载 BeanDefinition 信息

   ```java
   @Override
   	protected final void refreshBeanFactory() throws BeansException {
   		// 销毁以前工厂
   		if (hasBeanFactory()) {
   			destroyBeans();
   			closeBeanFactory();
   		}
   		try {
   			// 创建工厂，并初始化
   			DefaultListableBeanFactory beanFactory = createBeanFactory();
   			beanFactory.setSerializationId(getId());
   			// 根据上线下文设置：是否允许覆盖 BeanDefinition、是否允许循环依赖
   			customizeBeanFactory(beanFactory);
   			// AbstractRefreshableApplicationContext 子类：
   			// 1. AbstractXmlApplicationContext
   			// 2. AnnotationConfigWebApplicationContext
   			// 3. GroovyWebApplicationContext
   			// 4. XmlWebApplicationContext
   			// 1、3、4 最终调用的 AbstractBeanDefinitionReader#loadBeanDefinitions() 加载 BeanDefinition
   			// 1和4 调用 XmlBeanDefinitionReader#loadBeanDefinitions() 加载，在 doLoadBeanDefinitions() 加载，最终在 DefaultBeanDefinitionDocumentReader#doRegisterBeanDefinitions() 完成 XML 文件解析
   			// 2 调用 AnnotatedBeanDefinitionReader#doRegisterBean() 加载 BeanDefinition，并最终在 DefaultListableBeanFactory#registerBeanDefinition 完成解析与注册
   
   			// 实现加载、解析 BeanDefinitions
   			// 加载 BeanDefinition，并最终解析
   			loadBeanDefinitions(beanFactory);
   			synchronized (this.beanFactoryMonitor) {
   				this.beanFactory = beanFactory;
   			}
   		}
   		catch (IOException ex) {
   			throw new ApplicationContextException("I/O error parsing bean definition source for " + getDisplayName(), ex);
   		}
   	}
   ```

6. **AbstractXmlApplicationContext** 核心方法 **loadBeanDefinitions()**  主要创建 XmlBeanDefinitionReader (XML 阅读器)，并定位资源，再次内部调用 **loadBeanDefinitions()** 进行读取 xml 并加载 BeanDefini

   ```java
     @Override
   	protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws BeansException, IOException {
   		// Create a new XmlBeanDefinitionReader for the given BeanFactory.
       // 创建 BeanDefinition 的阅读器 XmlBeanDefinitionReader
   		XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(beanFactory);
   
   		// Configure the bean definition reader with this context's
   		// resource loading environment.
   		beanDefinitionReader.setEnvironment(this.getEnvironment());
   		beanDefinitionReader.setResourceLoader(this);
   		beanDefinitionReader.setEntityResolver(new ResourceEntityResolver(this));
   
   		// Allow a subclass to provide custom initialization of the reader,
   		// then proceed with actually loading the bean definitions.
   		initBeanDefinitionReader(beanDefinitionReader);
   		// 加载阅读器并定位资源，内部调用资源定位，
   		// 进入 AbstractBeanDefinitionReader 类的 loadBeanDefinitions() 方法，
   		// 最终调用 XmlBeanDefinitionReader 的 loadBeanDefinitions() 方法
   		loadBeanDefinitions(beanDefinitionReader);
   	}
   ```

   ```java
     // 内部调用资源定位，进入 AbstractBeanDefinitionReader 类的 loadBeanDefinitions() 方法
   	protected void loadBeanDefinitions(XmlBeanDefinitionReader reader) throws BeansException, IOException {
       // 资源定位
   		Resource[] configResources = getConfigResources();
   		if (configResources != null) {
   			reader.loadBeanDefinitions(configResources);
   		}
   		String[] configLocations = getConfigLocations();
   		if (configLocations != null) {
   			reader.loadBeanDefinitions(configLocations);
   		}
   	}
   ```

7. **AbstractBeanDefinitionReader** 的 **loadBeanDefinitions()** 方法主要通过 Resource 定位资源，将资源加载

   ```java
   	@Override
   	public int loadBeanDefinitions(Resource... resources) throws BeanDefinitionStoreException {
   		Assert.notNull(resources, "Resource array must not be null");
   		int counter = 0;
   		for (Resource resource : resources) {
         // 进入 XmlBeanDefinitionReader 的 loadBeanDefinitions 方法
   			counter += loadBeanDefinitions(resource);
   		}
   		return counter;
   	}
   ```

8. **XmlBeanDefinitionReader** 的 **loadBeanDefinitions()** 方法，核心方法 **doLoadBeanDefinitions()**

   ```java
   @Override
   	public int loadBeanDefinitions(Resource resource) throws BeanDefinitionStoreException {
   		// 将读取 xml 资源进行编码处理，并加载
   		return loadBeanDefinitions(new EncodedResource(resource));
   	}
   ```

   ```java
   public int loadBeanDefinitions(EncodedResource encodedResource) throws BeanDefinitionStoreException {
   		Assert.notNull(encodedResource, "EncodedResource must not be null");
   		if (logger.isInfoEnabled()) {
   			logger.info("Loading XML bean definitions from " + encodedResource.getResource());
   		}
   
   		Set<EncodedResource> currentResources = this.resourcesCurrentlyBeingLoaded.get();
   		if (currentResources == null) {
   			currentResources = new HashSet<>(4);
   			this.resourcesCurrentlyBeingLoaded.set(currentResources);
   		}
   		if (!currentResources.add(encodedResource)) {
   			throw new BeanDefinitionStoreException(
   					"Detected cyclic loading of " + encodedResource + " - check your import definitions!");
   		}
   		try {
   			// 获取 IO 流
   			InputStream inputStream = encodedResource.getResource().getInputStream();
   			try {
   				// 获取 XML 解析源
   				InputSource inputSource = new InputSource(inputStream);
   				if (encodedResource.getEncoding() != null) {
   					inputSource.setEncoding(encodedResource.getEncoding());
   				}
   				// 具体加载 BeanDefinitions 过程
   				return doLoadBeanDefinitions(inputSource, encodedResource.getResource());
   			}
   			finally {
   				inputStream.close();
   			}
   		}
   		catch (IOException ex) {
   				...
   		}
   		finally {
   				...
   			}
   		}
   	}
   ```

9. **XmlBeanDefinitionReader** 的 **doLoadBeanDefinitions()** 具体加载 BeanDefinition，其中 **doLoadDocument()** 负责解析 Document 对象

   ```java
   protected int doLoadBeanDefinitions(InputSource inputSource, Resource resource)
   			throws BeanDefinitionStoreException {
   		try {
   			// 解析 xml 文件，形成 Document
   			Document doc = doLoadDocument(inputSource, resource);
   			// 通过解析 Document 对象并获取新注册的 BeanDefinition 数量
   			return registerBeanDefinitions(doc, resource);
   		}
   		catch (BeanDefinitionStoreException ex) {
   	     ...
       }
   }
   ```

10. **XmlBeanDefinitionReader** 的 **registerBeanDefinitions()** 创建 BeanDefinitionDocumentReader，由该类的子类DefaultBeanDefinitionDocumentReader 去完成注册 BeanDefinition

    ```java
    	public int registerBeanDefinitions(Document doc, Resource resource) throws BeanDefinitionStoreException {
    		// 创建 BeanDefinitionDocumentReader 对象用来解析 Document 对象，完成 BeanDefinition 解析
    		BeanDefinitionDocumentReader documentReader = createBeanDefinitionDocumentReader();
    		// 获取已注册的 BeanDefinition 数量
    		// getRegistry() 获取的是 DefaultListableBeanFactory 实例，因为在最初 XmlBeanFactory 继承的就是该类
    		int countBefore = getRegistry().getBeanDefinitionCount();
    		// 注册 BeanDefinition 具体过程，使用 子类 DefaultBeanDefinitionDocumentReader 实现该过程
    		documentReader.registerBeanDefinitions(doc, createReaderContext(resource));
    		// 统计新注册的 BeanDefinition 数量
    		return getRegistry().getBeanDefinitionCount() - countBefore;
    	}
    ```

11. **DefaultBeanDefinitionDocumentReader** 的 **registerBeanDefinitions()**，具体工作由 **doRegisterBeanDefinitions()** 采用委托模式来完成解析 BeanDefinition 工作

    ```java
    	@Override
    	public void registerBeanDefinitions(Document doc, XmlReaderContext readerContext) {
    		this.readerContext = readerContext;
    		logger.debug("Loading bean definitions");
    		// 获取 <beans> 标签元素
    		Element root = doc.getDocumentElement();
    		// 解析并注册 BeanDefinition
    		doRegisterBeanDefinitions(root);
    	}
    ```

    ```java
    protected void doRegisterBeanDefinitions(Element root) {
    		// Any nested <beans> elements will cause recursion in this method. In
    		// order to propagate and preserve <beans> default-* attributes correctly,
    		// keep track of the current (parent) delegate, which may be null. Create
    		// the new (child) delegate with a reference to the parent for fallback purposes,
    		// then ultimately reset this.delegate back to its original (parent) reference.
    		// this behavior emulates a stack of delegates without actually necessitating one.
    		// 采用委托模式，具体的 BeanDefinition 解析由 BeanDefinitionParserDelegate 完成
    		BeanDefinitionParserDelegate parent = this.delegate;
    		this.delegate = createDelegate(getReaderContext(), root, parent);
    
    		if (this.delegate.isDefaultNamespace(root)) {
    			String profileSpec = root.getAttribute(PROFILE_ATTRIBUTE);
    			if (StringUtils.hasText(profileSpec)) {
    				// 解析 xml 文件的 <beans> 标签内 profile 属性环境配置
    				String[] specifiedProfiles = StringUtils.tokenizeToStringArray(
    						profileSpec, BeanDefinitionParserDelegate.MULTI_VALUE_ATTRIBUTE_DELIMITERS);
    				if (!getReaderContext().getEnvironment().acceptsProfiles(specifiedProfiles)) {
    					if (logger.isInfoEnabled()) {
    						logger.info("Skipped XML bean definition file due to specified profiles [" + profileSpec +
    								"] not matching: " + getReaderContext().getResource());
    					}
    					return;
    				}
    			}
    		}
    
    		// 在我们开始处理bean定义之前，首先通过处理任何自定义元素类型，允许XML可扩展。
    		// 此方法是XML的任何其他自定义预处理的自然*扩展点。
    		// 默认实现为空。例如，子类可以重写此方法以将自定义元素转换为标准的Spring bean定义。
    		// 实现者可以通过相应的访问器访问解析器的bean定义读取器和底层XML资源
    		preProcessXml(root);
    		parseBeanDefinitions(root, this.delegate);
    		postProcessXml(root);
    
    		this.delegate = parent;
    	}
    ```

    **DefaultBeanDefinitionDocumentReader** 的 **parseBeanDefinitions()** 从根标签开始解析

    ```java
    protected void parseBeanDefinitions(Element root, BeanDefinitionParserDelegate delegate) {
    		if (delegate.isDefaultNamespace(root)) {
    			// 获取根元素下所有子节点（bean、import、alias 和其他自定义标签 context、aop 等）
    			NodeList nl = root.getChildNodes();
    			for (int i = 0; i < nl.getLength(); i++) {
    				Node node = nl.item(i);
    				if (node instanceof Element) {
    					Element ele = (Element) node;
    					// 判断是否符合 Spring xml 命令规则
    					if (delegate.isDefaultNamespace(ele)) {
    						// bean、import、alias 等标签使用默认解析
    						parseDefaultElement(ele, delegate);
    					}
    					else {
    						// context、aop 等标签使用自定义解析
    						delegate.parseCustomElement(ele);
    					}
    				}
    			}
    		}
    		else {
    			delegate.parseCustomElement(root);
    		}
    	}
    ```

    **DefaultBeanDefinitionDocumentReader** 的 **parseDefaultElement()** 主要核心方法 **processBeanDefinition()** 用来解析 <bean> 等等默认的子标签

    ```java
    private void parseDefaultElement(Element ele, BeanDefinitionParserDelegate delegate) {
    		if (delegate.nodeNameEquals(ele, IMPORT_ELEMENT)) {
    			importBeanDefinitionResource(ele);
    		}
    		else if (delegate.nodeNameEquals(ele, ALIAS_ELEMENT)) {
    			processAliasRegistration(ele);
    		}
    		else if (delegate.nodeNameEquals(ele, BEAN_ELEMENT)) {
    			// 处理 <bean> 标签
    			processBeanDefinition(ele, delegate);
    		}
    		else if (delegate.nodeNameEquals(ele, NESTED_BEANS_ELEMENT)) {
    			// recurse
    			doRegisterBeanDefinitions(ele);
    		}
    	}
    ```

    **DefaultBeanDefinitionDocumentReader** 的 **processBeanDefinition()** 解析并创建 BeanDefinition，核心方法为 **parseBeanDefinitionElement()** 

    ```java
    protected void processBeanDefinition(Element ele, BeanDefinitionParserDelegate delegate) {
    		// 解析 <bean> 并封装 BeanDefinition 到 BeanDefinitionHolder 对象中
    		BeanDefinitionHolder bdHolder = delegate.parseBeanDefinitionElement(ele);
    		if (bdHolder != null) {
    			// 装潢模式
    			bdHolder = delegate.decorateBeanDefinitionIfRequired(ele, bdHolder);
    			try {
    				// Register the final decorated instance.
    				BeanDefinitionReaderUtils.registerBeanDefinition(bdHolder, getReaderContext().getRegistry());
    			}
    			catch (BeanDefinitionStoreException ex) {
    				getReaderContext().error("Failed to register bean definition with name '" +
    						bdHolder.getBeanName() + "'", ele, ex);
    			}
    			// Send registration event.
    			getReaderContext().fireComponentRegistered(new BeanComponentDefinition(bdHolder));
    		}
    	}
    ```

12. **BeanDefinitionParserDelegate** 的 **parseBeanDefinitionElement()** 调用重载方法 **parseBeanDefinitionElement()** 创建 BeanDefinition 对象并解析

    ```java
    @Nullable
    	public BeanDefinitionHolder parseBeanDefinitionElement(Element ele, @Nullable BeanDefinition containingBean) {
    		// 解析 <bean> 标签属性
    		String id = ele.getAttribute(ID_ATTRIBUTE);
    		String nameAttr = ele.getAttribute(NAME_ATTRIBUTE);
    
    		// 处理别名
    		List<String> aliases = new ArrayList<>();
    		if (StringUtils.hasLength(nameAttr)) {
    			String[] nameArr = StringUtils.tokenizeToStringArray(nameAttr, MULTI_VALUE_ATTRIBUTE_DELIMITERS);
    			aliases.addAll(Arrays.asList(nameArr));
    		}
    
    		String beanName = id;
    		if (!StringUtils.hasText(beanName) && !aliases.isEmpty()) {
    			beanName = aliases.remove(0);
    			if (logger.isDebugEnabled()) {
    				logger.debug("No XML 'id' specified - using '" + beanName +
    						"' as bean name and " + aliases + " as aliases");
    			}
    		}
    
    		if (containingBean == null) {
    			checkNameUniqueness(beanName, aliases, ele);
    		}
    
    		// 根据 xml 元素 解析并封装 AbstractBeanDefinition 对象
    		AbstractBeanDefinition beanDefinition = parseBeanDefinitionElement(ele, beanName, containingBean);
    		if (beanDefinition != null) {
    			if (!StringUtils.hasText(beanName)) {
    				try {
    					if (containingBean != null) {
    						beanName = BeanDefinitionReaderUtils.generateBeanName(
    								beanDefinition, this.readerContext.getRegistry(), true);
    					}
    					else {
    						beanName = this.readerContext.generateBeanName(beanDefinition);
    						// Register an alias for the plain bean class name, if still possible,
    						// if the generator returned the class name plus a suffix.
    						// This is expected for Spring 1.2/2.0 backwards compatibility.
    						String beanClassName = beanDefinition.getBeanClassName();
    						if (beanClassName != null &&
    								beanName.startsWith(beanClassName) && beanName.length() > beanClassName.length() &&
    								!this.readerContext.getRegistry().isBeanNameInUse(beanClassName)) {
    							aliases.add(beanClassName);
    						}
    					}
    					if (logger.isDebugEnabled()) {
    						logger.debug("Neither XML 'id' nor 'name' specified - " +
    								"using generated bean name [" + beanName + "]");
    					}
    				}
    				catch (Exception ex) {
    					error(ex.getMessage(), ele);
    					return null;
    				}
    			}
    			String[] aliasesArray = StringUtils.toStringArray(aliases);
    			return new BeanDefinitionHolder(beanDefinition, beanName, aliasesArray);
    		}
    
    		return null;
    	}          
    ```

    **BeanDefinitionParserDelegate** 的重载 **parseBeanDefinitionElement()** 调用 **createBeanDefinition()** 创建 BeanDefinition 并使用 **parsePropertyElements()** 解析 property 属性

    ```java
    	@Nullable
    	public AbstractBeanDefinition parseBeanDefinitionElement(
    			Element ele, String beanName, @Nullable BeanDefinition containingBean) {
    
    		this.parseState.push(new BeanEntry(beanName));
    
    		String className = null;
    		if (ele.hasAttribute(CLASS_ATTRIBUTE)) {
    			className = ele.getAttribute(CLASS_ATTRIBUTE).trim();
    		}
    		String parent = null;
    		if (ele.hasAttribute(PARENT_ATTRIBUTE)) {
    			parent = ele.getAttribute(PARENT_ATTRIBUTE);
    		}
    
    		try {
    			AbstractBeanDefinition bd = createBeanDefinition(className, parent);
    
    			// 解析 singleton、scope、abstract、lazy-init、autowire 等属性
    			parseBeanDefinitionAttributes(ele, beanName, containingBean, bd);
    			bd.setDescription(DomUtils.getChildElementValueByTagName(ele, DESCRIPTION_ELEMENT));
    			// 解析 meta 属性
    			parseMetaElements(ele, bd);
    			parseLookupOverrideSubElements(ele, bd.getMethodOverrides());
    			parseReplacedMethodSubElements(ele, bd.getMethodOverrides());
    
    			// 解析 constructor-arg 构造参数
    			parseConstructorArgElements(ele, bd);
    			// 解析 property 属性
    			parsePropertyElements(ele, bd);
    			parseQualifierElements(ele, bd);
    
    			bd.setResource(this.readerContext.getResource());
    			bd.setSource(extractSource(ele));
    
    			return bd;
    		}
    		catch (ClassNotFoundException ex) {
    			...
    		}
    		finally {
    			this.parseState.pop();
    		}
    
    		return null;
    	}
    ```

    **BeanDefinitionParserDelegate** 的 **createBeanDefinition()** 最终调用 **BeanDefinitionReaderUtils.createBeanDefinition()** 创建 AbstractBeanDefinition ，其中 **ClassUtils.forName(className, classLoader)** 使用反射处理。

    ```java
    public static AbstractBeanDefinition createBeanDefinition(
    			@Nullable String parentName, @Nullable String className, @Nullable ClassLoader classLoader) throws ClassNotFoundException {
    
    		// 创建标准 BeanDefinition 对象
    		GenericBeanDefinition bd = new GenericBeanDefinition();
    		bd.setParentName(parentName);
    		if (className != null) {
    			if (classLoader != null) {
    				bd.setBeanClass(ClassUtils.forName(className, classLoader));
    			}
    			else {
    				bd.setBeanClassName(className);
    			}
    		}
    		return bd;
    	}
    ```

    **BeanDefinitionParserDelegate** 的 **parsePropertyElements()** 遍历解析 <property> 标签

    ```java
    public void parsePropertyElements(Element beanEle, BeanDefinition bd) {
    		NodeList nl = beanEle.getChildNodes();
    		for (int i = 0; i < nl.getLength(); i++) {
    			Node node = nl.item(i);
    			if (isCandidateElement(node) && nodeNameEquals(node, PROPERTY_ELEMENT)) {
    				parsePropertyElement((Element) node, bd);
    			}
    		}
    	}
    
    
    public void parsePropertyElement(Element ele, BeanDefinition bd) {
    		String propertyName = ele.getAttribute(NAME_ATTRIBUTE);
    		if (!StringUtils.hasLength(propertyName)) {
    			error("Tag 'property' must have a 'name' attribute", ele);
    			return;
    		}
    		this.parseState.push(new PropertyEntry(propertyName));
    		try {
    			if (bd.getPropertyValues().contains(propertyName)) {
    				error("Multiple 'property' definitions for property '" + propertyName + "'", ele);
    				return;
    			}
    			// 主要解析 ref、value 等类型。
    			// ref 引用标签封装为 RuntimeBeanReference 返回
    			// value 标签封装为 TypedStringValue 返回
    			Object val = parsePropertyValue(ele, bd, propertyName);
    			// val 可能是引用类型，也可能为普通 value 类型
    			PropertyValue pv = new PropertyValue(propertyName, val);
    			// 解析 <property> 标签的 k，v 值，并将该标签内子元素对应的数据存储 BeanMetadataAttribute(BeanDefinition 的基础类)
    			parseMetaElements(ele, pv);
    			pv.setSource(extractSource(ele));
    			// 将解析完的 PropertyValue 存储到 BeanDefinition
    			bd.getPropertyValues().addPropertyValue(pv);
    		}
    		finally {
    			this.parseState.pop();
    		}
    	}
    ```

    

    





​	

## CustomIoC 

### 基础流程分析

1. 创建一级接口 BeanFactory，定义获取 bean instance 最基本方法。

2. 采用设计原则的接口隔离原则，设计二级接口，增加部分功能。（该模块使用 Abstract Class 代理。）

3. 使用默认实现类 **DefaultListableBeanFactory** 实现一、二级接口功能。

   `以上三个步骤实现了 getBean() 功能`

4. **DefaultListableBeanFactory** 内需要使用数据结构存储 bean 的相关信息及实例。所以根据 xml 标签可以看出来，
   最好的数据结构为 Map。
   
   此处以 **SpringBasic** 模块配置文件为例。
   
    ```xml
    <!-- 配置 Hikari -->
    <bean id="hikariConfig" class="com.zaxxer.hikari.HikariConfig" init-method="getDataSource">
        <property name="jdbcUrl" value="${datasource.url}"/>
        <property name="poolName" value="${datasource.poolName}"/>
        <property name="username" value="${datasource.username}"/>
        <property name="password" value="${datasource.password}"/>
        <property name="driverClassName" value="${datasource.driverClassName}"/>
        <property name="maximumPoolSize" value="${datasource.maximumPoolSize}"/>
        <property name="maxLifetime" value="${datasource.max-leftTime}"/>
    </bean>

    <!-- 注册 DataSource -->
    <bean id="dataSource" class="com.zaxxer.hikari.HikariDataSource">
        <constructor-arg ref="hikariConfig"/>
    </bean>

    <!-- 配置事务 -->
    <bean id="transactionManager" class="org.springframework.jdbc.datasource.DataSourceTransactionManager">
        <property name="dataSource" ref="dataSource" />
    </bean>
   
    ``` 

    根据以上 xml 配置信息，可以简单推导出 **DefaultListableB** 中应该有两个 Map 结构。
   
    存储解析的 xml 信息 **Map<String, BeanDefinition>** 其中 **BeanDefinition** 封装了 xml 中 *'<bean'>* 标签信息
   
    存储 bean 的实例 **Map<String, Object> singletonBeanMap**
   
5. **BeanDefinition** 应该存储标签中的 id、class、init-method 以及 **PropertyValue** 封装的 *'<property'>* 标签信息

6. **PropertyValue** 存储的 *value* 必须是 Object 类型，因为 *'<property'>* 的 *name* 是固定的，但是 *value* 和 *ref* 都是 String 类型。
   所以 *value* 应该有 **TypeStringValue** 封装类来存储 value 的**属性值和属性数据类型**（根据类型、值进行**依赖注入**），*ref* 使用 **RuntimeBeanReference** 来存储字符串（*ref* 在创建 bean instance 时，通过反射获取 ref 的 ClassName 并进行实例创建）。
   
7. 使用加载 xml 方式，使用 **Resource** 接口获取 IO 流进行解析。     

--- 

## AOP 三种实现方式

### 纯 XML 方式配置 AOP
    
- 针对具体核心业务逻辑，定义一个通知类
    
    - 根据具体核心业务场景，针对具体类或具体方法，选择相应的通知方式
    
        - before 目标方法调用前，执行此 advice
                                        
        - around 目标方法调用前、调用后处理，执行此 advice（当抛出异常，立即退出，会转向 after advice，执行完转到 throwing advice）
        
        - after 目标方法正常结束、异常都执行此 advice
                                                                                                                                                                                                                      
        - after returning 目标方法调用正常结束，不管有无返回结果，执行此 advice
                                                                                                                                                                                                                      
        - after throwing 目标方法执行异常退出，执行此 advice
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    
- xml 配置                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        
    
    - 参考 SpringBasic 模块
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              
        - XMLAdvice 和 spring-aop.xml，测试通知在 UserServiceImpl 需要打开或关闭人为异常。


### XML 和注解混合

- 定义一个切面类

    - 定义通知方式
        
        - @Before
                              
        - @Around
                                                                                       
        - @After
                                                                                                                                                                                                         
        - @AfterReturning(pointcut="..")
                                                                                                                                                                                                                                                                                                                                                                                                                                            
        - @AfterThrowing(pointcut="..",throwing="方法内的 throwable 参数")
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          
            其中 @AfterReturning 和 @AfterThrowing 中，既可以用 pointcut 也可以使用 value
            具体区别为：两者都定义，pointcut 优先于 value，
            如果只定义 value 则可以省略（@Before、@Around、@After 可以省略 value）。
            
- xml 配置

    - 参考 SpringBasic 模块
    
        - SimpleAspect 和 spring-annotation.xml，测试通知在 UserServiceImpl 需要打开或关闭人为异常。
    
        - 注意 SimpleAspect 中 @Pointcut 注解及 commonPointcut 方法用法。                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             

### 纯注解

 - 使用注解开启 AOP 
 
     - @Configuration
      
     - @EnableAspectJAutoProxy
      
     - @ComponentScan(basePackages=""")

 - xml 项目模块，所以纯注解未实现。

----

### 部分配置区别

  ```xml
    
     <!-- 定义切面，aspect 标签用法 -->
     <aop:config>
         <aop:aspect ref="xmlAdvice">
             <aop:pointcut id="userPointcut" expression="execution(* io.stayhungrystayfoolish.aop.service..*.*(..))" />
             <aop:before method="xmlBeforeAdvice" pointcut-ref="userPointcut" />
         </aop:aspect>  
     </aop:config>
     
     <!-- 定义通知器，advisor 标签用法 -->                      
     <aop:config>
         <aop:pointcut id="userPointcut" expression="execution(* io.stayhungrystayfoolish.aop.service..*.*(..))" />
         <aop:advisor advice-ref="xmlBeforeAdvice" pointcut-ref="userPointcut" />
     </aop:config>
```

        
    
 - 如果使用 aspect 标签，在 XMLAdvice 中直接定义一般方法即可。

 - 如果使用 advisor 标签，在 XMLAdvice 中则需要实现类似 MethodBeforeAdvice、AfterReturningAdvice 等接口，在实现方法中分别定义通知方式。 
 
 - 推荐使用 aop:aspect 标签       

### Pointcut 语法

- execution(* io.stayhungrystayfoolish.aop.service..*.*(..))

  - execution() 表达式主体

  - 第一个 * 符号，返回值任意类型
  
  - io.stayhungrystayfoolish.aop.service 包名

  - 第一个 .. 表示当前包及其子包

  - 第二个 * 符号表示所有类

  - .*(..)  第三个 * 符号表示任意方法名 (..) 表示任意参数类型、参数个数

  **其他指示符如 within、this、target、args、@within、@target、@args 等等，并且支持 &&、||、! 逻辑符号**

---

## 事务 Transactional 的传播机制与隔离级别

- Spring 的声明式事务管理在底层是建立在 AOP 的基础之上的。其本质是对方法**前后**进行拦截，然后在目标方法开始之前创建或者加入一个事务，在执行完目标方法之后根据执行情况提交或者回滚事务。

- 声明式事务最大的优点就是不需要通过编程的方式管理事务，这样就不需要在业务逻辑代码中掺杂事务管理的代码，只需在配置文件中做相关的事务规则声明（或通过等价的基于标注的方式），便可以将事务规则应用到业务逻辑中。因为事务管理本身就是一个典型的横切逻辑，正是 AOP 的用武之地。Spring 开发团队也意识到了这一点，为声明式事务提供了简单而强大的支持。
  
- 声明式事务管理曾经是 EJB 引以为傲的一个亮点， Spring 让 POJO 在事务管理方面也拥有了和 EJB 一样的待遇，让开发人员在 EJB 容器之外也用上了强大的声明式事务管理功能，这主要得益于 Spring 依赖注入容器和 Spring AOP 的支持。依赖注入容器为声明式事务管理提供了基础设施，使得 Bean 对于 Spring 框架而言是可管理的；而 Spring AOP 则是声明式事务管理的直接实现者。
  
- 建议在开发中使用声明式事务，不仅因为其简单，更主要是因为这样使得纯业务代码不被污染，极大方便后期的代码维护。和编程式事务相比，声明式事务唯一不足地方是，后者的最细粒度只能作用到方法级别，无法做到像编程式事务那样可以作用到代码块级别。但是即便有这样的需求，也存在很多变通的方法，比如，可以将需要进行事务管理的代码块独立为方法等等。

- **<proxy-target-class="false/true"> 作用**

  1. **proxy-target-class** 属性值来控制是基于接口的还是基于类的代理被创建。 

  2. <tx:annotation-driven transaction-manager=“transactionManager” proxy-target-class=“true”/> 

     如果 proxy-target-class 属性值被设置为 **true**，那么基于类的代理将起作用（这时需要 CGLIB 库）。
     如果 proxy-target-class 属值被设置为 **false** 或者这个属性被省略，那么标准的JDK 基于接口的代理。
     注解 @Transactional CGLIB 与 JAVA 动态代理最大区别是代理目标对象不用实现接口，那么注解要是写到接口方法上，要是使用 CGLIB 代理，这是注解事物就失效了，为了保持兼容注解最好都写到实现类方法上。
     Spring 团队建议在具体的类（或类的方法）上使用 @Transactional 注解，而不要使用在类所要实现的任何接口上。在接口上使用 @Transactional 注解，只能当你设置了基于接口的代理时它才生效。
     因为注解是不能继承的，这就意味着如果正在使用基于类的代理时，那么事务的设置将不能被基于类的代理所识别，而且对象也将不会被事务代理所包装。
     @Transactional 的事务开启 ，或者是基于接口的 或者是基于类的代理被创建。所以在同一个类中一个方法调用另一个方法有事务的方法，事务是不会起作用的。
      
     **原因：**（这也是为什么在项目中有些 @Async 并没有异步执行） Spring 在扫描 Bean 的时候会扫描方法上是否包含 @Transactional 注解，
     如果包含，Spring 会为这个 Bean 动态地生成一个子类（即代理类，proxy），代理类是继承原来那个 Bean 的。
     此时，当这个有注解的方法被调用的时候，实际上是由代理类来调用的，代理类在**调用之前**就会启动 Transaction。
     然而，如果这个有注解的方法是被同一个类中的其他方法调用的，那么该方法的调用并没有通过代理类，而是直接通过原来的那个bean，
     所以就不会启动 Transaction，我们看到的现象就是 @Transactional 注解无效。
   
  3. 注解形式 @EnableTransactionManagement(proxyTargetClass = false)

     默认为 false，使用 JDK 动态代理。同理 @EnableAspectJAutoProxy 也拥有该属性。   

---

### 事务传播机制

  ```java
   public enum Propagation {
       REQUIRED(0),
       SUPPORTS(1),
       MANDATORY(2),
       REQUIRES_NEW(3),
       NOT_SUPPORTED(4),
       NEVER(5),
       NESTED(6);
   
       private final int value;
   
       private Propagation(int value) {
           this.value = value;
       }
   
       public int value() {
           return this.value;
       }
   }
```

1. REQUIRED （事务传播默认机制）
    
    如果有事务，则加入事务，没有新建。
    
   （ClassA methodA 方法调用 ClassB methodB 方法，如果 methodA 前不存在任何事务，开启一个新事务，methodA 调用 methodB 时，事务已经存在，methodB 会使用已存在的事务）
    
2. SUPPORTS **（不推荐使用）**

    其他 Bean 调用该事务所在方法，其他 Bean 中声明事务则用启用事务，若其他 Bean 没有，则不启用事务。
   
3. MANDATORY

    必须在已有事务中执行，否则抛出异常。
    
   （使用该传播机制的事务，不能单独对外提供，因为只能存在于一个已经开启事务的方法中。）
    
4. REQUIRES_NEW **（外部事务和内部事务互相独立）**

    无论是否存在事务，都会新建事务，原事务挂起，新事务执行完毕，继续执行原事务。
    
    内部的事务独立运行，在各自的作用域中，可以独立的回滚或者提交；而外部的事务将不受内部事务的回滚状态影响。 
    
5. NOT_SUPPORTED

    不开启事务，并挂起已存在事务。
    
6. NEVER

    必须在无事务中执行，否则抛出异常。如果存在一个活动事务，抛出异常。
    
7. NESTED **（内部事务和外部事务有关联，会互相影响）**

    如果一个事务存在，则运行在一个嵌套的事务中。如果没有按照 REQUIRED 执行。
    NESTED 基于单一的事务来管理，提供了多个保存点。
    这种多个保存点的机制允许内部事务的变更触发外部事务的回滚。
    而外部事务在混滚之后，仍能继续进行事务处理，即使部分操作已经被回滚。 
    由于这个设置基于 JDBC 的保存点，所以只能工作在 JDBC 的机制智商。
    由此可知，两者都是事务嵌套，不同之处在于，内外事务之间是否存在彼此之间的影响；
    
    NESTED 之间会受到影响，而产生部分回滚，而 REQUIRED_NEW 则是独立的。

- **常见事务失效原因**

  1. 抛出异常不属于 RunTimeException （unchecked 异常）及其子类

  2. 异常在该方法被 try...catch，如果需要 try...catch，则在 catch 内使用 throw 也可以使事务重新生效。

  3. 同一类中 Method_A 调用 Method_B 不会使事务生效。
     前提：类上没有 @Transactional 注解，且 Method_A 对外方法没有 @Transactional 注解，只在 Method_B 方法上加该注解。
     则 Method_A 对外方法事务不生效，因为 @Transactional 注解只能用在对外方法上。
   
       测试事务自调用，Method_A 调用 Method_B 方法，如需事务生效，需要打开此配置
       
       前提：Method_A 没有事务注解，且 Method_B 有事务注解，
       需要将 Method_B 注解改为 @Transactional(propagation = Propagation.REQUIRES_NEW)
       并且将原调用 methodB() 更改为 ((CurrentClass)AopContext.currentProxy()).methodB() 才可以生效  
       <aop:aspectj-autoproxy expose-proxy="true"/>
       
   **若类上加 @Transactional，则 Method_A 调用 Method_B 事务生效。**
   
   **详细可参考 https://zhuanlan.zhihu.com/p/35483036** 

4. 非 Public 方法。

5. 配置错误（参考 spring-aop.xml）

---

### 事务隔离级别

  ```java
    public enum Isolation {
        DEFAULT(-1),
        READ_UNCOMMITTED(1),
        READ_COMMITTED(2),
        REPEATABLE_READ(4),
        SERIALIZABLE(8);
    
        private final int value;
    
        private Isolation(int value) {
            this.value = value;
        }
    
        public int value() {
            return this.value;
        }
    }
```

1. DEFAULT （默认隔离级别）
    
    采用数据源的默认隔离级别。
    
    **MySQL 的 InnoDB 引擎，那么级别就是 REPEATABLE_READ**
    **Oracle，隔离级别就是 READ_COMMITTED**
    
2. READ_UNCOMMITTED 

    读取未提交数据（会出现脏读, 不可重复读） 
    
   （以操作同一行数据为前提，读事务允许其他读事务和写事务，未提交的写事务**禁止**其他写事务（但允许其他读事务）。
    此隔离级别可以防止更新丢失，但不能防止脏读、不可重复读、幻读。此隔离级别可以通过**排他写锁**实现。）
    **不推荐使用**
    
3. READ_COMMITTED

    读取已提交数据（会出现不可重复读和幻读）
    
    以操作同一行数据为前提，读事务允许其他读事务和写事务，未提交的写事务**禁止**其他读事务和写事务。
    此隔离级别可以**防止更新丢失、脏读，但不能防止不可重复读、幻读**。此隔离级别可以通过**瞬间共享读锁**和**排他写锁**实现。
    
4. REPEATABLE_READ

   可重复读（会出现幻读）
   
   以操作同一行数据为前提，读事务禁止其他写事务（但允许其他读事务），未提交的写事务禁止其他读事务和写事务。
   此隔离级别可以**防止更新丢失、脏读、不可重复读，但不能防止幻读**。此隔离级别可以通过**共享读锁**和**排他写锁**实现。     
          
5. SERIALIZABLE

    序列号级别
    
    提供严格的事务隔离。它要求事务序列化执行，事务只能一个接着一个地执行，不能并发执行。
    此隔离级别可以防止**更新丢失、脏读、不可重复读、幻读**。
    如果仅仅通过**行级锁**是无法实现事务序列化的，必须通过其他机制保证新插入的数据不会被刚执行查询操作的事务访问到。

 隔离级别越高，越能保证数据的完整性和一致性，但是对并发性能的影响也越大。 
  
 对于多数应用程序，可以优先考虑把数据库系统的隔离级别设为 **READ_COMMITTED**。它能够避免更新丢失、脏读，而且具有较好的并发性能。
 
 尽管它会导致不可重复读、幻读这些并发问题，在可能出现这类问题的个别场合，可以由应用程序采用悲观锁或乐观锁来控制。

---

### 事务关键词

- **原子性（Atomicity）**

    事务是数据库的逻辑工作单位，它对数据库的修改要么全部执行，要么全部不执行。
    
- **一致性（Consistency）** 
    
    事务前后，数据库的状态都满足所有的完整性约束。

- **隔离性（Isolation）**

    并发执行的事务是隔离的，一个不影响一个。
    如果有两个事务，运行在相同的时间内，执行相同的功能，事务的隔离性将确保每一事务在系统中认为只有该事务在使用系统。
    这种属性有时称为串行化，为了防止事务操作间的混淆，必须串行化或序列化请求，使得在同一时间仅有一个请求用于同一数据。
    通过设置数据库的**隔离级别**，可以达到不同的隔离效果。
    
- **持久性（Durability）**

    在事务完成以后，该事务所对数据库所作的更改便持久的保存在数据库之中，并不会被回滚。
    
- **更新丢失**

    两个事务都同时更新一行数据，但是第二个事务却中途失败退出，导致对数据的两个修改都失效了。
    这是因为系统没有执行任何的锁操作，因此并发事务并没有被隔离开来。    
        
- **脏读**

    脏读又称无效数据读出。一个事务读取另外一个事务还**未提交**的数据叫**脏读**。
    例如：事务 T1 修改了一行数据，但是还没有提交，这时候事务 T2 读取了被事务 T1 修改后的数据，
    之后事务 T1 因为某种原因 Rollback 了，那么事务 T2 读取的数据就是脏的。  
    
- **不可重复读**

    不可重复读是指在同一个事务内，两个相同的查询返回了**不同的结果**。
    
    例如：事务 T1 读取某一数据，事务 T2 读取并修改了该数据，T1 为了对读取值进行检验而再次读取该数据，便得到了不同的结果。
    
- **幻读**

    事务在操作过程中进行两次查询，第二次查询的结果包含了第一次查询中未出现的数据或者缺少了第一次查询中出现的数据
    
    例如：系统管理员A将数据库中所有学生的成绩从具体分数改为ABCDE等级，但是系统管理员B就在这个时候插入了一条具体分数的记录，
    当系统管理员A改结束后发现还有一条记录没有改过来，就好像发生了幻觉一样。这就叫幻读。
                 
  **注意：**  使用 for update 实现悲观锁的时候，需要注意锁的级别，MySQL InnoDB 默认行级锁。行级锁都是基于索引的，如果一条sql语句用不到索引，是不会使用行级锁的，会使用表级，把整张表锁住。

  **注意：**  使用乐观锁时多数实现方法是使用版本号，或者时间戳。但是如果事务的隔离级别允许重复读（比如：REPEATABLE_READ；MySQL InnoDB 默认也是这个级别），那么使用乐观锁是查询不出版本或者时间戳的变化的，但是 Oracle 的话默认就可以。

---

### 事务超时问题

- @Transactional 注释不适用于 JDBC **读取超时异常**。

- Spring 事务和数据库事务超时时间相互影响，官方 default 为-1，不限制超时时间。

- 超时通过 deadLine 和 JDBC 的 Statement 和 SetQueryTime 两种策略来判断超时
  
  Spring事务超时 = 事务开始时到最后一个 Statement 创建时时间 + 最后一个 Statement 的执行时超时时间（即其 QueryTimeout）。
  
  源码 **DataSourceUtils.applyTimeout()**
  
- **参考文章**：
    
    https://www.cubrid.org/blog/understanding-jdbc-internals-and-timeout-configuration
    
    https://codete.com/blog/5-common-spring-transactional-pitfalls/
    
    http://www.heartthinkdo.com/?p=910
      
    https://benjaminwhx.com/2018/02/01/JDBC%E8%B6%85%E6%97%B6%E9%97%AE%E9%A2%98%E5%85%A8%E9%9D%A2%E5%88%86%E6%9E%90/
 
 ----
 
