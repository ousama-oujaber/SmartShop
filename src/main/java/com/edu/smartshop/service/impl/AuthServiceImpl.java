package com.edu.smartshop.service.impl;

import com.edu.smartshop.dto.request.LoginDTO;
import com.edu.smartshop.entity.User;
import com.edu.smartshop.exception.BusinessRuleException;
import com.edu.smartshop.exception.UnauthorizedException;
import com.edu.smartshop.repository.UserRepository;
import com.edu.smartshop.service.IAuthService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements IAuthService {

    private final UserRepository userRepository;
    private static final String USER_SESSION_KEY = "LOGGED_USER";

    @Override
    public User login(LoginDTO loginDTO, HttpSession session) {
        log.info("Login attempt for user: {}", loginDTO.getUsername());

        User user = userRepository.findByUsername(loginDTO.getUsername())
                .orElseThrow(() -> new BusinessRuleException("Invalid username or password"));

        if (!user.getPassword().equals(loginDTO.getPassword())) {
            throw new BusinessRuleException("Invalid username or password");
        }

        session.setAttribute(USER_SESSION_KEY, user);
        log.info("User {} logged in successfully", user.getUsername());

        return user;
    }

    @Override
    public void logout(HttpSession session) {
        User user = (User) session.getAttribute(USER_SESSION_KEY);
        if (user != null) {
            log.info("User {} logged out", user.getUsername());
        }
        session.invalidate();
    }

    @Override
    public User getCurrentUser(HttpSession session) {
        User user = (User) session.getAttribute(USER_SESSION_KEY);
        if (user == null) {
            throw new UnauthorizedException("Not logged in");
        }
        return user;
    }

    @Override
    public boolean isLoggedIn(HttpSession session) {
        return session.getAttribute(USER_SESSION_KEY) != null;
    }
}
