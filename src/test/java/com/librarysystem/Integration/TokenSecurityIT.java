package com.librarysystem.Integration;

import com.librarysystem.Repositories.BookRepository;
import com.librarysystem.Repositories.BorrowRecordRepository;
import com.librarysystem.Repositories.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.fail;

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
        fail("TODO: write this test");
    }

    // Protected endpoint with a garbage token -> 401
    // (fails today: returns 500 "Malformed JWT JSON")
    @Test
    void protectedEndpoint_returns401WithGarbageToken() throws Exception {
        fail("TODO: write this test");
    }

    // Protected endpoint with an expired token -> 401
    // (fails today: returns 500)
    // Make the token with your own JwtService: test secret + negative expiration (see JwtServiceTest).
    @Test
    void protectedEndpoint_returns401WithExpiredToken() throws Exception {
        fail("TODO: write this test");
    }
}
