package io.stayhungrystayfoolish.custom.ioc.exercise.domain;

/**
 * @Author: Created by bonismo@hotmail.com on 2019-08-23 15:29
 * @Description:
 * @Version: 1.0
 */
public class Course {

    private String subject;

    private Integer day;

//    public void init() {
//        System.out.println(" Initialization Course .");
//    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        this.day = day;
    }
}
