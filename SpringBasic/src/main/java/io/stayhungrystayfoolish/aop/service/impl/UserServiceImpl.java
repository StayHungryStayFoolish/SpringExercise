package io.stayhungrystayfoolish.aop.service.impl;

import io.stayhungrystayfoolish.aop.dao.UserDao;
import io.stayhungrystayfoolish.aop.domain.User;
import io.stayhungrystayfoolish.aop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Author: bonismo@hotmail.com
 * @Description:
 * @Date: 2019-08-18 18:35
 * @Version: V1.0
 */
@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDao userDao;

    public User save(User user) {
        userDao.save(user);
        // 测试 AOP 异常，打开
        // after 无论是否异常均执行 AOP
        // after-returning 异常不执行 AOP
        // after-throwing 只有异常执行 AOP
//        int i = 1 / 0;
//        deleteById(user.getId());
        user.setName("Lily");
        user.setAge(19);
        update(user);
        return user;
    }

    public User update(User user) {
        userDao.update(user);
        return user;
    }

    public User findById(Long id) {
        return userDao.findById(id);
    }

    public List<User> findAll() {
        return userDao.findAll();
    }

    public void deleteById(Long id) {
        userDao.deleteById(id);
    }
}
