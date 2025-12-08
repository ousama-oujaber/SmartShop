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

        // Public endpoints
        if (path.startsWith("/auth/") || path.startsWith("/error")) {
            return true;
        }

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("LOGGED_USER") == null) {
            throw new UnauthorizedException("You must be logged in to access this resource");
        }

        User user = (User) session.getAttribute("LOGGED_USER");

        // ADMIN can do everything
        if (user.getRole() == UserRole.ADMIN) {
            return true;
        }

        // CLIENT restrictions
        if (user.getRole() == UserRole.CLIENT) {
            // Clients can only do GET requests (Read-only)
            if (!method.equals("GET")) {
                throw new UnauthorizedException("Clients are not allowed to perform this action");
            }
            
            // Clients cannot see other clients' data (simplified check)
            // In a real app, we would check if the requested ID matches the logged client
            // But for now, we enforce the "READ ONLY" rule strictly
            return true;
        }

        return true;
    }
}
