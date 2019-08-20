show databases ;
drop database if exists `exercise`;
create database `exercise`;
use `exercise`;
drop table if exists `user`;
create table `user`(
  id bigint primary key auto_increment,
  name varchar(100) not null ,
  age int not null
);
select *
from user;
