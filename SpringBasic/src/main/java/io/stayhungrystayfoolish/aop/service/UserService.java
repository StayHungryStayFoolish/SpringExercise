package io.stayhungrystayfoolish.aop.service;

import io.stayhungrystayfoolish.aop.domain.User;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: bonismo@hotmail.com
 * @Description:
 * @Date: 2019-08-18 18:35
 * @Version: V1.0
 */
@Service
public interface UserService {

    User save(User user);

    User update(User user);

    User findById(Long id);

    List<User> findAll();

    void deleteById(Long id);
}
