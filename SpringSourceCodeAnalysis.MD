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
- BeanFactoryPostProcessor    允许 Bean 在**初始化前**修改 Bean 属性。
- BeanPostProcessor    允许 Bean 只能在**初始化后**修改 Bean 属性。
- InstantiationAwareBeanPostProcessor    允许 Bean 在**实例化后**修改 Bean 属性。

参考文章：

[Spring 钩子方法和接口使用详解](http://www.sohu.com/a/166804449_714863)

[Spring 钩子 BeanFactoryPostProcessor 和 BeanPostProcessor 源码学习](https://www.jianshu.com/p/a90a3e617ba6)

---

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

## ApplicationContext 方式 Spring IoC 容器加载过程

### 类 + 方法扼要说明

```diff
- new ClassPathXmlApplicationContext in red


1.```diff + new ClassPathXmlApplicationContext this will be highlighted in red```  new ClassPathXmlApplicationContext # <strong>构造方法</strong>

2.<span style="color:red;">AbstractApplicationContext</span> # <strong>refresh</strong> 刷新 Spring IoC 容器（删除原容器，创建新容器）
   
3.<b>obtainFreshBeanFactory</b> 创建工厂并初始化，<strong>以下为 BeanFactory 具体流程</strong>
         
4.<span style="color:red;">AbstractRefreshableApplicationContext</span> # <strong>refreshBeanFactory</strong> 创建DefaultListableBeanFactory
   
5.<span style="color:red;">AbstractXmlApplicationContext</span> # **loadBeanDefinitions** 创建 XmlBeanDefinitionReader
   
6.<b>loadBeanDefinitions</b> 定位资源，使用 XmlBeanDefinitionReader 调用 loadBeanDefinitions
   
7.<span style="color:red;">AbstractBeanDefinitionReader</span> # <b>loadBeanDefinitions</b> 遍历资源，进入子类 XmlBeanDefinitionReader 的 loadBeanDefinitions
   
8.<span style="color:red;">XmlBeanDefinitionReader</span> # <b>loadBeanDefinitions</b> 获取 IO 流，设置资源编码，调用具体方法 doLoadBeanDefinitions

9.<b>doLoadBeanDefinitions</b>  通过 doLoadDocument 解析 Xml 文件，获取 Document 对象，调用 registerBeanDefinitions 注册 BeanDefinition

10.<b>registerBeanDefinitions</b> 创建 BeanDefinitionReader，并由子类 DefaultBeanDefinitionDocumentReader 实现 registerBeanDefinitions 方法。

11.<span style="color:red;">DefaultBeanDefinitionDocumentReader</span> # <b>registerBeanDefinitions</b> 获取 <beans> 标签，调用 doRegisterBeanDefinitions 来完成注册工作

12.<b>doRegisterBeanDefinitions</b>  采用委托模式由 BeanDefinitionParserDelegate 完成 BeanDefinition 解析工作，通过 parserBeanDefinitions 完成根标签解析

13.<b>parserBeanDefinitions</b> 解析<beans> 下所有子节点

14.<b>parserDefaultElement</b> 解析 <alias>、<bean>、<imoprt> 等，重要方法为解析 <bean> 标签的 processBeanDefinition

15.<b>processBeanDefinition</b> 使用委托模式 BeanDefinitionParserDelegate 调用  parseBeanDefinitionElement 封装

16.<span style="color:red;">BeanDefinitionParserDelegate</span><b>parseBeanDefinitionElement</b> 调用重载方法 parseBeanDefinitionElement，内部再次调用 createBeanDefinition 方法解析并封装为 AbstractBeanDefinition 对象

<span style="color:red;">16.1</span> <b>createBeanDefinition</b> 最终调用 BeanDefinitionReaderUtils.createBeanDefinition() 通过反射创建 AbstractBeanDefinition 对象

<span style="color:red;">16.2</span> <span style="color:red;"> 16.1 完成后调用</span><b>parserPropertyElements</b> 遍历 <property> 将解析的 PropertyValue 存入 BeanDefiniton 



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

