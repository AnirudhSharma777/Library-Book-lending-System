package com.librarysystem.Controller;

import com.librarysystem.Config.SecurityConfig;
import com.librarysystem.Entities.Role;
import com.librarysystem.Entities.User;
import com.librarysystem.Services.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserServiceImpl userService;
    @MockitoBean
    private AuthenticationProvider authenticationProvider;

    @Test
    @WithMockUser(username= "harryl423@gmail.com", authorities = "ROLE_ADMIN")
    public void getUserByEmail_returnUserWithOk() throws Exception {
        User u = User.builder()
                .id("u1")
                .name("Alice")
                .email("alice@gmail.com")
                .password("alice123")
                .role(Role.MEMBER).build();

        when(userService.getUserByUsername("alice@gmail.com")).thenReturn(u);

        mockMvc.perform(get("/api/v1/user/email/alice@gmail.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value("alice@gmail.com"))
                .andExpect(jsonPath("$.name").value("Alice"))
                .andExpect(jsonPath("$.role").value("MEMBER"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @WithMockUser(username= "harryl423@gmail.com", authorities = "ROLE_MEMBER")
    public void tryEveryWayOfGettingAUser_returns403ForMemberRequest() throws Exception {
        mockMvc.perform(get("/api/v1/user/email/alice@gmail.com"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/user/u1"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isForbidden());
    }

    // Same as the Above. Just another way to do so
    @ParameterizedTest
    @ValueSource(strings = {
            "/api/v1/user/email/alice@gmail.com",
            "/api/v1/user/u1",
            "/api/v1/users"
    })
    @WithMockUser(username = "harryl423@gmail.com", authorities = "ROLE_MEMBER")
    void userEndpoints_return403ForMember(String url) throws Exception {
        mockMvc.perform(get(url))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }

}
