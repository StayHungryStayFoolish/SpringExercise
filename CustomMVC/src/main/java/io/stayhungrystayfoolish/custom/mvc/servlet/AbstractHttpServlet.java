package io.stayhungrystayfoolish.custom.mvc.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Author: bonismo@hotmail.com
 * @Description:
 * @Date: 2019-10-18 01:37
 * @Version: V1.0
 */
public abstract class AbstractHttpServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doDispatch(req, resp);
    }

    public abstract void doDispatch(HttpServletRequest req, HttpServletResponse resp);
}
