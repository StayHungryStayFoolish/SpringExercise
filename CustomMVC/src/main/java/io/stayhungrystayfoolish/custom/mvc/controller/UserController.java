package io.stayhungrystayfoolish.custom.mvc.controller;

import io.stayhungrystayfoolish.custom.mvc.annotation.Controller;
import io.stayhungrystayfoolish.custom.mvc.annotation.RequestMapping;
import io.stayhungrystayfoolish.custom.mvc.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: Created by bonismo@hotmail.com on 2019-10-22 17:42
 * @Description:
 * @Version: 1.0
 */
@Controller
public class UserController {

    @RequestMapping(value = "/query")
    @ResponseBody
    public Map<String, Object> query(Integer id, String username) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("username", username);
        return map;
    }

    @RequestMapping(value = "/delete")
    @ResponseBody
    public String delete(Integer id) {
        return "OK";
    }

    @RequestMapping(value = "/add")
    @ResponseBody
    public String add() {
        return "OK";
    }
}
