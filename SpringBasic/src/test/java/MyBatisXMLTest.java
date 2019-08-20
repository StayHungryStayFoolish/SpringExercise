import io.stayhungrystayfoolish.aop.domain.User;
import io.stayhungrystayfoolish.aop.service.UserService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

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
}
