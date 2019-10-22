package io.stayhungrystayfoolish.custom.mvc.handlermapping;

import javax.servlet.http.HttpServletRequest;

/**
 * @Author: bonismo@hotmail.com
 * @Description:
 * @Date: 2019-10-17 15:50
 * @Version: V1.0
 */
public interface HandlerMapping {

    /**
     * 根据请求查找处理器
     */
    Object getHandler(HttpServletRequest request);

}
