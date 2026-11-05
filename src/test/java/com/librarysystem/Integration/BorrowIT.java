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

// Borrowing books and reading borrow history through the real app.
// Keep these three annotations identical in every *IT class, so Spring starts the app once and reuses it.
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BorrowIT {

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

    // Save a book with bookRepository, member borrows it ->
    // 200, my-history has 1 record, and availableCopies in the database went down by 1
    @Test
    void borrowBook_createsRecordAndDecreasesCopiesInDatabase() throws Exception {
        fail("TODO: write this test");
    }

    // Borrow history must not contain the user's password hash
    // (fails today: "password": "$2a$..." is in the response)
    @Test
    void borrowHistory_doesNotExposePassword() throws Exception {
        fail("TODO: write this test");
    }
}
