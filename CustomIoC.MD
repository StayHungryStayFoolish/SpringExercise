# CustomIoC 框架实现基本思路

1. **IoC** 的容器 **BeanFactory** 使用**简单工厂**模式根据各种需求获取 **Bean 实例**

   ```java
   public interface BeanFactory {
   
       /**
        * 根据 bean name 获取实例
        * @param beanName beanName
        * @return 对象实例
        */
       Object getBean(String beanName);
   
       /**
        * 根据 bean name 获取实例
        * @param beanName beanName
        * @param args 参数
        * @return 对象实例
        */
       Object getBean(String beanName, String... args);
   
       /**
        * 根据指定 bean 类型，获取对应的类型和子类型对应的bean实例
        * @param clazz clazz
        * @return Bean 实例集合
        */
       public <T> List<T> getBeansByType(Class<T> clazz);
   
       /**
        * 根据指定 bean 类型，获取对应的类型和子类型对应的bean名称
        * @param type type
        * @return Bean 名称结合
        */
       public List<String> getBeanNamesByType(Class<?> type);
   
       /**
        * 获取 BeanDefinition 集合
        * @return BeanDefinition 集合
        */
       public Map<String, BeanDefinition> getBeanDefinitions();
   }
   ```

2. 根据**接口隔离**原则，设计 **DefaultListableBeanFactory** 并在构造器中完成 xml 文件加载，在 **getBean** 接口创建实例、注入属性、初始化，最后存储。

   ```java
   public class DefaultListableBeanFactory extends AbstractBeanFactory {
     
     
     	/**
        * 存储根据 XML 读取信息封装的 BeanDefinition 的集合
        * Key: String beanName
        * Value: 封装的 Bean 信息
        */
       private Map<String, BeanDefinition> beanDefinitionMap = new HashMap<>();
   
       /**
        * 根据 BeanName 存储 Bean 实例的集合
        * Key: String BeanName
        * Value: Bean 实例
        */
       private Map<String, Object> singletonBeanMap = new HashMap<>();
     
       // 构造参数，通过读取 xml 文件加载 BeanDefinition
     	public DefaultListableBeanFactory(String location) {
           registerResources();
           registerTypeConverters();
   
           // 创建 XmlBeanDefinitionParser 对象，加载 BeanDefinition 信息到 BeanFactory
           XmlBeanDefinitionParser xmlBeanDefinitionParser = new XmlBeanDefinitionParser();
   
           Resource resource = getResource(location);
   
           if (null == resource) {
               throw new NullPointerException();
           }
           // 执行加载 xml IO 流
           xmlBeanDefinitionParser.loadBeanDefinitions(this, resource);
       }
     
       // 获取 Bean
       @Override
       public Object getBean(String beanName) {
           Object instance = singletonBeanMap.get(beanName);
           if (null != instance) {
               return instance;
           }
           // 如果单例不存在于工程，则根据 beanName 获取 bean 信息
           BeanDefinition beanDefinition = this.getBeanDefinitionMap().get(beanName);
           String beanClassName = beanDefinition.getBeanClassName();
           // 使用无参构造 #createBeanInstance 通过反射创建
           instance = createBeanInstance(beanClassName, null);
           // 注入属性 （DI）#setInstanceProperty 根据 BeanDefinition 封装得属性集合获取对应的数据类型最终注入到实例中
           setInstanceProperty(instance, beanDefinition);
           // 初始化 #initInstance 根据是否有 init-method 属性进行初始化
           initInstance(instance, beanDefinition);
           // 将生成的最终实例存储到 SingletonMap 中
           registerSingletonBean(beanName, instance);
           return instance;
       }
   }
   ```

3. 创建 **XmlBeanDefinitionParser** 获取 **IO** 流，使用 dom4j 进行解析 xml 文件元素，并由 **XmlBeanDefinitionDocumentParser** 最终加载 xml 文件封装 **BeanDefinition** 对象。

   ```java
   public class XmlBeanDefinitionParser {
   
       /**
        * 加载 XML 资源，最终由 XmlBeanDefinitionDocumentParser 解析封装进 BeanDefinition 中，并注册到 DefaultListableFactory 中。
        * @param beanFactory DefaultListableFactory
        * @param resource resource（获取 xml 路径的 IO ）
        */
       public void loadBeanDefinitions(DefaultListableBeanFactory beanFactory, Resource resource) {
           InputStream inputStream = resource.getInputStream();
           Document document = DocumentReader.createDocument(inputStream);
           XmlBeanDefinitionDocumentParser documentParser = new XmlBeanDefinitionDocumentParser(beanFactory);
           documentParser.loadBeanDefinitions(document.getRootElement());
       }
   }
   ```

   ```java
   public class XmlBeanDefinitionDocumentParser {
   
       private DefaultListableBeanFactory beanFactory;
   
       public XmlBeanDefinitionDocumentParser(DefaultListableBeanFactory beanFactory) {
           this.beanFactory = beanFactory;
       }
   
       /**
        * 解析 XML 标签信息
        *
        * @param rootElement <beans>
        */
       public void loadBeanDefinitions(Element rootElement) {
           List<Element> elementList = rootElement.elements();
           for (Element element : elementList) {
               String name = element.getName();
               if ("bean".equalsIgnoreCase(name)) {
                   parseDefaultElement(element);
               } else {
                   parseCustomElement(element);
               }
           }
       }
     
       private void parseDefaultElement(Element beanElement) {
   				1. 获取标签各种属性
           2. 封装 BeanDefintion 对象
           BeanDefinition beanDefinition = new BeanDefinition(beanName, clazz);
           3. 在 IoC 容器中存储入 beanDefinitionMap 中
           registerBeanDefinition(beanName, beanDefinition);  
       }
     
       // 如果标签元素为 component-scan ，则将该路径下的类都注册为 BeanDefinition 对象
       private void parseCustomElement(Element element) {
           if ("component-scan".equals(element.getName())) {
               String packageName = element.attributeValue("package");
               List<String> beanClassNames = getBeanClassNames(packageName);
               BeanDefinition beanDefinition = null;
               for (String className : beanClassNames) {
                   String beanName = className.substring(className.lastIndexOf(".") + 1);
                   beanDefinition = new BeanDefinition(beanName, className);
                   registerBeanDefinition(beanName, beanDefinition);
               }
           }
       }
   } 
   ```

