package com.edu.smartshop.integration;

import com.edu.smartshop.dto.request.LoginDTO;
import com.edu.smartshop.entity.User;
import com.edu.smartshop.enums.UserRole;
import com.edu.smartshop.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Authentication Integration Tests")
public class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        
        // Create Admin User
        User admin = User.builder()
                .username("admin@test.com")
                .password("admin123")
                .role(UserRole.ADMIN)
                .build();
        userRepository.save(admin);

        // Create Client User
        User client = User.builder()
                .username("client@test.com")
                .password("client123")
                .role(UserRole.CLIENT)
                .build();
        userRepository.save(client);
    }

    @Test
    @DisplayName("Should allow public access to login")
    void shouldAllowPublicAccessToLogin() throws Exception {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("admin@test.com");
        loginDTO.setPassword("admin123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should deny access to protected resource without login")
    void shouldDenyAccessToProtectedResourceWithoutLogin() throws Exception {
        mockMvc.perform(get("/api/clients"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should allow Admin to access protected resource")
    void shouldAllowAdminToAccessProtectedResource() throws Exception {
        // 1. Login to get session
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("admin@test.com");
        loginDTO.setPassword("admin123");

        MockHttpSession session = (MockHttpSession) mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andReturn()
                .getRequest()
                .getSession();

        // 2. Access protected resource with session
        mockMvc.perform(get("/api/clients").session(session))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should deny Client from accessing Admin resource")
    void shouldDenyClientFromAccessingAdminResource() throws Exception {
        // 1. Login as Client
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("client@test.com");
        loginDTO.setPassword("client123");

        MockHttpSession session = (MockHttpSession) mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andReturn()
                .getRequest()
                .getSession();

        // 2. Try to access Admin-only resource
        mockMvc.perform(get("/api/clients").session(session))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should allow Client to access allowed resource")
    void shouldAllowClientToAccessAllowedResource() throws Exception {
        // 1. Login as Client
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("client@test.com");
        loginDTO.setPassword("client123");

        MockHttpSession session = (MockHttpSession) mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andReturn()
                .getRequest()
                .getSession();

        // 2. Access allowed resource (Products)
        mockMvc.perform(get("/api/products").session(session))
                .andExpect(status().isOk());
    }
}
