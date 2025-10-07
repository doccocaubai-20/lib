package com.example.lib.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.example.lib.model.User;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        String uri = request.getRequestURI();

        // Nếu chưa login, chặn tất cả URL trừ /login, /register
        if (user == null) {
            if (uri.startsWith("/login") || uri.startsWith("/register")) {
                return true; // cho phép
            } else {
                response.sendRedirect("/login");
                return false;
            }
        }

        // Chặn URL /admin/** nếu không phải admin
        if (uri.startsWith("/admin") && !"admin".equals(user.getRole())) {
            response.sendRedirect("/index");
            return false;
        }

        return true; // cho phép tất cả các URL khác
    }
}
