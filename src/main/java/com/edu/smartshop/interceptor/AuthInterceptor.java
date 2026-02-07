package com.edu.smartshop.interceptor;

import com.edu.smartshop.entity.User;
import com.edu.smartshop.enums.UserRole;
import com.edu.smartshop.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        String method = request.getMethod();

        if (path.startsWith("/api/auth/") || path.startsWith("/error") || path.startsWith("/v3/api-docs") || path.startsWith("/swagger-ui")) {
            return true;
        }

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("LOGGED_USER") == null) {
            throw new UnauthorizedException("You must be logged in to access this resource");
        }

        User user = (User) session.getAttribute("LOGGED_USER");

        if (user.getRole() == UserRole.ADMIN) {
            return true;
        }

        if (user.getRole() == UserRole.CLIENT) {
            
            
            if (method.equals("GET")) {
                if (path.startsWith("/api/products")) {
                    return true;
                }
                if (path.startsWith("/api/clients/me")) {
                    return true;
                }
            }
            
            throw new UnauthorizedException("Access denied: Insufficient permissions for CLIENT");
        }

        return false;
    }
}
