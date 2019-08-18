import io.stayhungrystayfoolish.aop.domain.User;
import io.stayhungrystayfoolish.aop.service.UserService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @Author: bonismo@hotmail.com
 * @Description:
 * @Date: 2019-08-18 20:56
 * @Version: V1.0
 */
public class StartTest {

    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("aop/applicationContext.xml");
        UserService userService = (UserService) context.getBean("userServiceImpl");
        User user = new User();
        user.setName("bonismo");
        user.setAge(18);
        user = userService.save(user);
        System.out.println(user);
    }
}
