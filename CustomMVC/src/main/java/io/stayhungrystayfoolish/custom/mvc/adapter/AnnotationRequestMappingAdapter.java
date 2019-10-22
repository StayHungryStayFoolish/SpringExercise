package io.stayhungrystayfoolish.custom.mvc.adapter;

import io.stayhungrystayfoolish.custom.mvc.adapter.iface.HandlerAdapter;
import io.stayhungrystayfoolish.custom.mvc.annotation.ResponseBody;
import io.stayhungrystayfoolish.custom.mvc.handlermapping.config.HandlerMethod;
import io.stayhungrystayfoolish.custom.mvc.util.JsonUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @Author: Created by bonismo@hotmail.com on 2019-10-22 14:48
 * @Description:
 * @Version: 1.0
 */
public class AnnotationRequestMappingAdapter implements HandlerAdapter {

    @Override
    public boolean support(Object handler) {
        return handler instanceof HandlerMethod;
    }

    @Override
    public void handleRequest(Object handler, HttpServletRequest request, HttpServletResponse response) {
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();
        Object controller = handlerMethod.getHandler();
        Object[] args = getParameters(request, method);
        try {
            // 使用反射调用并处理返回结构（只处理 String 和 Map 不处理 POJO 和 ModelAndView ）
            Object returnValue = method.invoke(handler, args);
            handleReturnValue(returnValue, response, method);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取方法中的参数
     */
    private Object[] getParameters(HttpServletRequest request, Method method) {

        List<Object> args = new ArrayList<>();
        Map<String, String[]> parameterMap = request.getParameterMap();
        Parameter[] parameters = method.getParameters();
        for (Parameter parameter : parameters) {
            // 在 maven 插件中配置该参数，会将 arg0,arg1 等参数转成真正的参数名字
            // <compilerArgs>
            //    <arg>-parameters</arg>
            //</compilerArgs>

            String name = parameter.getName();
            Class<?> type = parameter.getType();
            // 从 url 中获取 String 参数(参数可能是多个) http://localhost:8080/user?id=1&name=bo
            String[] values = parameterMap.get(name);
            // 可以使用策略模式优化
            if (type == List.class) {
                args.add(values);
            } else if (type == Integer.class) {
                args.add(Integer.parseInt(values[0]));
            } else if (type == Long.class) {
                args.add(Long.parseLong(values[0]));
            } else if (type == Boolean.class) {
                args.add(Boolean.valueOf(values[0]));
            } else if (type == Double.class) {
                args.add(Double.parseDouble(values[0]));
            } else if (type == Float.class) {
                args.add(Float.parseFloat(values[0]));
            } else if (type == Short.class) {
                args.add(Short.parseShort(values[0]));
            } else if (type == Byte.class) {
                args.add(Byte.parseByte(values[0]));
            } else if (type == Character.class) {
                args.add(values[0]);
            } else if (type == String.class) {
                args.add(values[0]);
            }
        }
        return args.toArray();
    }

    private void handleReturnValue(Object returnValue, HttpServletResponse response, Method method) {
        try {
            if (method.isAnnotationPresent(ResponseBody.class)) {
                if (returnValue instanceof String) {
                    response.setContentType("text/html;charset=utf8");
                    response.getWriter().write(returnValue.toString());
                } else if (returnValue instanceof Map) {
                    response.setContentType("application/json;charset=utf8");
                    response.getWriter().write(Objects.requireNonNull(JsonUtil.object2Json(returnValue)));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
