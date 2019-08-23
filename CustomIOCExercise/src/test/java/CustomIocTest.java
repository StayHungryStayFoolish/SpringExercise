import io.stayhungrystayfoolish.custom.ioc.exercise.domain.Student;
import io.stayhungrystayfoolish.custom.ioc.factory.BeanFactory;
import io.stayhungrystayfoolish.custom.ioc.factory.DefaultListableBeanFactory;
import org.junit.Test;

/**
 * @Author: Created by bonismo@hotmail.com on 2019-08-23 15:44
 * @Description:
 * @Version: 1.0
 */
public class CustomIocTest {

    @Test
    public void test() throws Exception {
        // 指定xml资源路径
        String location = "classpath:beans.xml";
//         Resource resource = new ClassPathResource(location);
        // 创建工厂
        BeanFactory beanFactory = new DefaultListableBeanFactory(location);
        // 从工厂中获取指定对象
        Student student = (Student) beanFactory.getBean("student");
        // 测试对象是否可用
        System.out.println(student);
    }
}
