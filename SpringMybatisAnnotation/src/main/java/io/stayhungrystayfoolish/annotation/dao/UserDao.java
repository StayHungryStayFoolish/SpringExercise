package io.stayhungrystayfoolish.annotation.dao;

import io.stayhungrystayfoolish.annotation.domain.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
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

    @Insert("INSERT INTO user (name,age) VALUES (#{name}, #{age})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void save(User user);

    User findById(Long id);

    List<User> findAll();

    void deleteById(Long id);
}
