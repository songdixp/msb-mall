package com.msb.mall.order.interceptor;

import com.msb.common.constant.AuthConstant;
import com.msb.common.constant.URLConstant;
import com.msb.common.dto.MemberSessionDTO;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class AuthInterceptor implements HandlerInterceptor {

    public static ThreadLocal<MemberSessionDTO> threadLocal = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //获取所有用户的session信息 loginUser里面获取
        HttpSession session = request.getSession();
        Object attribute = session.getAttribute(AuthConstant.AUTH_SESSION_REDIS);
        if (attribute !=null){
            MemberSessionDTO memberSessionDTO = (MemberSessionDTO) attribute;
            //添加到本地线程
            threadLocal.set(memberSessionDTO);
            return true;
        }
        //没有拿到用户信息，返回登录页面，和cart服务逻辑类似
        session.setAttribute(AuthConstant.AUTH_SESSION_MSG, "order需要登录状态");
        response.sendRedirect(URLConstant.AUTH_LOGIN_HTML);
        return false;
    }
}
