import io.stayhungrystayfoolish.annotation.dao.UserDao;
import io.stayhungrystayfoolish.annotation.domain.User;
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
        UserDao userDao = (UserDao) context.getBean("userDao");
        User user = new User();
        user.setName("bonismo");
        user.setAge(20);
        userDao.save(user);
        System.out.println(user);
    }
}
