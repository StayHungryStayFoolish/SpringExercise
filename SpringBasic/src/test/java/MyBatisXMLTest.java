import io.stayhungrystayfoolish.aop.domain.User;
import io.stayhungrystayfoolish.aop.service.UserService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * @Author: bonismo@hotmail.com
 * @Description:
 * @Date: 2019-08-18 20:56
 * @Version: V1.0
 */
public class MyBatisXMLTest {

    private UserService userService;

    @Before
    public void init() {
        ApplicationContext context = new ClassPathXmlApplicationContext("aop/applicationContext.xml");
        DefaultListableBeanFactory context1 = new XmlBeanFactory(new ClassPathResource("aop/applicationContext.xml"));
        userService = (UserService) context.getBean("userServiceImpl");
    }

    @Test
    public void save() {
        User user = new User();
        user.setName("bonismo");
        user.setAge(18);
        user = userService.save(user);
        System.out.println(user);
    }

    @Test
    public void findById() {
        User user = userService.findById(1L);
        System.out.println(user);
    }

    @Test
    public void findAll() {
        List<User> userList = userService.findAll();
        System.out.println(userList);
    }

    @Test
    public void deleteById() {
        Long id = 1L;
        userService.deleteById(id);
        User user = userService.findById(id);
        System.out.println(user);
    }

    public static void main(String[] args) {

        User m = new User();
        try {
            // 从其所在位置开始停止分析的基类。stopClass 或其基类中的所有方法/属性/事件都将在分析中被忽略。
            BeanInfo beanInfo = Introspector.getBeanInfo(m.getClass(), Object.class);
            PropertyDescriptor[] p = beanInfo.getPropertyDescriptors();
            for (int i = 0; i < p.length; i++) {
                System.out.println(p[i]);
                System.out.println(p[i].getName() + "=" + p[i].getReadMethod().invoke(m, (Object[]) null));
            }

        } catch (IntrospectionException | IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
