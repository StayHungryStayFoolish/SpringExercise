# Spring IOC 基础流通分析及手写(AOP 简单应用)

## AOP 三种实现方式

- 纯 XML 方式配置 AOP
    
    - 针对具体核心业务逻辑，定义一个通知类
        
        - 根据具体核心业务场景，针对具体类或具体方法，选择相应的通知方式
        
            > before 目标方法调用前，执行此 advice
                                            
            > around 目标方法调用前、调用后处理，执行此 advice（当抛出异常，立即退出，会转向 after advice，执行完转到 throwing advice）
            
            > after 目标方法正常结束、异常都执行此 advice
                                                                                                                                                                                                                          
            > after returning 目标方法调用正常结束，不管有无返回结果，执行此 advice
                                                                                                                                                                                                                          
            > after throwing 目标方法执行异常退出，执行此 advice
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        
    - xml 配置                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        
        
        > 参考 SpringBasic 模块
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  
        >> XMLAdvice 和 spring-aop.xml，测试通知在 UserServiceImpl 需要打开或关闭人为异常。


- XML 和注解混合

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

- 纯注解
