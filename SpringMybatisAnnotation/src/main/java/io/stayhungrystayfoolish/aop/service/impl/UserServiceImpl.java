package io.stayhungrystayfoolish.aop.service.impl;

import io.stayhungrystayfoolish.aop.dao.UserDao;
import io.stayhungrystayfoolish.aop.domain.User;
import io.stayhungrystayfoolish.aop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: bonismo@hotmail.com
 * @Description:
 * @Date: 2019-08-18 18:35
 * @Version: V1.0
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDao userDao;

    public User save(User user) {
        Long id = userDao.save(user);
        user.setId(id);
        return user;
    }

    public User findById(Long id) {
        return null;
    }

    public List<User> findAll() {
        return null;
    }

    public void deleteById(Long id) {

    }
}
