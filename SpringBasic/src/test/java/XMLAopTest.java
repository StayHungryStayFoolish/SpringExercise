import io.stayhungrystayfoolish.aop.domain.User;
import io.stayhungrystayfoolish.aop.service.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @Author: Created by bonismo@hotmail.com on 2019-08-20 17:15
 * @Description:
 * @Version: 1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:aop/spring-aop.xml")
public class XMLAopTest {

    private UserService userService;

    @Before
    public void init() {
        ApplicationContext context = new ClassPathXmlApplicationContext("aop/spring-aop.xml");
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
}
