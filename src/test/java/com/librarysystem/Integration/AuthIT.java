package com.librarysystem.Integration;

import com.jayway.jsonpath.JsonPath;
import com.librarysystem.Repositories.BookRepository;
import com.librarysystem.Repositories.BorrowRecordRepository;
import com.librarysystem.Repositories.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
// Signup and login through the real app.
// Keep these three annotations identical in every *IT class, so Spring starts the app once and reuses it.
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BorrowRecordRepository borrowRecordRepository;

    // The database is shared by every test in the run, so clear it after each one.
    // Borrow records first: they point at users and books (foreign keys).
    @AfterEach
    void cleanUp() {
        borrowRecordRepository.deleteAll();
        bookRepository.deleteAll();
        userRepository.deleteAll();
    }

    // signup -> login -> GET /api/v1/borrow-records/my-history with the token -> 200 and []
    @Test
    void signupLoginThenCallProtectedEndpoint_returns200() throws Exception {
        //Sign up first
        String requestJson = """
            {
              "name": "Harry",
              "email": "harryl423@gmail.com",
              "password": "not.a.password",
              "role": "member"
            }
            """;

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isString())
                .andExpect(jsonPath("$.email").value("harryl423@gmail.com"))
                .andExpect(jsonPath("$.role").value("MEMBER"));

        // Then login with that credentials to get a JWT token
        String requestJsonLogin = """
            {
              "email": "harryl423@gmail.com",
              "password": "not.a.password"
            }
            """;

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJsonLogin))
                .andExpect(status().isOk())      // check login worked before using the token
                .andReturn();                    // give me the response

        String body = loginResult.getResponse().getContentAsString();
        String token = JsonPath.read(body, "$.token");

        // Then check history
        String requestJwt = "Bearer " + token;
        mockMvc.perform(get("/api/v1/borrow-records/my-history")
                        .header("Authorization", requestJwt)
                 )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // Login with an unknown email and with a wrong password -> both 401 with the identical message
    @Test
    void login_unknownEmailAndWrongPassword_returnSame401() throws Exception {
        //Sign up first
        String requestJson = """
            {
              "name": "Harry",
              "email": "harryl423@gmail.com",
              "password": "not.a.password",
              "role": "member"
            }
            """;

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                )
                .andExpect(status().isOk());
        // wrong password
        String requestJsonLogin = """
            {
              "email": "harryl423@gmail.com",
              "password": "wrong.password"
            }
            """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJsonLogin))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication Failed: Bad credentials"));

        requestJsonLogin = """
            {
              "email": "notharryl423@gmail.com",
              "password": "wrong.password"
            }
            """;
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJsonLogin))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication Failed: Bad credentials"));

    }

    // Signup with "role": "admin" must not create an admin account
    // (fails today: an ADMIN account is created)
    // Decide the expected behavior first: reject with 400, or create a MEMBER.
    @Test
    void signup_cannotCreateAdminAccount() throws Exception {
        String requestJson = """
            {
              "name": "Harry",
              "email": "harryl423@gmail.com",
              "password": "not.a.password",
              "role": "admin"
            }
            """;

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.role").exists());
    }


}
