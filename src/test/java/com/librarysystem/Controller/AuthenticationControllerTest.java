package com.librarysystem.Controller;


import com.librarysystem.Dto.RegisterDto;
import com.librarysystem.Exception.AccountAlreadyExistsException;
import com.librarysystem.ResponseDto.RegisterResponseDto;
import com.librarysystem.Services.AuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthenticationController.class)
@AutoConfigureMockMvc(addFilters = false) // `addFilters = false` turns off the security filters.
public class AuthenticationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthServiceImpl authService;

    @Test
    public void signup_returns200AndCreatedUser() throws Exception {

        String requestJson = """
            {
              "name": "Harry",
              "email": "harryl423@gmail.com",
              "password": "not.a.password",
              "role": "member"
            }
            """;
        RegisterResponseDto response =
                new RegisterResponseDto("user-1",
                        "Harry",
                        "harryl423@gmail.com",
                        "MEMBER");
        when(authService.signup(any(RegisterDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("user-1"))
                .andExpect(jsonPath("$.email").value("harryl423@gmail.com"))
                .andExpect(jsonPath("$.role").value("MEMBER"));


    }

    @Test
    public void signup_returns400WhenEmailBlank() throws Exception {
        String requestJson = """
            {
              "name": "Harry",
              "email": "",
              "password": "not.a.password",
              "role": "member"
            }
            """;
        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists());

        verify(authService, never()).signup(any());
    }

    @Test
    public void signup_returns400WhenEmailExists() throws Exception {
        String requestJson = """
            {
              "name": "Harry",
              "email": "harryl423@gmail.com",
              "password": "not.a.password",
              "role": "member"
            }
            """;
        when(authService.signup(any())).thenThrow(new AccountAlreadyExistsException("Account already exists using this Email"));

        mockMvc.perform(post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Account already exists using this Email"));
    }

    @Test
    public void login_returns401WhenBadCredentials() throws Exception {
        String requestJson = """
            {
              "email": "harryl423@gmail.com",
              "password": "wrong.password"
            }
            """;
        when(authService.login(any()))
                .thenThrow(new BadCredentialsException("Bad Credentials"));

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
        )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication Failed: Bad Credentials"));
    }
}
