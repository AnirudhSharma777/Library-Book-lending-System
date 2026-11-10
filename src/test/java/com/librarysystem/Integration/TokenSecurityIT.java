package com.librarysystem.Integration;

import com.librarysystem.Entities.Role;
import com.librarysystem.Entities.User;
import com.librarysystem.Repositories.BookRepository;
import com.librarysystem.Repositories.BorrowRecordRepository;
import com.librarysystem.Repositories.UserRepository;
import com.librarysystem.Utils.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// What protected endpoints do with a missing, broken or expired token.
// Keep these three annotations identical in every *IT class, so Spring starts the app once and reuses it.
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TokenSecurityIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BorrowRecordRepository borrowRecordRepository;

    // Must match security.jwt.secret in application-test.yml
    private static final String TEST_SECRET = "dGVzdC1zZWNyZXQta2V5LWZvci11bml0LXRlc3RzLTEyMzQ=";

    // The database is shared by every test in the run, so clear it after each one.
    // Borrow records first: they point at users and books (foreign keys).
    @AfterEach
    void cleanUp() {
        borrowRecordRepository.deleteAll();
        bookRepository.deleteAll();
        userRepository.deleteAll();
    }

    // Protected endpoint with no Authorization header -> 401
    @Test
    void protectedEndpoint_returns401WithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/borrow-records/my-history")
                )
                .andExpect(status().isUnauthorized());
    }

    // Protected endpoint with a garbage token -> 401
    // (fails today: returns 500 "Malformed JWT JSON")
    @Test
    void protectedEndpoint_returns401WithGarbageToken() throws Exception {
        String malformedToken = "ewv345344.4dgdfg32345234.eynkj23423";
        String requestJwt = "Bearer " + malformedToken;

        mockMvc.perform(get("/api/v1/borrow-records/my-history")
                        .header("Authorization", requestJwt)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid or expired token"));
    }

    // Protected endpoint with an expired token -> 401
    // (fails today: returns 500)
    // Make the token with your own JwtService: test secret + negative expiration (see JwtServiceTest).
    @Test
    void protectedEndpoint_returns401WithExpiredToken() throws Exception {
        User user = User.builder()
                .name("Harry")
                .email("harryl423@gmail.com")
                .password("not-used-in-this-test")
                .role(Role.MEMBER)
                .build();
        userRepository.save(user);

        // Our OWN JwtService, made with `new`. Spring doesn't know about it,
        //    so changing it can't affect the app or any other test.
        JwtService expiredJwtService = new JwtService();

        // Fill the two @Value fields by hand (Spring only fills them on its own beans).
        //    Same secret as application-test.yml, so the app accepts our signature.
        ReflectionTestUtils.setField(expiredJwtService, "secretKey", TEST_SECRET);
        //    The token expires at "now + jwtExpiration". With -1000 ms, that's 1 second in the past.
        ReflectionTestUtils.setField(expiredJwtService, "jwtExpiration", -1000L);

        // A correctly signed token for a real user, already expired.
        String token = expiredJwtService.generateToken(user);

        // The app parses it, sees the expiry date has passed, and must reply 401.
        mockMvc.perform(get("/api/v1/borrow-records/my-history")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid or expired token"));
    }
}
