# CustomMVC 框架基本思路

1. 以 **HttpServlet** 为入口，所有请求都会经过 **doGet 或 doPost**

   ```java
      @Override
       protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
       }
   
       @Override
       protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
       }
   ```

2. CustomMVC 需要设置一个中间层，提供一个抽象模板方法 **doDispatch** 用来实现自定义框架逻辑处理

   ```java
   public abstract void doDispatch(HttpServletRequest req, HttpServletResponse resp);
   ```

3. 以注解形式，需要从 **HttpServletReuqest** 获取请求的 **uri**，然后根据请求的 uri 找到对应的 Class 和 Method，因此需要建立一个处理器映射器 **HandlerMapping** 用来存储 uri 和 **HandlerMethod（存储 真正的 Controller 类和要执行的 Method）** 的关系

   ```java
   继承 BeanFactoryAware 取得 BeanFactory ，从 IoC 容器获取 BeanDefinition 将带有 			         @Controller 和 @RequestMapping 的类和方法与注解上的 uri 建立映射关系
   最终可以取出 uri 对应的 Method 调用 method.invoke()
           
   public class AnnotationHandlerMapping implements HandlerMapping, BeanFactoryAware {
     
       private BeanFactory beanFactory;
   
     	
       private Map<String, HandlerMethod> annotationHandlerMethodMap = new HashMap<>();
     
     	private void init(){        
         	1. 获取 IoC 容器所有 BeanName
         	2. 遍历获取 @Controller 和 @RequestMapping 的类和方法
         	3. 取出 uri 对应的 HanlerMethod，最终调用 method.invoke()
     	}
    
       // 获取 Handler 
     	@Override
       public Object getHandler(HttpServletRequest request) {
           String uri = request.getRequestURI();
           return annotationHandlerMethodMap.get(uri);
       }
   }
   ```

4. 使用适配器模式 **HandlerRequestMapping** 执行处理器映射起 **HandlerMapping** 的请求

   ```java
   public class AnnotationRequestMappingAdapter implements HandlerAdapter {
     
     	@Override
       public void handleRequest(Object handler, HttpServletRequest request, HttpServletResponse response) {
           HandlerMethod handlerMethod = (HandlerMethod) handler;
           Method method = handlerMethod.getMethod();
           Object controller = handlerMethod.getHandler();
           Object[] args = getParameters(request, method);
           try {
               // 使用反射调用并处理返回结构（只处理 String 和 Map 不处理 POJO 和 ModelAndView ）
               Object returnValue = method.invoke(controller, args);
               handleReturnValue(returnValue, response, method);
           } catch (IllegalAccessException | InvocationTargetException e) {
               e.printStackTrace();
           }
       }
   }
   ```

5. 使用 **DispatchServlet** 处理请求，执行 **GenericServlet#init** 方法，获取**HandlerAdapter（处理器适配器）和 HandlerMapping（处理器映射器）**，并通过 **doGet 调用 doDispatch**

   ```java
   public class DispatcherServlet extends AbstractHttpServlet {
     
       private List<HandlerMapping> handlerMappings = new ArrayList<>();
       private List<HandlerAdapter> handlerAdapters = new ArrayList<>();
     
     	    @Override
       public void init(ServletConfig config) throws ServletException {
           String contextConfig = config.getInitParameter("contextConfigLocation");
           // 创建 IoC 容器
           BeanFactory beanFactory = new DefaultListableBeanFactory(contextConfig);
           // 一次性创建所有 Bean
           beanFactory.getBeansByType(Object.class);
           handlerMappings = beanFactory.getBeansByType(HandlerMapping.class);
           handlerAdapters = beanFactory.getBeansByType(HandlerAdapter.class);
       }
     
         @Override
       public void doDispatch(HttpServletRequest req, HttpServletResponse resp) {
           // 获取处理器
           Object handler = getHandler(req);
           if (null == handler) {
               return;
           }
           // 获取处理器适配器
           HandlerAdapter adapter = getAdapter(handler);
           if (null == adapter) {
               return;
           }
   
           adapter.handleRequest(handler, req, resp);
       }
   }
   ```

   
