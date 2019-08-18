package io.stayhungrystayfoolish.aop.dao;

import io.stayhungrystayfoolish.aop.domain.User;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author: bonismo@hotmail.com
 * @Description:
 * @Date: 2019-08-18 23:55
 * @Version: V1.0
 */
@Mapper
@Component
public interface UserDao {

    Long save(User user);

    User findById(Long id);

    List<User> findAll();

    void deleteById(Long id);
}
