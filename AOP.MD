# Spring AOP 及事务源码解析

## AOP 相关类图

[AOP 相关类图](https://github.com/StayHungryStayFoolish/SpringExercise/tree/master/AOP-ClassImg)

## AOP 相关节点解析后对应的 BeanClass

1.  **aop:point-cut** 标签对应的是 **AspectJExpressionPointcut**

    @see org.springframework.aop.aspectj.AspectJExpressionPointcut

2.  **aop:before、aop:after、aop:around、aop:after-returning、aop:throwing** 5个通知标签对应是 **AbstractAspectJAdvice** 子类

    @see org.springframework.aop.aspectj.AbstractAspectJAdvice

3.  **aop:advisor** 标签对应的是 **DefaultBeanFactoryPointcutAdvisor**

    @see org.springframework.aop.support.DefaultBeanFactoryPointcutAdvisor

4.  **aop:aspect** 标签对应的是 **AspectJPointcutAdvisor**

    @see org.springframework.aop.aspectj.AspectJPointcutAdvisor

------

## AOP BeanDefinition 解析并注册到 IOC 容器过程

1.   **DefaultBeanDefinitionDocumentReader** 的 **parseBeanDefinitions** 使用自定义方法解析 AOP 标签

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
    						// bean、import、alias 等标签使用默认解析 ----
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

2.  **BeanDefinitionParserDelegate** 的 **parseCustomElement** 解析自定义标签，获取 **AopNamespaceHandler**，并在 **resolve** 方法中进行了 **init**初始化，最后调用 **parse** 进行具体解析（此处调用的 parse 方法是 AopNamespaceHandler 的父类 NamespaceHandlerSupport ）。

    ```java
    @Nullable
    public BeanDefinition parseCustomElement(Element ele) {
    	return parseCustomElement(ele, null);
    }
    
    @Nullable
    public BeanDefinition parseCustomElement(Element ele, @Nullable BeanDefinition containingBd) {
    	// 获取命名空间 URI，如：beans 标签的 xmlns:aop、xmlns:context 属性的值）
    	String namespaceUri = getNamespaceURI(ele);
    	if (namespaceUri == null) {
    		return null;
    	}
    	// 根据不同命名获取不同的 handler
    	// 具体如何获取在 DefaultNamespaceHandlerResolver.resolve() 的 getHandlerMappings()
    	/**
    	 * @see DefaultNamespaceHandlerResolver#resolve(String)
    	 * @see DefaultNamespaceHandlerResolver#getHandlerMappings()
    	*/
    	NamespaceHandler handler = this.readerContext.getNamespaceHandlerResolver().resolve(namespaceUri);
    	if (handler == null) {
    		error("Unable to locate Spring NamespaceHandler for XML schema namespace [" + namespaceUri + "]", ele);
    		return null;
    	}
    		// 匹配到 handler 解析并返回 BeanDefinition
    	return handler.parse(ele, new ParserContext(this.readerContext, this, containingBd));
    }
    
    
    ```

3.  **DefaultNamespaceHandlerResolver** 的 **resolve** 进行 **init** 初始化，此处调用 **AopNamespaceHandler** 的初始化方法，初始化 **ConfigBeanDefinitionParser、AspectJAutoProxyBeanDefinitionParser、ScopedProxyBeanDefinitionDecorator、SpringConfiguredBeanDefinitionParser**

    ```java
    @Override
    @Nullable
    public NamespaceHandler resolve(String namespaceUri) {
    	// 获取 NamespaceHandler 集合，此处处理了集合加载
    	// 读取 spring 所有工程的 META-INF/spring.handlers 文件
    	Map<String, Object> handlerMappings = getHandlerMappings();
    	Object handlerOrClassName = handlerMappings.get(namespaceUri);
    	if (handlerOrClassName == null) {
    		return null;
    	}
    	else if (handlerOrClassName instanceof NamespaceHandler) {
    		return (NamespaceHandler) handlerOrClassName;
    	}
    	else {
    		String className = (String) handlerOrClassName;
    		try {
    			Class<?> handlerClass = ClassUtils.forName(className, this.classLoader);
    			if (!NamespaceHandler.class.isAssignableFrom(handlerClass)) {
    				throw new FatalBeanException("Class [" + className + "] for namespace [" + namespaceUri +
    						"] does not implement the [" + NamespaceHandler.class.getName() + "] interface");
    			}
    			NamespaceHandler namespaceHandler = (NamespaceHandler) BeanUtils.instantiateClass(handlerClass);
    			// 按照不同的 handler 初始化
    			namespaceHandler.init();
    			handlerMappings.put(namespaceUri, namespaceHandler);
    			return namespaceHandler;
    		}
    		catch (ClassNotFoundException ex) {
    			throw new FatalBeanException("Could not find NamespaceHandler class [" + className +
    					"] for namespace [" + namespaceUri + "]", ex);
    		}
    		catch (LinkageError err) {
    			throw new FatalBeanException("Unresolvable class definition for NamespaceHandler class [" +
    					className + "] for namespace [" + namespaceUri + "]", err);
    		}
    	}
    }
    ```

    ```java
    public class AopNamespaceHandler extends NamespaceHandlerSupport {
    
    	/**
    	 * Register the {@link BeanDefinitionParser BeanDefinitionParsers} for the
    	 * '{@code config}', '{@code spring-configured}', '{@code aspectj-autoproxy}'
    	 * and '{@code scoped-proxy}' tags.
    	 */
    	@Override
    	public void init() {
    		// In 2.0 XSD as well as in 2.1 XSD.
    		registerBeanDefinitionParser("config", new ConfigBeanDefinitionParser());
    		registerBeanDefinitionParser("aspectj-autoproxy", new AspectJAutoProxyBeanDefinitionParser());
    		registerBeanDefinitionDecorator("scoped-proxy", new ScopedProxyBeanDefinitionDecorator());
    
    		// Only in 2.0 XSD: moved to context namespace as of 2.1
    		registerBeanDefinitionParser("spring-configured", new SpringConfiguredBeanDefinitionParser());
    	}
    
    }
    ```

4.  **NamespaceHandlerSupport** (AopNamespaceHandler 的父类) 的 **parse** 方法，此处 **findParserForElement** 获取了第3步初始化的 ConfigBeanDefinitionParser，从而调用该 BeanDefinition 的 parse 方法。

    ```java
    @Override
    @Nullable
    public BeanDefinition parse(Element element, ParserContext parserContext) {
    	// NamespaceHandler 子类 init() 初始化了大量的 BeanDefinitionParser 来分别处理不同的自定义标签
    	// 从指定的 NamespaceHandler 中，匹配到指定的 BeanDefinitionParser
    	// 如果是 <aop:config> 标签则获取 ConfigBeanDefinitionParser，
    	/**
    	 * @see org.springframework.aop.config.ConfigBeanDefinitionParser#parse(Element, ParserContext)
    	 */
    	BeanDefinitionParser parser = findParserForElement(element, parserContext);
    	return (parser != null ? parser.parse(element, parserContext) : null);
    }
    ```

5.  **ConfigBeanDefinitionParser** 的 **parse** 方法调用了 configureAutoProxyCreator 注册 AOP 的 AspectJAwareAdvisorAutoProxyCreator，并分别解析了三种标签（aop:pointcut、aop:advisor、aop:aspect） **parsePointcut、parseAdvisor、parseAspect** ，以解析 aspect 方法解析以下流程

    ```java
    @Override
    @Nullable
    public BeanDefinition parse(Element element, ParserContext parserContext) {
    	CompositeComponentDefinition compositeDef =
    			new CompositeComponentDefinition(element.getTagName(), parserContext.extractSource(element));
    	parserContext.pushContainingComponent(compositeDef);
    	/**
    	 * 在 IOC 容器注册 AspectJAwareAdvisorAutoProxyCreator 作为 BeanDefinition，用来创建 AOP 代理对象
    	 * AspectJAwareAdvisorAutoProxyCreator 实现了 BeanPostProcessor 接口，所以会在目标对象实例化后，创建目标对象的代理对象。
    	 *
    	 * @see org.springframework.aop.aspectj.autoproxy.AspectJAwareAdvisorAutoProxyCreator
    	 * 具体用处请参考
    	 * @see org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator#postProcessBeforeInstantiation
    	 * @see org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator#postProcessAfterInitialization
    	 */
    	configureAutoProxyCreator(parserContext, element);
    	List<Element> childElts = DomUtils.getChildElements(element);
    	for (Element elt: childElts) {
    		String localName = parserContext.getDelegate().getLocalName(elt);
    		if (POINTCUT.equals(localName)) {
    			// 解析 <aop:pointcut>
    			/**
    			 * 生成 AspectJExpressionPointcut 的 BeanDefinition 对象并注册
    			 * @see #parsePointcut
    			 * @see #createPointcutDefinition
    			 */
    			parsePointcut(elt, parserContext);
    		}
    		else if (ADVISOR.equals(localName)) {
    			// 解析 <aop:advisor>
    			/**
    			 * Spring AOP 实现方式
    			 * 生成 DefaultBeanFactoryPointcutAdvisor 的 BeanDefinition 对象并注册
    			 * @see #createAdvisorBeanDefinition
    			 */
    			parseAdvisor(elt, parserContext);
    		}
    		else if (ASPECT.equals(localName)) {
    			// 解析 <aop:aspect>
    			/**
    			 * AspectJ 实现方式
    			 * @see #getAdviceClass
    			 * 生成 AspectJMethodBeforeAdvice、AspectJAfterAdvice、AspectJAfterReturningAdvice、AspectJAfterThrowingAdvice、AspectJAroundAdvice 五个标签通知类
    			 * 解析 advice 生成 AspectJPointcutAdvisor 类的 BeanDefinition 对象，该类最终会形成执行链对目标对象进行增强
    			 * 共生成 6 个对象
    			 */
    			parseAspect(elt, parserContext);
    		}
    	}
    	parserContext.popAndRegisterContainingComponent();
    	return null;
    }
    ```

6.  **parseAspect** 解析 **AspectJ** 的 AOP 实现方式，**parseAdvice** 解析5种通知标签，并生成 AspectJMethodBeforeAdvice、AspectJAfterAdvice、AspectJAfterReturningAdvice、AspectJAfterThrowingAdvice、AspectJAroundAdvice 五个标签通知类，解析 advice 生成 AspectJPointcutAdvisor 类的 BeanDefinition 对象，该类最终会形成执行链对目标对象进行增强。

    ```java
    private void parseAspect(Element aspectElement, ParserContext parserContext) {
    	// 获取 <aop:aspect> 标签的 id 属性值
    	String aspectId = aspectElement.getAttribute(ID);
    	// 获取 <aop:aspect> 标签的 ref 属性值
    	String aspectName = aspectElement.getAttribute(REF);
    	try {
    		this.parseState.push(new AspectEntry(aspectId, aspectName));
    		List<BeanDefinition> beanDefinitions = new ArrayList<>();
    		List<BeanReference> beanReferences = new ArrayList<>();
    		// 处理 <aop:aspect> 标签的 <aop:declare-parents> 子标签
    		List<Element> declareParents = DomUtils.getChildElementsByTagName(aspectElement, DECLARE_PARENTS);
    		for (int i = METHOD_INDEX; i < declareParents.size(); i++) {
    			Element declareParentsElement = declareParents.get(i);
    			beanDefinitions.add(parseDeclareParents(declareParentsElement, parserContext));
    		}
    		// We have to parse "advice" and all the advice kinds in one loop, to get the
    		// ordering semantics right.
    		// 获取 <aop:aspect> 标签的所有子标签
    		NodeList nodeList = aspectElement.getChildNodes();
    		boolean adviceFoundAlready = false;
    		for (int i = 0; i < nodeList.getLength(); i++) {
    			Node node = nodeList.item(i);
    			// 判断是否是 <aop:before>、<aop:after>、<aop:after-returning>、<aop:after-throwing method="">、<aop:around method=""> 五个标签
    			if (isAdviceNode(node, parserContext)) {
    				if (!adviceFoundAlready) {
    					adviceFoundAlready = true;
    					if (!StringUtils.hasText(aspectName)) {
    						parserContext.getReaderContext().error(
    								"<aspect> tag needs aspect bean reference via 'ref' attribute when declaring advices.",
    								aspectElement, this.parseState.snapshot());
    						return;
    					}
    					beanReferences.add(new RuntimeBeanReference(aspectName));
    				}
    				// 解析 <aop:before>、<aop:around>、<aop:after>、<aop:after-returning>、<aop:after-throwing> 五个子标签
    				// 解析 advice 生成 AspectJPointcutAdvisor 类的 BeanDefinition 对象，该类最终会形成执行链对目标对象进行增强
    				AbstractBeanDefinition advisorDefinition = parseAdvice(
    						aspectName, i, aspectElement, (Element) node, parserContext, beanDefinitions, beanReferences);
    				beanDefinitions.add(advisorDefinition);
    			}
    		}
    		AspectComponentDefinition aspectComponentDefinition = createAspectComponentDefinition(
    				aspectElement, aspectId, beanDefinitions, beanReferences, parserContext);
    		parserContext.pushContainingComponent(aspectComponentDefinition);
    		List<Element> pointcuts = DomUtils.getChildElementsByTagName(aspectElement, POINTCUT);
    		for (Element pointcutElement : pointcuts) {
    			parsePointcut(pointcutElement, parserContext);
    		}
    		parserContext.popAndRegisterContainingComponent();
    	}
    	finally {
    		this.parseState.pop();
    	}
    }
    ```

7.   **parseAdvice** 解析5个通知标签，创建了 **方法g工厂 MethodLocationFactoryBean、封装了切面信息的切面工厂 SimpleBeanFactoryAwareAspectInstanceFactory** 并调用 **createAdviceDefinition** 解析 **aop:pointcut** 生成5个通知类和将切点通知器 **AspectJPointcutAdvisor** 注册到 IOC 容器

    ```java
    /**
     * Parses one of '{@code before}', '{@code after}', '{@code after-returning}',
     * '{@code after-throwing}' or '{@code around}' and registers the resulting
     * BeanDefinition with the supplied BeanDefinitionRegistry.
     * @param aspectName	    待绑定的切面，<aop:aspect ref="xmlAdvice"> 标签内 ref 的值，也就是对应的 <bean id="xmlAdvice" class="io.stayhungrystayfoolish.aop.advice.XMLAdvice"/> Bean 的 id
     * @param aspectElement		<aop:aspect> 节点，<aop:config> 的子标签 <aop:aspect>
     * @param adviceElement		<aop:advice> 节点，当前节点
     * @param beanDefinitions	与 aspect 相关的所有 bean 对象集合
     * @param beanReferences	与 aspect 相关的所有 bean 引用对象集合
     * @return the generated advice RootBeanDefinition
     */
    private AbstractBeanDefinition parseAdvice(
    		String aspectName, int order, Element aspectElement, Element adviceElement, ParserContext parserContext,
    		List<BeanDefinition> beanDefinitions, List<BeanReference> beanReferences) {
    	try {
    		this.parseState.push(new AdviceEntry(parserContext.getDelegate().getLocalName(adviceElement)));
    		// create the method factory bean
    		// 创建方法工厂 MethodLocatingFactoryBean 的 BeanDefinition 对象：用于获取 Advice 增强类的 Method 对象
    		RootBeanDefinition methodDefinition = new RootBeanDefinition(MethodLocatingFactoryBean.class);
    		// 设置 MethodLocatingFactoryBean 的 targetBeanName 为 advice 类的引用名称(此处相当于 SpringBasic 的 XMLAdvice)
    		methodDefinition.getPropertyValues().add("targetBeanName", aspectName);
    		// 获取 <aop:before method="****"> 方法名
    		methodDefinition.getPropertyValues().add("methodName", adviceElement.getAttribute("method"));
    		methodDefinition.setSynthetic(true);
    		// create instance factory definition
    		// 创建封装了切面信息的切面工厂类
    		RootBeanDefinition aspectFactoryDef =
    				new RootBeanDefinition(SimpleBeanFactoryAwareAspectInstanceFactory.class);
    		aspectFactoryDef.getPropertyValues().add("aspectBeanName", aspectName);
    		aspectFactoryDef.setSynthetic(true);
    		// register the pointcut
    		// 涉及 point-cut 属性的解析，并调用 getAdviceClass 生成5个通知类，并结合上述的两个bean 最终包装为通知对象
    		/**
    		 * <aop:point-cut> 标签对应的是 AspectJExpressionPointcut
    		 * @see org.springframework.aop.aspectj.AspectJExpressionPointcut
    		 *
    		 * <aop:before>、<aop:after>、<aop:around>、<aop:after-returning>、<aop:throwing> 标签对应的是 AbstractAspectJAdvice 子类
    		 * @see org.springframework.aop.aspectj.AbstractAspectJAdvice
    		 *
    		 * <aop:advisor> 标签对应的是 DefaultBeanFactoryPointcutAdvisor
    		 * @see org.springframework.aop.support.DefaultBeanFactoryPointcutAdvisor
    		 *
    		 * <aop:aspect>  AspectJPointcutAdvisor
    		 * @see org.springframework.aop.aspectj.AspectJPointcutAdvisor
    		 */
    		AbstractBeanDefinition adviceDef = createAdviceDefinition(
    				adviceElement, parserContext, aspectName, order, methodDefinition, aspectFactoryDef,
    				beanDefinitions, beanReferences);
    		// configure the advisor
    		// 封装 AspectJPointcutAdvisor 切点通知器类对象，该类最终会形成执行链对目标对象进行增强
    		RootBeanDefinition advisorDefinition = new RootBeanDefinition(AspectJPointcutAdvisor.class);
    		// 通知器设置相关属性
    		advisorDefinition.setSource(parserContext.extractSource(adviceElement));
    		advisorDefinition.getConstructorArgumentValues().addGenericArgumentValue(adviceDef);
    		if (aspectElement.hasAttribute(ORDER_PROPERTY)) {
    			advisorDefinition.getPropertyValues().add(
    					ORDER_PROPERTY, aspectElement.getAttribute(ORDER_PROPERTY));
    		}
    		// register the final advisor
    		// 将切点通知器 AspectJPointcutAdvisor 注册到 IOC 容器
    		parserContext.getReaderContext().registerWithGeneratedName(advisorDefinition);
    		return advisorDefinition;
    	}
    	finally {
    		this.parseState.pop();
    	}
    }
    ```

8.  **createAdviceDefinition** 解析 **aop:pointcut** 并调用 **getAdvice** 生成5个通知类和最终封装对象

    ```java
    private AbstractBeanDefinition createAdviceDefinition(
    		Element adviceElement, ParserContext parserContext, String aspectName, int order,
    		RootBeanDefinition methodDef, RootBeanDefinition aspectFactoryDef,
    		List<BeanDefinition> beanDefinitions, List<BeanReference> beanReferences) {
    	// 根据 adviceElement 节点分析出是什么类型的 Advice
    	/**
    	 * @see #getAdviceClass(Element, ParserContext)
    	 */
    	RootBeanDefinition adviceDefinition = new RootBeanDefinition(getAdviceClass(adviceElement, parserContext));
    	adviceDefinition.setSource(parserContext.extractSource(adviceElement));
    	// 设置切面信息
    	adviceDefinition.getPropertyValues().add(ASPECT_NAME_PROPERTY, aspectName);
    	// 设置 declarationOrder
    	adviceDefinition.getPropertyValues().add(DECLARATION_ORDER_PROPERTY, order);
    	// 解析节点 <aop:5 个切面通知 > 是否含有 returning、throwing、arg-names 参数，有则设置
    	if (adviceElement.hasAttribute(RETURNING)) {
    		adviceDefinition.getPropertyValues().add(
    				RETURNING_PROPERTY, adviceElement.getAttribute(RETURNING));
    	}
    	if (adviceElement.hasAttribute(THROWING)) {
    		adviceDefinition.getPropertyValues().add(
    				THROWING_PROPERTY, adviceElement.getAttribute(THROWING));
    	}
    	if (adviceElement.hasAttribute(ARG_NAMES)) {
    		adviceDefinition.getPropertyValues().add(
    				ARG_NAMES_PROPERTY, adviceElement.getAttribute(ARG_NAMES));
    	}
    	// 获取并设置构造参数的三个参数
    	ConstructorArgumentValues cav = adviceDefinition.getConstructorArgumentValues();
    	// 1. 设置 MethodLocatingFactoryBean 参数
    	cav.addIndexedArgumentValue(METHOD_INDEX, methodDef);
    	// 解析 point-cut 属性，可能是 BeanDefinition 也可能是 ref 的 String 类型
    	Object pointcut = parsePointcutProperty(adviceElement, parserContext);
    	// 2. 设置 AspectJExpressionPointcut 参数
    	if (pointcut instanceof BeanDefinition) {
    		cav.addIndexedArgumentValue(POINTCUT_INDEX, pointcut);
    		beanDefinitions.add((BeanDefinition) pointcut);
    	}
    	else if (pointcut instanceof String) {
    		RuntimeBeanReference pointcutRef = new RuntimeBeanReference((String) pointcut);
    		cav.addIndexedArgumentValue(POINTCUT_INDEX, pointcutRef);
    		beanReferences.add(pointcutRef);
    	}
    	// 3. 设置 AspectJPointcutAdvisor 参数
    	cav.addIndexedArgumentValue(ASPECT_INSTANCE_FACTORY_INDEX, aspectFactoryDef);
    	return adviceDefinition;
    }
    ```

9.  **getAdvice** 生成5个通知类

    ```java
    /**
     * Gets the advice implementation class corresponding to the supplied {@link Element}.
     * 5 个通知方式 Advice 类
     */
    private Class<?> getAdviceClass(Element adviceElement, ParserContext parserContext) {
    	String elementName = parserContext.getDelegate().getLocalName(adviceElement);
    	if (BEFORE.equals(elementName)) {
    		return AspectJMethodBeforeAdvice.class;
    	}
    	else if (AFTER.equals(elementName)) {
    		return AspectJAfterAdvice.class;
    	}
    	else if (AFTER_RETURNING_ELEMENT.equals(elementName)) {
    		return AspectJAfterReturningAdvice.class;
    	}
    	else if (AFTER_THROWING_ELEMENT.equals(elementName)) {
    		return AspectJAfterThrowingAdvice.class;
    	}
    	else if (AROUND.equals(elementName)) {
    		return AspectJAroundAdvice.class;
    	}
    	else {
    		throw new IllegalArgumentException("Unknown advice kind [" + elementName + "].");
    	}
    }
    ```

------

## Transaction 事务处理流程

-   因为事务的 BeanDefinition 在 AOP 第2步的 **NamespaceHandler handler = this.readerContext.getNamespaceHandlerResolver().resolve(namespaceUri);** 已经注册到 IoC 容器中，所以事务处理只需看处理流程即可。

-   **事务流程的入口：**
    -   加上 **@Transactional** 注解之后，Spring可以启到事务控制的功能了，再正式执行方法前它会做一些操作
    -   首先进入 **CglibAopProxy.class** 的 **intercept** 方法或者 **JdkDynamicAopProxy.class** 的 **invoke **方法（继承接口方式），根据不同实现方法选择不同的动态代理类
    -   之后进入到 **ReflectiveMethodInvocation.class** 然后一步一步往下走来到 **invoke** 方法，最终进入 **TransactionInterceptor** 的 **invoke** 方法。
    -   从 **MethodInterceptor** 子类寻找，因为事务的实现就是通过 AOP，而代理最终都会调用 **invoke** 方法。所以事务的入口类是 **TransactionInterceptor**。

------

1.   **TransactionInterceptor**  事务入口，最终调用父类 **TransactionAspectSupport** 的**invokeWithinTransaction** 实现事务支持。

    ```java
    /**
     * 事务处理入口
     */
    @Override
    @Nullable
    public Object invoke(final MethodInvocation invocation) throws Throwable {
    	// Work out the target class: may be {@code null}.
    	// The TransactionAttributeSource should be passed the target class
    	// as well as the method, which may be from an interface.
    	Class<?> targetClass = (invocation.getThis() != null ? AopUtils.getTargetClass(invocation.getThis()) : null);
    	// Adapt to TransactionAspectSupport's invokeWithinTransaction...
    	return invokeWithinTransaction(invocation.getMethod(), targetClass, invocation::proceed);
    }
    ```

2.  **TransactionAspectSupport**  的 **invokeWithinTransaction** 实现事务支持，通过 **determineTransactionManager** 获取具体事务管理平台， 并通过 **createTransactionIfNecessary** 创建事务，执行调用链 **invocation.proceedWithInvocation()** 并最终执行事务。

    ```java
    @Nullable
    protected Object invokeWithinTransaction(Method method, @Nullable Class<?> targetClass,
    		final InvocationCallback invocation) throws Throwable {
    	// If the transaction attribute is null, the method is non-transactional.
    	// 获取事务属性，如果未空，说明不支持事务
    	TransactionAttributeSource tas = getTransactionAttributeSource();
    	// 获取事务属性
    	final TransactionAttribute txAttr = (tas != null ? tas.getTransactionAttribute(method, targetClass) : null);
    	// 确定具体事务平台
    	final PlatformTransactionManager tm = determineTransactionManager(txAttr);
    	final String joinpointIdentification = methodIdentification(method, targetClass, txAttr);
    	if (txAttr == null || !(tm instanceof CallbackPreferringPlatformTransactionManager)) {
    		// Standard transaction demarcation with getTransaction and commit/rollback calls.
    		// 创建事务信息对象，通过事务平台创建事务
    		TransactionInfo txInfo = createTransactionIfNecessary(tm, txAttr, joinpointIdentification);
    		Object retVal = null;
    		try {
    			// This is an around advice: Invoke the next interceptor in the chain.
    			// This will normally result in a target object being invoked.
    			retVal = invocation.proceedWithInvocation();
    		}
    		catch (Throwable ex) {
    			// target invocation exception
    			// 执行事务 rollback 方法
    			completeTransactionAfterThrowing(txInfo, ex);
    			throw ex;
    		}
    		finally {
    			cleanupTransactionInfo(txInfo);
    		}
    		// 执行事务 commit 方法
    		commitTransactionAfterReturning(txInfo);
    		return retVal;
    	}
    	else {
    		final ThrowableHolder throwableHolder = new ThrowableHolder();
    		// It's a CallbackPreferringPlatformTransactionManager: pass a TransactionCallback in.
    		try {
    			Object result = ((CallbackPreferringPlatformTransactionManager) tm).execute(txAttr, status -> {
    				TransactionInfo txInfo = prepareTransactionInfo(tm, txAttr, joinpointIdentification, status);
    				try {
    					return invocation.proceedWithInvocation();
    				}
    				catch (Throwable ex) {
    					if (txAttr.rollbackOn(ex)) {
    						// A RuntimeException: will lead to a rollback.
    						if (ex instanceof RuntimeException) {
    							throw (RuntimeException) ex;
    						}
    						else {
    							throw new ThrowableHolderException(ex);
    						}
    					}
    					else {
    						// A normal return value: will lead to a commit.
    						throwableHolder.throwable = ex;
    						return null;
    					}
    				}
    				finally {
    					cleanupTransactionInfo(txInfo);
    				}
    			});
    			// Check result state: It might indicate a Throwable to rethrow.
    			if (throwableHolder.throwable != null) {
    				throw throwableHolder.throwable;
    			}
    			return result;
    		}
    		catch (ThrowableHolderException ex) {
    			throw ex.getCause();
    		}
    		catch (TransactionSystemException ex2) {
    			...
    		}
    		catch (Throwable ex2) {
                ...
    		}
    	}
    }
    ```

3.   **determineTransactionManager**  最终从 BeanFactory 充获取事务管理对象，MyBatis 和 JDBC 使用 DataSourceTransactionManager，JPA 使用 JpaTransactionManager

    ```java
    @Nullable
    protected PlatformTransactionManager determineTransactionManager(@Nullable TransactionAttribute txAttr) {
    	// Do not attempt to lookup tx manager if no tx attributes are set
    	if (txAttr == null || this.beanFactory == null) {
    		return getTransactionManager();
    	}
    	String qualifier = txAttr.getQualifier();
    	// 确定事务管理平台
    	if (StringUtils.hasText(qualifier)) {
    		// 无论走哪个流程，最终都是从对应的 BeanFactory 获取
    		return determineQualifiedTransactionManager(this.beanFactory, qualifier);
    	}
    	else if (StringUtils.hasText(this.transactionManagerBeanName)) {
    		return determineQualifiedTransactionManager(this.beanFactory, this.transactionManagerBeanName);
    	}
    	else {
    		PlatformTransactionManager defaultTransactionManager = getTransactionManager();
    		if (defaultTransactionManager == null) {
    			defaultTransactionManager = this.transactionManagerCache.get(DEFAULT_TRANSACTION_MANAGER_KEY);
    			if (defaultTransactionManager == null) {
    				// 从 spring 容器中获取指定的平台事务管理器
    				defaultTransactionManager = this.beanFactory.getBean(PlatformTransactionManager.class);
    				this.transactionManagerCache.putIfAbsent(
    						DEFAULT_TRANSACTION_MANAGER_KEY, defaultTransactionManager);
    			}
    		}
    		return defaultTransactionManager;
    	}
    }
    ```

4.   **createTransactionIfNecessary** 根据不同的事务管理平台创建事务

    ```java
    @SuppressWarnings("serial")
    protected TransactionInfo createTransactionIfNecessary(@Nullable PlatformTransactionManager tm,
    		@Nullable TransactionAttribute txAttr, final String joinpointIdentification) {
    	// If no name specified, apply method identification as transaction name.
    	if (txAttr != null && txAttr.getName() == null) {
    		txAttr = new DelegatingTransactionAttribute(txAttr) {
    			@Override
    			public String getName() {
    				return joinpointIdentification;
    			}
    		};
    	}
    	// 事务的状态
    	TransactionStatus status = null;
    	if (txAttr != null) {
    		if (tm != null) {
    			/**
    			 * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#getTransaction(TransactionDefinition)
    			 */
    			status = tm.getTransaction(txAttr);
    		}
    		else {
    			if (logger.isDebugEnabled()) {
    				logger.debug("Skipping transactional joinpoint [" + joinpointIdentification +
    						"] because no transaction manager has been configured");
    			}
    		}
    	}
    	return prepareTransactionInfo(tm, txAttr, joinpointIdentification, status);
    }
    ```

