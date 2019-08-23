package io.stayhungrystayfoolish.custom.ioc.exercise.domain;

/**
 * @Author: Created by bonismo@hotmail.com on 2019-08-23 15:28
 * @Description:
 * @Version: 1.0
 */
public class Student {

    private String name;
    private Integer age;
    private Boolean marry;

    private Course course;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Boolean getMarry() {
        return marry;
    }

    public void setMarry(Boolean marry) {
        this.marry = marry;
    }
}
