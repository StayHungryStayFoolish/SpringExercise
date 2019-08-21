# Spring IOC 基础流程分析及手写(AOP 简单应用)

## 项目模块说明

- SpringBasic 采用 XML 方式配置 MyBatis、AOP  
- SpringMybatisAnnotation 采用注解方式配置 MyBatis  

## AOP 三种实现方式

### 纯 XML 方式配置 AOP
    
- 针对具体核心业务逻辑，定义一个通知类
    
    - 根据具体核心业务场景，针对具体类或具体方法，选择相应的通知方式
    
        > before 目标方法调用前，执行此 advice
                                        
        > around 目标方法调用前、调用后处理，执行此 advice（当抛出异常，立即退出，会转向 after advice，执行完转到 throwing advice）
        
        > after 目标方法正常结束、异常都执行此 advice
                                                                                                                                                                                                                      
        > after returning 目标方法调用正常结束，不管有无返回结果，执行此 advice
                                                                                                                                                                                                                      
        > after throwing 目标方法执行异常退出，执行此 advice
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    
- xml 配置                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        
    
    > 参考 SpringBasic 模块
    >                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          
    >> XMLAdvice 和 spring-aop.xml，测试通知在 UserServiceImpl 需要打开或关闭人为异常。


### XML 和注解混合

- 定义一个切面类

    - 定义通知方式
        
        > @Before
                              
        > @Around
                                                                                       
        > @After
                                                                                                                                                                                                         
        > @AfterReturning(pointcut="..")
                                                                                                                                                                                                                                                                                                                                                                                                                                            
        > @AfterThrowing(pointcut="..",throwing="方法内的 throwable 参数")
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          
            其中 @AfterReturning 和 @AfterThrowing 中，既可以用 pointcut 也可以使用 value
            具体区别为：两者都定义，pointcut 优先于 value，
            如果只定义 value 则可以省略（@Before、@Around、@After 可以省略 value）。
            
- xml 配置

    > 参考 SpringBasic 模块
    >> SimpleAspect 和 spring-annotation.xml，测试通知在 UserServiceImpl 需要打开或关闭人为异常。
    >>> 注意 SimpleAspect 中 @Pointcut 注解及commonPointcut方法用法。                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             

### 纯注解

 - 使用注解开启 AOP 
 
     > @Configuration
     > 
     > @EnableAspectJAutoProxy
     > 
     > @ComponentScan(basePackages=""")

 - xml 项目模块，所以纯注解未实现。

----

### 部分配置区别

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
        
-         
    > 如果使用 aspect 标签，在 XMLAdvice 中直接定义一般方法即可。
    >
    > 如果使用 advisor 标签，在 XMLAdvice 中则需要实现类似 MethodBeforeAdvice、AfterReturningAdvice 等接口，在实现方法中分别定义通知方式。 
    >
    > 推荐使用 aop:aspect 标签       

### Pointcut 语法

- execution(* io.stayhungrystayfoolish.aop.service..*.*(..))

> execution() 表达式主体
>
> 第一个 * 符号，返回值任意类型
>
> io.stayhungrystayfoolish.aop.service 包名
>
> 第一个 .. 表示当前包及其子包
>
> 第二个 * 符号表示所有类
>
> .*(..)  第三个 * 符号表示任意方法名 (..) 表示任意参数类型、参数个数

**其他指示符如 within、this、target、args、@within、@target、@args 等等，并且支持 &&、||、! 逻辑符号**

---

## 事务 Transactional 的传播机制与隔离级别

1）Spring 的声明式事务管理在底层是建立在 AOP 的基础之上的。其本质是对方法**前后**进行拦截，然后在目标方法开始之前创建或者加入一个事务，在执行完目标方法之后根据执行情况提交或者回滚事务。

2）声明式事务最大的优点就是不需要通过编程的方式管理事务，这样就不需要在业务逻辑代码中掺杂事务管理的代码，只需在配置文件中做相关的事务规则声明（或通过等价的基于标注的方式），便可以将事务规则应用到业务逻辑中。因为事务管理本身就是一个典型的横切逻辑，正是 AOP 的用武之地。Spring 开发团队也意识到了这一点，为声明式事务提供了简单而强大的支持。

3）声明式事务管理曾经是 EJB 引以为傲的一个亮点， Spring 让 POJO 在事务管理方面也拥有了和 EJB 一样的待遇，让开发人员在 EJB 容器之外也用上了强大的声明式事务管理功能，这主要得益于 Spring 依赖注入容器和 Spring AOP 的支持。依赖注入容器为声明式事务管理提供了基础设施，使得 Bean 对于 Spring 框架而言是可管理的；而 Spring AOP 则是声明式事务管理的直接实现者。

4）建议在开发中使用声明式事务，不仅因为其简单，更主要是因为这样使得纯业务代码不被污染，极大方便后期的代码维护。和编程式事务相比，声明式事务唯一不足地方是，后者的最细粒度只能作用到方法级别，无法做到像编程式事务那样可以作用到代码块级别。但是即便有这样的需求，也存在很多变通的方法，比如，可以将需要进行事务管理的代码块独立为方法等等。

- <proxy-target-class="false/true"> 作用

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

- 事务传播机制

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

1. REQUIRED （事务传播默认行为）
    
    如果有事务，则加入事务，没有新建。
    
2. SUPPORTS

    其他 Bean 调用该事务所在方法，其他 Bean 中声明事务则用启用事务，若其他 Bean 没有，则不启用事务。
    
3. MANDATORY

    必须在已有事务中执行，否则抛出异常。
    
4. REQUIRES_NEW（外部事务和内部事务互相独立）

    无论是否存在事务，都会新建事务，原事务挂起，新事务执行完毕，继续执行原事务。
    内部的事务独立运行，在各自的作用域中，可以独立的回滚或者提交；而外部的事务将不受内部事务的回滚状态影响。 
    
5. NOT_SUPPORTED

    不开启事务，并挂起已存在事务。
    
6. NEVER

    必须在无事务中执行，否则抛出异常。如果存在一个活动事务，抛出异常。
    
7. NESTED（内部事务和外部事务有关联，会互相影响）

    如果一个事务存在，则运行在一个嵌套的事务中。如果没有按照 REQUIRED 执行。
    NESTED 基于单一的事务来管理，提供了多个保存点。
    这种多个保存点的机制允许内部事务的变更触发外部事务的回滚。
    而外部事务在混滚之后，仍能继续进行事务处理，即使部分操作已经被回滚。 
    由于这个设置基于 JDBC 的保存点，所以只能工作在 JDBC 的机制智商。
    由此可知，两者都是事务嵌套，不同之处在于，内外事务之间是否存在彼此之间的影响；
    
    NESTED 之间会受到影响，而产生部分回滚，而 REQUIRED_NEW 则是独立的。

- 常见事务不起作用原因

1. 抛出异常不属于 RunTimeException 及其子类

2. 异常在该方法被 try...catch，如果需要 try...catch，则在 catch 内使用 throw 也可以使事务重新生效。

3. 同一类中 Method_A 调用 Method_B 不会使事务生效。
   前提：类上没有 @Transactional 注解，且 Method_A 对外方法没有 @Transactional 注解，只在 Method_B 方法上加该注解。
   则 Method_A 对外方法事务不生效，因为 @Transactional 注解只能用在对外方法上。
   **若类上加 @Transactional，则 Method_A 调用 Method_B 事务生效。**
   
   **详细可参考 https://zhuanlan.zhihu.com/p/35483036** 

4. 非 Public 方法。

5. 配置错误（参考 spring-aop.xml）

