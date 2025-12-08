package com.edu.smartshop.service;

import com.edu.smartshop.dto.request.LoginDTO;
import com.edu.smartshop.entity.User;
import jakarta.servlet.http.HttpSession;

public interface IAuthService {
    User login(LoginDTO loginDTO, HttpSession session);
    void logout(HttpSession session);
    User getCurrentUser(HttpSession session);
    boolean isLoggedIn(HttpSession session);
}
