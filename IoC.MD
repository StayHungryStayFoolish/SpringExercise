# Spring 类图关系、IoC 流程分析

## BeanFactory 继承体系 

![avatar](https://github.com/StayHungryStayFoolish/SpringIOCExercise/blob/master/BeanFactory.png?raw=true)

**层级关系简单说明**

- 一级接口
  - BeanFactory	定义最基本功能，例如：getBean()
- 二级接口
  - AutowireCapableBeanFactory**【侧重装配】**
    - 装配工厂，定义了一系列装配策略及方法
      - 5个常量定义了装备策略。例如：按名字、按类型、按构造方法装配
      - 8个跟自动装备相关的方法。例如：根据分别根据5个不同装配策略
      - 2个 BeanPostProcessor 相关的前置、后置处理方法。
      - 2个分解指定依赖的范发过。
  - HierarchicalBeanFactory**【侧重分级】**
    - 层级工厂，定义了两个功能：1.返回 Bean 的父工厂 2.判断本地工厂是否包含该 Bean
  - ListableBeanFactory**【侧重展示】**
    - 列出工厂的 Bean 相关数据。
      - 3个和 BeanDefinition 相关的方法。
      - 2个 getBeanNamesForType() 重载方法。根据指定类型（包括子类）获取子对应的所有 BeanName。
      - 2个 getBeanOfType() 重载方法。根据类型（包括子类）获取指定 BeanName 的 Bean 的 Map 集合。
      - 2个注解相关的方法。根据注解类型获取 BeanName 和 Bean 的 Map 集合，根据 BeanName 和类型获取指定的 Bean。
- 三级接口
  - ConfigurableBeanFactory**【侧重配置属性：如单例、原型】**
    - 继承二级接口 **HierarchicalBeanFactory**
    - 相关方法：类加载器（ClassLoader）、缓存元数据（cacheMetadata）、Bean 表达式分解器、相关属性注册及设置、Scope 相关操作、别名（Alias）、合并 BeanDefinition、销毁 Bean 等等众多操作。
  - ApplicationContext**【BeanFactory 的增强版，侧重上下文相关】**
    - 不同于 BeanFactory 很多功能必须以编程方式实现，更偏向于配置方式，提供了较为完整的框架功能。
    - 继承了几个比较重要的接口，如：
      - MessageSource	提供了国际化 i18n 
      - ApplicationEvenetPublisher	拥有容器上下文关系，启动、关闭时间等等。
      - ResourcePatternResovler	加载资源   
- 核心接口
    - DefaultListableBeanFactory
        - 实现了所有父级工厂的方法，BeanFactory 的集大成者        

---

## BeanDefinition 继承体系

![avatar](https://github.com/StayHungryStayFoolish/SpringIOCExercise/blob/master/BeanDefinition.png?raw=true)

- BeanDefinition **继承的接口**
    - AttributeAccessor
        - 对任意对象元数据操作
    - BeanMetadataElement
        -  获取可配置的源对象
        
- BeanDefinition **【描述 Bean 实例】**
  - 2个标注 Bean Scope 的常量（singleton、prototype）。
  - 3个标注 Bean 创建由哪种角色创建。
  - BeanClassName、Scope、LazyInit、依赖的 Bean、MutablePropertyValues、父级工厂名字和方法、构造参数的 value 等相关操作。
  - **主要作用**
    - **可以理解为以 XML 方式定义的 Bean 在 IoC 容器内的表现形式**
    - 允许 **BeanFactoryPostProcessor** 在已经**初始化**，但**尚未实例化**的 Bean 覆盖或添加属性。比如实现类 **PropertyResourceConfigurer** 能够检索并修改属性值和别的 Bean 的元数据。

- RootBeanDefinition
  - 可以被 Merge 的 BeanDefinition。配置阶段进行在 BeanFactory 通常使用的类。在 Spring 4 以后，更好的选择是 GenericBeanDefinition。
- GenericBeanDefinition
  - 一站式标准的 BeanDefinition。除了具有指定类可选的构造参数值、属性参数或其它 BeanDefinition 一样的特性外，还具有parenetName属性，这也意味着可以用来灵活设置。
      
------

## BeanDefinition 相关类说明

- **属性相关**
  - **BeanWrapper** 及子类 **BeanWrapperImpl**（继承了 AbastactNestablePropertyAccwessor 所以有处理属性能力）    
    - **操作 Bean 属性**
  - **PropertyValue**     
    - 持有 Bean 的 **属性名称** 和 **属性值**的对象，对应 <property> 标签
  - **TypedStringValue**    
    - 持有 **value** 标签的对象
      - 对应 <property name="subject" value="Spring" /> 标签中的 value 标签
  - **RuntimeBeanReference**    
    - 持有 **ref** 标签的对象
      - 对应  <property name="subject" ref="Spring" /> 标签中的 ref 标签
- **解析相关**
  - **AbstractBeanDefinitionReader** 及子类 **XmlBeanDefinitionReader**、**PropertiesBeanDefinitionReader**
    - 解析 Xml 或 Properties 文件
  - **BeanDefinitionDocumentReader** 及子类 **DefaultBeanDefinitionDocumentReader**
    - 解析 Xml 文件封装的 Document 对象
- **委托相关**
  - **BeanDefinitionParserDelegate**
    - 负责 BeanDefinition 的具体工作

------

## Spring 钩子

- InitializingBean    允许 Bean 在**初始化中**后，可以自定义修改 Bean 属性	
- BeanFactoryPostProcessor    允许 Bean 在**初始化前**修改 Bean 属性，**执行时机：** 在 BeanDefinition 未被用来创建对象之前，可以针对 **BeanDefinition** 进行修改。。
    
    例： PropertyPlaceholderConfigurer(context:property-placeholder 标签)，判断该 BeanDefinition 中属性是否含有 ${}，如果有，根据 key 去获取对应文件的 value 值进行替换。
        
- BeanPostProcessor    允许 Bean 只能在**初始化后**修改 Bean 属性，**执行时机：** 在 Bean 已经创建完成属性填充后，在 Bean 初始化前后被调用。
    
    例： AbstractAspectJAutoProxyCreator 实现了 BeanPostProcessor，作用就是对已创建的 Bean 进行 AOP 切面操作。

- InstantiationAwareBeanPostProcessor    允许 Bean 在**实例化后**修改 Bean 属性。

参考文章：

[Spring 钩子方法和接口使用详解](http://www.sohu.com/a/166804449_714863)

[Spring 钩子 BeanFactoryPostProcessor 和 BeanPostProcessor 源码学习](https://www.jianshu.com/p/a90a3e617ba6)

---

## Bean 生命周期

![Spring Bean 生命周期](https://github.com/StayHungryStayFoolish/SpringExercise/blob/master/Bean%20%E7%94%9F%E5%91%BD%E5%91%A8%E6%9C%9F.png?raw=true)

## Spring IoC 

- Spring IoC 容器加载（BeanDefinition 注册）只是完成了 **BeanDefinition** 的注册，只是用 Map 数据结构维护了 BeanDefinition。

- **getBean** 向 IoC 容器索要 Bean 时，通过一系列的操作最终完成了**属性输入（DI）**

### Spring IoC 入口

- 基于 BeanFactory 加载 Bean 的入口

  ```java
   DefaultListableBeanFactory bf = new XmlBeanFactory(new ClassPathResource("applicationContext.xml"));
  ```

- 基于 ApplicationContext 加载 Bean 的入口

  ```java
   ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
  ```

## ApplicationContext 方式 Spring IoC 容器加载（BeanDefinition 注册）过程

### 类 + 方法扼要说明

![avatar](https://github.com/StayHungryStayFoolish/SpringIOCExercise/blob/master/BeanFactory-1.jpg?raw=true)

1. new ClassPathXmlApplicationContext # **构造方法**

2. AbstractApplicationContext # **refresh** 刷新 Spring IoC 容器（删除原容器，创建新容器）
   
3. **obtainFreshBeanFactory** 创建工厂并初始化，**以下为 BeanFactory 具体流程**
         
4. AbstractRefreshableApplicationContext # **refreshBeanFactory** 创建DefaultListableBeanFactory
   
5. AbstractXmlApplicationContext # **loadBeanDefinitions** 创建 XmlBeanDefinitionReader
   
6. **loadBeanDefinitions** 定位资源，使用 XmlBeanDefinitionReader 调用 loadBeanDefinitions
   
7. AbstractBeanDefinitionReader # **loadBeanDefinitions** 遍历资源，进入子类 XmlBeanDefinitionReader 的 loadBeanDefinitions
   
8. XmlBeanDefinitionReader # **loadBeanDefinitions** 获取 IO 流，设置资源编码，调用具体方法 doLoadBeanDefinitions

9. **doLoadBeanDefinitions**  通过 doLoadDocument 解析 Xml 文件，获取 Document 对象，调用 registerBeanDefinitions 注册 BeanDefinition

10. **registerBeanDefinitions** 创建 BeanDefinitionReader，并由子类 DefaultBeanDefinitionDocumentReader 实现 registerBeanDefinitions 方法。

11. DefaultBeanDefinitionDocumentReader # **registerBeanDefinitions** 获取 <beans> 标签，调用 doRegisterBeanDefinitions 来完成注册工作

12. **doRegisterBeanDefinitions**  采用委托模式由 BeanDefinitionParserDelegate 完成 BeanDefinition 解析工作，通过 parserBeanDefinitions 完成根标签解析

13. **parserBeanDefinitions** 解析<beans> 下所有子节点

14. **parserDefaultElement** 解析 <alias>、<bean>、<imoprt> 等，重要方法为解析 <bean> 标签的 processBeanDefinition

15. **processBeanDefinition** 使用委托模式 BeanDefinitionParserDelegate 调用  parseBeanDefinitionElement 封装

16. BeanDefinitionParserDelegate**parseBeanDefinitionElement** 调用重载方法 parseBeanDefinitionElement，内部再次调用 createBeanDefinition 方法解析并封装为 AbstractBeanDefinition 对象

16.1 **createBeanDefinition** 最终调用 BeanDefinitionReaderUtils.createBeanDefinition() 通过反射创建 AbstractBeanDefinition 对象

16.2 在 16.1 完成后调用**parserPropertyElements** 遍历 <property> 将解析的 PropertyValue 存入 BeanDefinition 



### 代码分析 

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

6. **AbstractXmlApplicationContext** 核心方法 **loadBeanDefinitions()**  主要创建 XmlBeanDefinitionReader (XML 阅读器)，并定位资源，再次内部调用 **loadBeanDefinitions()** 进行读取 xml 并加载 BeanDefinition

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

---

## Spring IoC 获取 Bean 实例过程

### 类 + 方法扼要说明

![avatar](https://github.com/StayHungryStayFoolish/SpringIOCExercise/blob/master/GetBean.png?raw=true)

1. AbstractBeanFactory # getBean 获取 Bean  实例

2. **doGetBean** 根据 Bean Name 转换为规范名称，处理循环依赖（第3步），获取 Bean 实例（该 Bean 实例由两种情况：普通的 Bean、FactoryBean，第4步），并根据 Scope 创建 Bean 实例（第5步，单例创建 createBean 流程）
   
3. DefaultSingletonBeanRegistry #** getSingleton** 处理循环依赖，通过三个 Map 工程缓存集合处理

4. AbstractBeanFactory # **getObjectBeanInstance** 如果是普通 Bean 直接返回，FactoryBean 则根据情况合并父类属性并最终通过 **getObjectFromFactoryBean** 获取（该方法最终调用 FactoryBean 的 getObject 获取 FactoryBean）,并进行后置处理（BeanPostProcessor）。
         
5. AbstractAutowireCapableBeanFactory # **createBean** 进行实例化前置处理（InstantiationAwareBeanPostProcessor），最终调用 doCreateBean 创建实例、注入属性、初始化
            
6. **doCreateBean** 实例化对象（第7步 createBeanInstance）、注入属性（第8步 populateBean）、初始化（第10步 initializeBean）
               
7. **createBeanInstance** 根据创建方式（工程方法、有参无参构造）创建实例（默认使用无参构造）
                 
8. **populateBean** 根据名称或类型将属性封装到 MutablePropertyValues，通过 applyPropertyValues 将属性注入
                     
9. **initializeBean**  使用 BeanPostProcessor 进行初始化前后置处理，并由 invokeInitMethods 初始化

### 代码解析

1.  AbstractBeanFactory # **getBean** 

    ```java
    	@Override
    	public Object getBean(String name) throws BeansException {
    		return doGetBean(name, null, null, false);
    	}
    ```

2.  **doGetBean** 处理循环依赖（**getSingleton**）、获取 Bean 实例（**getObjectBeanInstance**）、创建 Bean (**createBean**)

    ```java
    @SuppressWarnings("unchecked")
    	protected <T> T doGetBean(final String name, @Nullable final Class<T> requiredType,
    			@Nullable final Object[] args, boolean typeCheckOnly) throws BeansException {
    
    		// 根据是否有别名进行转换为规范名称
    		final String beanName = transformedBeanName(name);
    		Object bean;
    
    		// 调用父类 DefaultSingletonFactoryRegistry 的 getSingleton() 方法，从缓存中读取
    		// 该方法通过三级缓存解决了循环依赖问题。
    		// 注意：sharedInstance 并不一定是自定义获取的 Bean，有可能是 FactoryBean
    		Object sharedInstance = getSingleton(beanName);
    		if (sharedInstance != null && args == null) {
    			if (logger.isDebugEnabled()) {
    				// 判断当前 beanName 是否正在创建
    				// 若条件为 true，表示这个 Bean 虽然在缓存里，但是还并没有完全被初始化（循环引用）
    				// 使用 ConcurrentHashMap 保证线程安全
    				if (isSingletonCurrentlyInCreation(beanName)) {
    					logger.debug("Returning eagerly cached instance of singleton bean '" + beanName 				+"' that is not fully initialized yet - a consequence of a circular reference");
    				}
    				else {
    					logger.debug("Returning cached instance of singleton bean '" + beanName + "'");
    				}
    			}
    			// 如果取出来的 Bean 实例是 FactoryBean，则需要使用 FactoryBean 生成一个 Bean 实例。
    			bean = getObjectForBeanInstance(sharedInstance, name, beanName, null);
    		}
    
    		else {
    			// Fail if we're already creating this bean instance:
    			// We're assumably within a circular reference.
    			// 原型对象不允许循环创建，如果正在创建，排除异常
    			if (isPrototypeCurrentlyInCreation(beanName)) {
    				throw new BeanCurrentlyInCreationException(beanName);
    			}
    
    			// Check if bean definition exists in this factory.
    			// 这一步也是必须要做的，若存在父容器，得看看父容器是否实例化过它了。避免被重复实例化（若父容器被实例化，就以父容器的为准）
    			// 这就是为何，我们扫描 controller，哪怕不加排除什么的，也不会出问题的原因，因为 Spring 中的单例 Bean 只会被实例化一次（即使父子容器都扫描了）
    			BeanFactory parentBeanFactory = getParentBeanFactory();
    			if (parentBeanFactory != null && !containsBeanDefinition(beanName)) {
    				// Not found -> check parent.
    				String nameToLookup = originalBeanName(name);
    				if (parentBeanFactory instanceof AbstractBeanFactory) {
    					return ((AbstractBeanFactory) parentBeanFactory).doGetBean(
    							nameToLookup, requiredType, args, typeCheckOnly);
    				}
    				else if (args != null) {
    					// Delegation to parent with explicit args.
    					return (T) parentBeanFactory.getBean(nameToLookup, args);
    				}
    				else {
    					// No args -> delegate to standard getBean method.
    					return parentBeanFactory.getBean(nameToLookup, requiredType);
    				}
    			}
    
    			if (!typeCheckOnly) {
    				markBeanAsCreated(beanName);
    			}
    
    			try {
    				final RootBeanDefinition mbd = getMergedLocalBeanDefinition(beanName);
    				checkMergedBeanDefinition(mbd, beanName, args);
    
    				// Guarantee initialization of beans that the current bean depends on.
    				// 因为我们会有属性注入等等，所以这里就是要保证它依赖的那些属性先初始化才行
    				// 这部分是处理循环依赖的核心
    				String[] dependsOn = mbd.getDependsOn();
    				if (dependsOn != null) {
    					for (String dep : dependsOn) {
    						if (isDependent(beanName, dep)) {
    							throw new BeanCreationException(mbd.getResourceDescription(), beanName,
    				"Circular depends-on relationship between '" + beanName + "' and '" + dep + "'");
    						}
    						registerDependentBean(dep, beanName);
    						try {
    							getBean(dep);
    						}
    						catch (NoSuchBeanDefinitionException ex) {
    							throw new BeanCreationException(mbd.getResourceDescription(), beanName,
    									"'" + beanName + "' depends on missing bean '" + dep + "'", ex);
    						}
    					}
    				}
    
    				// Create bean instance.
    				// 创建 Bean 单例实例（只有单例会存入缓存）
    				if (mbd.isSingleton()) {
              // 此处的 getSingleton 将完成的 Bean 实例存入了一级缓存
    					sharedInstance = getSingleton(beanName, () -> {
    						try {
    							// AbstractAutowireCapableBeanFactory 实现
    							// 重要：创建 Bean 实例、属性填充、初始化 --------------------------------------
    							// AbstractAutowireCapableBeanFactory.createBean()
    							return createBean(beanName, mbd, args);
    						}
    						catch (BeansException ex) {
    							// Explicitly remove instance from singleton cache: It might have been put there
    							// eagerly by the creation process, to allow for circular reference resolution.
    							// Also remove any beans that received a temporary reference to the bean.
    							destroySingleton(beanName);
    							throw ex;
    						}
    					});
    					bean = getObjectForBeanInstance(sharedInstance, name, beanName, mbd);
    				}
    				// 创建原型实例
    				else if (mbd.isPrototype()) {
    					// It's a prototype -> create a new instance.
    					Object prototypeInstance = null;
    					try {
    						beforePrototypeCreation(beanName);
    						// AbstractAutowireCapableBeanFactory 实现
    						prototypeInstance = createBean(beanName, mbd, args);
    					}
    					finally {
    						afterPrototypeCreation(beanName);
    					}
    					bean = getObjectForBeanInstance(prototypeInstance, name, beanName, mbd);
    				}
    
    				// 创建 session、request 实例
    				else {
    					String scopeName = mbd.getScope();
    					final Scope scope = this.scopes.get(scopeName);
    					if (scope == null) {
    						throw new IllegalStateException("No Scope registered for scope name '" + scopeName + "'");
    					}
    					try {
    						Object scopedInstance = scope.get(beanName, () -> {
    							beforePrototypeCreation(beanName);
    							try {
    								// AbstractAutowireCapableBeanFactory 实现
    								return createBean(beanName, mbd, args);
    							}
    							finally {
    								afterPrototypeCreation(beanName);
    							}
    						});
    						bean = getObjectForBeanInstance(scopedInstance, name, beanName, mbd);
    					}
    					catch (IllegalStateException ex) {
    						throw new BeanCreationException(beanName,
    						"Scope '" + scopeName + "' is not active for the current thread; consider " +"defining a scoped proxy for this bean if you intend to refer to it from a singleton",
    								ex);
    					}
    				}
    			}
    			catch (BeansException ex) {
    				cleanupAfterBeanCreationFailure(beanName);
    				throw ex;
    			}
    		}
    
    		// Check if required type matches the type of the actual bean instance.
    		// requiredType 如果是 Integer，但是获取的是 String，通过 TypeConverter 转换
    		if (requiredType != null && !requiredType.isInstance(bean)) {
    			try {
    				T convertedBean = getTypeConverter().convertIfNecessary(bean, requiredType);
    				if (convertedBean == null) {
    					throw new BeanNotOfRequiredTypeException(name, requiredType, bean.getClass());
    				}
    				return convertedBean;
    			}
    			catch (TypeMismatchException ex) {
    				if (logger.isDebugEnabled()) {
    					logger.debug("Failed to convert bean '" + name + "' to required type '" +
    							ClassUtils.getQualifiedName(requiredType) + "'", ex);
    				}
    				throw new BeanNotOfRequiredTypeException(name, requiredType, bean.getClass());
    			}
    		}
    		return (T) bean;
    	}
    ```

3.  DefaultSingletonBeanRegistry # **getSingleton** 通过三个集合缓存处理循环依赖

    **Spring IoC 三级缓存**

    ```java
        	/**
        	 * 一级缓存：单例对象的 Cache
        	 * value: 完全初始化的完整对象
        	 *
        	 * 一级缓存在 AbstractBeanFactory # doGetBean 的 Bean 初始化完成时存入，并删除二、三级缓存
        	 */
        	private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(256);
        
        	/**
        	 * 三级缓存：单例对象工厂（ ObjectFactory <?> ）的 Cache
        	 * value: 将只完成实例化未初始化的实例提前暴露（不只放入缓存，
        	 * 还需要使用 InstantiationAwareBeanPostProcessor 进行后置处理，处理过程中可能会改变 Bean 实例一些属性）
        	 * 该缓存在循环依赖中起决定作用：
        	 *
        	 * 例如：A - B - C (C - A) 依赖循环，A 创建 B，B 又创建 C ，C 又依赖 A 的时候，此时 A 会有判断是否正在创建中，
        	 * 此时 A 属于创建中状态，C 则在三级缓存中提前暴露，从而完成 A 实例的初始化。
        	 *
        	 * 三级缓存在  AbstractAutowireCapableBeanFactory # doCrateBean 创建实例时存入
        	 */
        	private final Map<String, ObjectFactory<?>> singletonFactories = new HashMap<>(16);
        
        	/**
        	 * 二级缓存：提前曝光的需要被引用的单例对象（通过三级缓存 ObjectFactory<?> 获取的 Bean 实例）的 Cache
        	 * value: 只实例化未初始化的实例（只存储 beanName 和 bean 实例映射关系）
        	 *
        	 * 只有循环依赖时，才会使用二级缓存（因为在循环依赖时需要提前曝光依赖的实例，所以该缓存名字也以 early 命名）
             * getSingleton（209 行使用）
        	 */
        	private final Map<String, Object> earlySingletonObjects = new HashMap<>(16);
    
    
    // -----------------------  以上为 IoC 容器的三个缓存 ----------------------------------
    // 从三级缓存可以看出，获取的 Bean 可能是普通的 Bean 或者 ObjectFactory，如果是后者则需要 getObject // 获取对象
    ```

    

    ```java
      @Nullable
    	protected Object getSingleton(String beanName, boolean allowEarlyReference) {
    		Object singletonObject = this.singletonObjects.get(beanName);
    		// 当一级缓存没有，并且 Bean 在创建中则进入该方法
    		// isSingletonCurrentlyInCreation() 判断当前单例bean是否正在创建中（即没有完成初始化的 Bean）
    		// 比如 A 的构造器依赖了 B 对象所以得先去创建 B 对象，或者在 A 的 populateBean 过程中依赖了 B 对象，得先去创建 B 对象，这时的 A 就是处于创建中的状态。
    		if (singletonObject == null && isSingletonCurrentlyInCreation(beanName)) {
    			// 同步一级缓存
    			synchronized (this.singletonObjects) {
    				// 从二级缓存中获取单例 Bean
    				singletonObject = this.earlySingletonObjects.get(beanName);
    				// allowEarlyReference 是否允许从 singletonFactories 中通过 getObject 拿到对象
    				if (singletonObject == null && allowEarlyReference) {
    					// 从三级缓存中获取 ObjectFactory 工厂对象
    					ObjectFactory<?> singletonFactory = this.singletonFactories.get(beanName);
    					// 如果工程对象存在，从三级缓存中移除，移到二级缓存中
    					if (singletonFactory != null) {
    						// 从工厂获取单例 Bean
    						singletonObject = singletonFactory.getObject();
    						// 存入二级缓存
    						this.earlySingletonObjects.put(beanName, singletonObject);
    						// 三级缓存移除
    						this.singletonFactories.remove(beanName);
    					}
    				}
    			}
    		}
    		return singletonObject;
    	}
    ```

4.  AbstractBeanFactory # **getObjectFromBeanInstance** 获取 Bean 实例，如果是用户自定义的**普通 Bean** 或者**解引用的（以 & 开头）Bean** 直接返回，如果是 **FactoryBean** 类型调用  getObjectFromFactoryBean 通过 **Java 自省** 获取

    ```java
    protected Object getObjectForBeanInstance(
    			Object beanInstance, String name, String beanName, @Nullable RootBeanDefinition mbd) {
    
    		// Don't let calling code try to dereference the factory if the bean isn't a factory.
    		// 判断 name 是否是 FactoryBean 类型（以 & 开头）
    		if (BeanFactoryUtils.isFactoryDereference(name)) {
    			// 如果是空类型的 Bean 直接返回
    			if (beanInstance instanceof NullBean) {
    				return beanInstance;
    			}
    			// 如果不是 FactoryBean 类型，直接抛出异常
    			if (!(beanInstance instanceof FactoryBean)) {
    				throw new BeanIsNotAFactoryException(transformedBeanName(name), beanInstance.getClass());
    			}
    		}
    
    		// Now we have the bean instance, which may be a normal bean or a FactoryBean.
    		// If it's a FactoryBean, we use it to create a bean instance, unless the
    		// caller actually wants a reference to the factory.
    		// 如果 Bean 实例是一个普通的 Bean 或者是 FactoryBean 类型（以 & 开头）直接返回实例
    		if (!(beanInstance instanceof FactoryBean) || BeanFactoryUtils.isFactoryDereference(name)) {
    			return beanInstance;
    		}
    
    		// 否则直接从 FactoryBean 中生成一个 Bean 实例
    		Object object = null;
    		if (mbd == null) {
    			object = getCachedObjectForFactoryBean(beanName);
    		}
    		if (object == null) {
    			// Return bean instance from factory.
    			FactoryBean<?> factory = (FactoryBean<?>) beanInstance;
    			// Caches object obtained from FactoryBean if it is a singleton.
    			if (mbd == null && containsBeanDefinition(beanName)) {
    				// 将存储 XML 配置文件的 GenericBeanDefinition 转换为 RootBeanDefinition，
    				// 如果指定 BeanName 是 子Bean ，同时合并父类的相关属性
    				mbd = getMergedLocalBeanDefinition(beanName);
    			}
    			boolean synthetic = (mbd != null && mbd.isSynthetic());
    			// 从 FactoryBean 中通过调用 getObject 返回一个 Bean 实例
    			object = getObjectFromFactoryBean(factory, beanName, !synthetic);
    		}
    		return object;
    	}
    ```

    FactoryBeanRegistrySupport # **getObjectFromFactoryBean**

    ```java
    protected Object getObjectFromFactoryBean(FactoryBean<?> factory, String beanName, boolean shouldPostProcess) {
    		// 判断是否是单例、是否包含该 beanName 的实例
    		if (factory.isSingleton() && containsSingleton(beanName)) {
    			synchronized (getSingletonMutex()) {
    				// 从缓存中获取
    				Object object = this.factoryBeanObjectCache.get(beanName);
    				if (object == null) {
    					// 如果没有则最终调用 FactoryBean.getObject()
    					object = doGetObjectFromFactoryBean(factory, beanName);
    					// Only post-process and store if not put there already during getObject() call above
    					// (e.g. because of circular reference processing triggered by custom getBean calls)
    					Object alreadyThere = this.factoryBeanObjectCache.get(beanName);
    					if (alreadyThere != null) {
    						object = alreadyThere;
    					}
    					else { // 后置处理流程
    						if (shouldPostProcess) {
    							// 判断该 Bean 是否正在创建中
    							if (isSingletonCurrentlyInCreation(beanName)) {
    								// Temporarily return non-post-processed object, not storing it yet..
    								// /这里返回的是临时的非后置处理的  Bean 实例，并不保存它
    								return object;
    							}
    							beforeSingletonCreation(beanName);
    							try {
    								// 对 Bean 实例进行后置处理，如果在 FactoryBean 找不到该  Bean 实例抛如下异常
    								object = postProcessObjectFromFactoryBean(object, beanName);
    							}
    							catch (Throwable ex) {
    								throw new BeanCreationException(beanName,
    										"Post-processing of FactoryBean's singleton object failed", ex);
    							}
    							finally {
    								// 当所有的条件具备完成，最后调用该方法对 单例 Bean 创建之后的一些处理，如：不能重复创建
    								afterSingletonCreation(beanName);
    							}
    						}
    						if (containsSingleton(beanName)) {
    							this.factoryBeanObjectCache.put(beanName, object);
    						}
    					}
    				}
    				return object;
    			}
    		}
    		else { // 如果 FactoryBean 不是单例，则缓存中肯定找不到，进入该流程
    			Object object = doGetObjectFromFactoryBean(factory, beanName);
    			if (shouldPostProcess) {
    				try {
    					// 从 FactoryBean 拿到 Bean 实例后对其后置处理逻辑，如暴露该 Bean 实例
    					object = postProcessObjectFromFactoryBean(object, beanName);
    				}
    				catch (Throwable ex) {
    					throw new BeanCreationException(beanName, "Post-processing of FactoryBean's object failed", ex);
    				}
    			}
    			return object;
    		}
    	}
    ```

    **doGetObjectFromFactoryBean** 验证系统权限后，最终通过 **Java自省 ** 获取 Bean 实例

    ```java
    private Object doGetObjectFromFactoryBean(final FactoryBean<?> factory, final String beanName)
    			throws BeanCreationException {
    
    		Object object;
    		try {
    			// 系统权限验证
    			if (System.getSecurityManager() != null) {
    				AccessControlContext acc = getAccessControlContext();
    				try {
    					object = AccessController.doPrivileged((PrivilegedExceptionAction<Object>) factory::getObject, acc);
    				}
    				catch (PrivilegedActionException pae) {
    					throw pae.getException();
    				}
    			}
    			else {
    				// 获取 Bean 实例
    				object = factory.getObject();
    			}
    		}
    		catch (FactoryBeanNotInitializedException ex) {
    			throw new BeanCurrentlyInCreationException(beanName, ex.toString());
    		}
    		catch (Throwable ex) {
    			throw new BeanCreationException(beanName, "FactoryBean threw exception on object creation", ex);
    		}
    
    		// Do not accept a null value for a FactoryBean that's not fully
    		// initialized yet: Many FactoryBeans just return null then.
    		// 如果 FactoryBean 没有拿到，判断是否正在创建
    		if (object == null) {
    			// 1. 如果是抛出异常
    			if (isSingletonCurrentlyInCreation(beanName)) {
    				throw new BeanCurrentlyInCreationException(
    						beanName, "FactoryBean which is currently in creation returned null from getObject");
    			}
    			// 2. 如果不是，返回一个空 Bean 对象
    			object = new NullBean();
    		}
    		return object;
    	}
    ```

5.  AbstractAutowireCapableBeanFactory # **createBean** 如果是单例，调用 getSingleton 重载方法（**此处将完成的 Bean 实例存入了一级缓存**），内部参数调用 createBean 最终由 **doCreateBean 完成实例创建、属性填充、初始化**，最后再次回到 **第4步 getObjectForBeanInstance**

    ```java
    	@Override
    	protected Object createBean(String beanName, RootBeanDefinition mbd, @Nullable Object[] args)
    			throws BeanCreationException {
    
    		if (logger.isDebugEnabled()) {
    			logger.debug("Creating instance of bean '" + beanName + "'");
    		}
    		RootBeanDefinition mbdToUse = mbd;
    
    		// Make sure bean class is actually resolved at this point, and
    		// clone the bean definition in case of a dynamically resolved Class
    		// which cannot be stored in the shared merged bean definition.
    		// 判断是否需要实例化
    		Class<?> resolvedClass = resolveBeanClass(mbd, beanName);
    		if (resolvedClass != null && !mbd.hasBeanClass() && mbd.getBeanClassName() != null) {
    			mbdToUse = new RootBeanDefinition(mbd);
    			mbdToUse.setBeanClass(resolvedClass);
    		}
    
    		// Prepare method overrides.
    		try {
    			// 这里主要是解析 <lookup-method name="***" bean="***"/> 类似这种方式的依赖注入（Spring 支持 lookup-method，replace-method 两个依赖注入的方式）
    			// 它相当于调用指定类里面的指定方法进行注入，所以需要考虑到方法重载的情况，因此这个方法解析的就是这种情况
    			// 不推荐使用
    			mbdToUse.prepareMethodOverrides();
    		}
    		catch (BeanDefinitionValidationException ex) {
    			throw new BeanDefinitionStoreException(mbdToUse.getResourceDescription(),
    					beanName, "Validation of method overrides failed", ex);
    		}
    
    		try {
    			// Give BeanPostProcessors a chance to return a proxy instead of the target bean instance.
    			// 给 BeanPostProcessors 一个机会来返回一个代理对象代替目标对象，动态代理之类的，都在这里实现的
    			// 1、具体逻辑是判断当前 Spring 容器是否注册了实现了 InstantiationAwareBeanPostProcessor 接口的后置处理器如果有，
    			// 则依次调用其中的 applyBeanPostProcessorsBeforeInstantiation()，如果中间任意一个方法返回不为 null，直接结束调用。
    			// 2、然后依次所有注册的 BeanPostProcessor 的 postProcessAfterInitialization()（同样如果任意一次返回不为 null，即终止调用。
    			// 容器里所有的 InstantiationAwareBeanPostProcessors 实例，都会在此处生效，进行前置处理
    			Object bean = resolveBeforeInstantiation(beanName, mbdToUse);
    			if (bean != null) {
    				return bean;
    			}
    		}
    		catch (Throwable ex) {
    			throw new BeanCreationException(mbdToUse.getResourceDescription(), beanName,
    					"BeanPostProcessor before instantiation of bean failed", ex);
    		}
    
    		try {
    			// 重要：创建 Bean 实例、属性填充、初始化 -----------------------------------------------------------------------
    			Object beanInstance = doCreateBean(beanName, mbdToUse, args);
    			if (logger.isDebugEnabled()) {
    				logger.debug("Finished creating instance of bean '" + beanName + "'");
    			}
    			return beanInstance;
    		}
    		catch (BeanCreationException | ImplicitlyAppearedSingletonException ex) {
    			// A previously detected exception with proper bean creation context already,
    			// or illegal singleton state to be communicated up to DefaultSingletonBeanRegistry.
    			throw ex;
    		}
    		catch (Throwable ex) {
    			throw new BeanCreationException(
    					mbdToUse.getResourceDescription(), beanName, "Unexpected exception during bean creation", ex);
    		}
    	}
    ```

6.  **doCreateBean** 该方法一共三大步骤：实例化对象（第7步 createBeanInstance）、注入属性（第8步 populateBean）、初始化（第10步 initializeBean）并存入三级缓存

    ```java
    protected Object doCreateBean(final String beanName, final RootBeanDefinition mbd, final @Nullable Object[] args)
    			throws BeanCreationException {
    
    		// Instantiate the bean.
    		BeanWrapper instanceWrapper = null;
    		if (mbd.isSingleton()) {
    			// 先从未实例化缓存中移除
    			instanceWrapper = this.factoryBeanInstanceCache.remove(beanName);
    		}
    		// 1. 初始化：实例化 Bean 对象
    		if (instanceWrapper == null) {
    			// 创建实例（创建的是空对象），默认调用无参构造实例化 Bean，构造参数注入发生在这一步
    			instanceWrapper = createBeanInstance(beanName, mbd, args);
    		}
    		final Object bean = instanceWrapper.getWrappedInstance();
    		Class<?> beanType = instanceWrapper.getWrappedClass();
    		if (beanType != NullBean.class) {
    			mbd.resolvedTargetType = beanType;
    		}
    
    		// Allow post-processors to modify the merged bean definition.
    		synchronized (mbd.postProcessingLock) {
    			if (!mbd.postProcessed) {
    				try {
    					applyMergedBeanDefinitionPostProcessors(mbd, beanType, beanName);
    				}
    				catch (Throwable ex) {
    					throw new BeanCreationException(mbd.getResourceDescription(), beanName,
    							"Post-processing of merged bean definition failed", ex);
    				}
    				mbd.postProcessed = true;
    			}
    		}
    
    		// Eagerly cache singletons to be able to resolve circular references
    		// even when triggered by lifecycle interfaces like BeanFactoryAware.
    		// 解决循环依赖关键过程
    		boolean earlySingletonExposure = (mbd.isSingleton() && this.allowCircularReferences &&
    				isSingletonCurrentlyInCreation(beanName));
    		// 如果需要提前保留单例 Bean，则放入三级缓存中
    		if (earlySingletonExposure) {
    			if (logger.isDebugEnabled()) {
    				logger.debug("Eagerly caching bean '" + beanName +
    						"' to allow for resolving potential circular references");
    			}
    			// 存入三级缓存
    			addSingletonFactory(beanName, () -> getEarlyBeanReference(beanName, mbd, bean));
    		}
    
    		// Initialize the bean instance.
    		Object exposedObject = bean;
    		try {
    			// 2. 初始化：注入属性（DI）
    			// 设置 Bean 属性
    			// 使用反射与自省进行属性设置
    			populateBean(beanName, mbd, instanceWrapper);
    			// 3.初始化：调用初始化方法，完成实例初始化（AOP 步骤）
    			exposedObject = initializeBean(beanName, exposedObject, mbd);
    		}
    		catch (Throwable ex) {
    			if (ex instanceof BeanCreationException && beanName.equals(((BeanCreationException) ex).getBeanName())) {
    				throw (BeanCreationException) ex;
    			}
    			else {
    				throw new BeanCreationException(
    						mbd.getResourceDescription(), beanName, "Initialization of bean failed", ex);
    			}
    		}
    
    		if (earlySingletonExposure) {
    			Object earlySingletonReference = getSingleton(beanName, false);
    			if (earlySingletonReference != null) {
    				if (exposedObject == bean) {
    					exposedObject = earlySingletonReference;
    				}
    				else if (!this.allowRawInjectionDespiteWrapping && hasDependentBean(beanName)) {
    					String[] dependentBeans = getDependentBeans(beanName);
    					Set<String> actualDependentBeans = new LinkedHashSet<>(dependentBeans.length);
    					for (String dependentBean : dependentBeans) {
    						if (!removeSingletonIfCreatedForTypeCheckOnly(dependentBean)) {
    							actualDependentBeans.add(dependentBean);
    						}
    					}
    					if (!actualDependentBeans.isEmpty()) {
    						throw new BeanCurrentlyInCreationException(beanName,
    								"Bean with name '" + beanName + "' has been injected into other beans [" +
    								StringUtils.collectionToCommaDelimitedString(actualDependentBeans) +
    								"] in its raw version as part of a circular reference, but has eventually been " +
    								"wrapped. This means that said other beans do not use the final version of the " +
    								"bean. This is often the result of over-eager type matching - consider using " +
    								"'getBeanNamesOfType' with the 'allowEagerInit' flag turned off, for example.");
    					}
    				}
    			}
    		}
    
    		// Register bean as disposable.
    		try {
    			registerDisposableBeanIfNecessary(beanName, bean, mbd);
    		}
    		catch (BeanDefinitionValidationException ex) {
    			throw new BeanCreationException(
    					mbd.getResourceDescription(), beanName, "Invalid destruction signature", ex);
    		}
    
    		return exposedObject;
    	}
    ```

7.  **createBeanInstance** 根据创建方式（工程方法、有参无参构造）创建实例（默认使用无参构造）

    ```java
    protected BeanWrapper createBeanInstance(String beanName, RootBeanDefinition mbd, @Nullable Object[] args) {
    		// Make sure bean class is actually resolved at this point.
    		Class<?> beanClass = resolveBeanClass(mbd, beanName);
    
    		if (beanClass != null && !Modifier.isPublic(beanClass.getModifiers()) && !mbd.isNonPublicAccessAllowed()) {
    			throw new BeanCreationException(mbd.getResourceDescription(), beanName,
    					"Bean class isn't public, and non-public access not allowed: " + beanClass.getName());
    		}
    
    		Supplier<?> instanceSupplier = mbd.getInstanceSupplier();
    		if (instanceSupplier != null) {
    			return obtainFromSupplier(instanceSupplier, beanName);
    		}
    
    		// 通过工程方法创建 Bean 实例（factory-method 标签）
    		if (mbd.getFactoryMethodName() != null)  {
    			return instantiateUsingFactoryMethod(beanName, mbd, args);
    		}
    
    		// Shortcut when re-creating the same bean...
    		boolean resolved = false;
    		boolean autowireNecessary = false;
    		if (args == null) {
    			synchronized (mbd.constructorArgumentLock) {
    				if (mbd.resolvedConstructorOrFactoryMethod != null) {
    					resolved = true;
    					autowireNecessary = mbd.constructorArgumentsResolved;
    				}
    			}
    		}
    		if (resolved) {
    			if (autowireNecessary) {
    				return autowireConstructor(beanName, mbd, null, null);
    			}
    			else {
    				return instantiateBean(beanName, mbd);
    			}
    		}
    
    		// Need to determine the constructor...
    		Constructor<?>[] ctors = determineConstructorsFromBeanPostProcessors(beanClass, beanName);
    		if (ctors != null ||
    				mbd.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_CONSTRUCTOR ||
    				mbd.hasConstructorArgumentValues() || !ObjectUtils.isEmpty(args))  {
    			return autowireConstructor(beanName, mbd, ctors, args);
    		}
    
    		// No special handling: simply use no-arg constructor.
    		// 无特殊处理，使用无参构造创建 Bean 实例
    		return instantiateBean(beanName, mbd);
    	}
    ```

8.  **populateBean** 根据名称或类型将属性封装到 MutablePropertyValues，通过 **applyPropertyValues**将属性注入

    ```java
    protected void populateBean(String beanName, RootBeanDefinition mbd, @Nullable BeanWrapper bw) {
    		// 没有实例化对象
    		if (bw == null) {
    			// 包含属性，抛出异常
    			if (mbd.hasPropertyValues()) {
    				throw new BeanCreationException(
    						mbd.getResourceDescription(), beanName, "Cannot apply property values to null instance");
    			}
    			else {
    				// Skip property population phase for null instance.
    				return;
    			}
    		}
    
    		// Give any InstantiationAwareBeanPostProcessors the opportunity to modify the
    		// state of the bean before properties are set. This can be used, for example,
    		// to support styles of field injection.
    		boolean continueWithPropertyPopulation = true;
    
    		if (!mbd.isSynthetic() && hasInstantiationAwareBeanPostProcessors()) {
    			for (BeanPostProcessor bp : getBeanPostProcessors()) {
    				if (bp instanceof InstantiationAwareBeanPostProcessor) {
    					InstantiationAwareBeanPostProcessor ibp = (InstantiationAwareBeanPostProcessor) bp;
    					if (!ibp.postProcessAfterInstantiation(bw.getWrappedInstance(), beanName)) {
    						continueWithPropertyPopulation = false;
    						break;
    					}
    				}
    			}
    		}
    
    		if (!continueWithPropertyPopulation) {
    			return;
    		}
    
    		PropertyValues pvs = (mbd.hasPropertyValues() ? mbd.getPropertyValues() : null);
    
    		if (mbd.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_BY_NAME ||
    				mbd.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_BY_TYPE) {
    			MutablePropertyValues newPvs = new MutablePropertyValues(pvs);
    
    			// Add property values based on autowire by name if applicable.
    			// 根据名字装配存储到 MutablePropertyValues
    			if (mbd.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_BY_NAME) {
    				autowireByName(beanName, mbd, bw, newPvs);
    			}
    
    			// Add property values based on autowire by type if applicable.
    			// 根据类型装配存储到 MutablePropertyValues
    			if (mbd.getResolvedAutowireMode() == RootBeanDefinition.AUTOWIRE_BY_TYPE) {
    				autowireByType(beanName, mbd, bw, newPvs);
    			}
    
    			pvs = newPvs;
    		}
    
    		boolean hasInstAwareBpps = hasInstantiationAwareBeanPostProcessors();
    		boolean needsDepCheck = (mbd.getDependencyCheck() != RootBeanDefinition.DEPENDENCY_CHECK_NONE);
    
    		if (hasInstAwareBpps || needsDepCheck) {
    			if (pvs == null) {
    				pvs = mbd.getPropertyValues();
    			}
    			// 提取属性
    			PropertyDescriptor[] filteredPds = filterPropertyDescriptorsForDependencyCheck(bw, mbd.allowCaching);
    			if (hasInstAwareBpps) {
    				for (BeanPostProcessor bp : getBeanPostProcessors()) {
    					if (bp instanceof InstantiationAwareBeanPostProcessor) {
    						InstantiationAwareBeanPostProcessor ibp = (InstantiationAwareBeanPostProcessor) bp;
    						// 对需要依赖检查的属性进行后处理，具体属性注入使用类似 AutowiredAnnotationBeanPostProcessor 类完成 
    						pvs = ibp.postProcessPropertyValues(pvs, filteredPds, bw.getWrappedInstance(), beanName);
    						if (pvs == null) {
    							return;
    						}
    					}
    				}
    			}
    			if (needsDepCheck) {
    				// 依赖检查，主要获取 Bean 属性及检测
    				checkDependencies(beanName, mbd, filteredPds, pvs);
    			}
    		}
    
    		if (pvs != null) {
    			// 将属性注入到 Bean
    			applyPropertyValues(beanName, mbd, bw, pvs);
    		}
    	}
    ```

    **applyPropertyValues** 属性注入（**DI**）

    ```java
    protected void applyPropertyValues(String beanName, BeanDefinition mbd, BeanWrapper bw, PropertyValues pvs) {
    		if (pvs.isEmpty()) {
    			return;
    		}
    
    		if (System.getSecurityManager() != null && bw instanceof BeanWrapperImpl) {
    			((BeanWrapperImpl) bw).setSecurityContext(getAccessControlContext());
    		}
    
    		MutablePropertyValues mpvs = null;
    		List<PropertyValue> original;
    
    		if (pvs instanceof MutablePropertyValues) {
    			mpvs = (MutablePropertyValues) pvs;
    			if (mpvs.isConverted()) {
    				// Shortcut: use the pre-converted values as-is.
    				try {
    					// 属性注入，最终使用 AbstractNestablePropertyAccessor 子类完成该功能
    					bw.setPropertyValues(mpvs);
    					return;
    				}
    				catch (BeansException ex) {
    					throw new BeanCreationException(
    							mbd.getResourceDescription(), beanName, "Error setting property values", ex);
    				}
    			}
    			original = mpvs.getPropertyValueList();
    		}
    		else {
    			// 如果不是 MutablePropertyValues 类型，使用 PropertyValue
    			original = Arrays.asList(pvs.getPropertyValues());
    		}
    
    		TypeConverter converter = getCustomTypeConverter();
    		if (converter == null) {
    			converter = bw;
    		}
    		// 获取属性解析器
    		BeanDefinitionValueResolver valueResolver = new BeanDefinitionValueResolver(this, beanName, mbd, converter);
    
    		// Create a deep copy, resolving any references for values.
    		List<PropertyValue> deepCopy = new ArrayList<>(original.size());
    		boolean resolveNecessary = false;
    		for (PropertyValue pv : original) {
    			if (pv.isConverted()) {
    				deepCopy.add(pv);
    			}
    			else {
    				String propertyName = pv.getName();
    				Object originalValue = pv.getValue();
    				Object resolvedValue = valueResolver.resolveValueIfNecessary(pv, originalValue);
    				Object convertedValue = resolvedValue;
    				boolean convertible = bw.isWritableProperty(propertyName) &&
    						!PropertyAccessorUtils.isNestedOrIndexedProperty(propertyName);
    				if (convertible) {
    					convertedValue = convertForProperty(resolvedValue, propertyName, bw, converter);
    				}
    				// Possibly store converted value in merged bean definition,
    				// in order to avoid re-conversion for every created bean instance.
    				if (resolvedValue == originalValue) {
    					if (convertible) {
    						pv.setConvertedValue(convertedValue);
    					}
    					deepCopy.add(pv);
    				}
    				else if (convertible && originalValue instanceof TypedStringValue &&
    						!((TypedStringValue) originalValue).isDynamic() &&
    						!(convertedValue instanceof Collection || ObjectUtils.isArray(convertedValue))) {
    					pv.setConvertedValue(convertedValue);
    					deepCopy.add(pv);
    				}
    				else {
    					resolveNecessary = true;
    					deepCopy.add(new PropertyValue(pv, convertedValue));
    				}
    			}
    		}
    		if (mpvs != null && !resolveNecessary) {
    			mpvs.setConverted();
    		}
    
    		// Set our (possibly massaged) deep copy.
    		try {
    			bw.setPropertyValues(new MutablePropertyValues(deepCopy));
    		}
    		catch (BeansException ex) {
    			throw new BeanCreationException(
    					mbd.getResourceDescription(), beanName, "Error setting property values", ex);
    		}
    	}
    ```

9.  **initializeBean** 使用 BeanPostProcessor 进行初始化前后置处理，并由 invokeInitMethods **初始化**

    ```java
    protected Object initializeBean(final String beanName, final Object bean, @Nullable RootBeanDefinition mbd) {
    		if (System.getSecurityManager() != null) {
    			AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
    				invokeAwareMethods(beanName, bean);
    				return null;
    			}, getAccessControlContext());
    		}
    		else {
    			invokeAwareMethods(beanName, bean);
    		}
    
    		Object wrappedBean = bean;
    		if (mbd == null || !mbd.isSynthetic()) {
    			// 在初始化 bean 之前，调用 BeanPostProcessors 的 postProcessBeforeInitialization 方法
    			wrappedBean = applyBeanPostProcessorsBeforeInitialization(wrappedBean, beanName);
    		}
    
    		try {
    			// 执行初始化方法（ 先调用 InitializingBean 的 afterPropertiesSet，再调用 init-method 属性指定的初始化方法）
    			invokeInitMethods(beanName, wrappedBean, mbd);
    		}
    		catch (Throwable ex) {
    			throw new BeanCreationException(
    					(mbd != null ? mbd.getResourceDescription() : null),
    					beanName, "Invocation of init method failed", ex);
    		}
    		if (mbd == null || !mbd.isSynthetic()) {
    			// 应用 BeanPostProcessor 的 postProcessAfterInitialization 方法(AOP 代理对象生成)
    			wrappedBean = applyBeanPostProcessorsAfterInitialization(wrappedBean, beanName);
    		}
    
    		return wrappedBean;
    	}
    ```

    

