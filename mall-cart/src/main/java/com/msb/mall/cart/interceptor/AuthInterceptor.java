package com.msb.mall.cart.interceptor;

import com.msb.common.constant.AuthConstant;
import com.msb.common.constant.URLConstant;
import com.msb.common.dto.MemberSessionDTO;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 我们自定义的拦截器：帮助我们获取当前登录的用户信息
 *     通过Session共享获取的
 */
public class AuthInterceptor implements HandlerInterceptor {
    // 本地线程对象  Map<thread,Object>
    public static ThreadLocal<MemberSessionDTO> threadLocal = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 通过HttpSession获取当前登录的用户信息
        HttpSession session = request.getSession();
        Object attribute = session.getAttribute(AuthConstant.AUTH_SESSION_REDIS);
        if(attribute != null){
            MemberSessionDTO memberSessionDTO = (MemberSessionDTO) attribute;
            threadLocal.set(memberSessionDTO);
            return true;
        }
        // 如果没有登录，那么就返回登录页面进行登录
        session.setAttribute(AuthConstant.AUTH_SESSION_MSG, "cart服务需要登录！");
        // response.addCookie(new Cookie(AuthConstant.AUTH_SESSION_MSG, "请先登录"));
        response.sendRedirect(URLConstant.AUTH_LOGIN_HTML);
        return false;
    }
}
