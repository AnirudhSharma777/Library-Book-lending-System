package com.librarysystem.Integration;

import com.jayway.jsonpath.JsonPath;
import com.librarysystem.Entities.Book;
import com.librarysystem.Repositories.BookRepository;
import com.librarysystem.Repositories.BorrowRecordRepository;
import com.librarysystem.Repositories.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

        int copies = 5;
        Book b = Book.builder()
                .title("BOOK OF THE YEAR")
                .author("Best Author")
                .imageUrl("an-image-url")
                .category("Horror")
                .totalCopies(copies)
                .availableCopies(copies).build();
        Book savedBook = bookRepository.save(b);
        String bookId = savedBook.getId();

        mockMvc.perform(post("/api/v1/books/borrow/" + bookId)
                    .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Book 'BOOK OF THE YEAR' borrowed successfully by harryl423@gmail.com"));

        mockMvc.perform(get("/api/v1/borrow-records/my-history")
                .header("Authorization", "Bearer " + token)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        Book after = bookRepository.findById(bookId).orElseThrow();
        assertEquals(copies - 1, after.getAvailableCopies());
    }

    // Borrow history must not contain the user's password hash
    // (fails today: "password": "$2a$..." is in the response)
    @Test
    void borrowHistory_doesNotExposePassword() throws Exception {
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

        int copies = 5;
        Book b = Book.builder()
                .title("BOOK OF THE YEAR")
                .author("Best Author")
                .imageUrl("an-image-url")
                .category("Horror")
                .totalCopies(copies)
                .availableCopies(copies).build();
        Book savedBook = bookRepository.save(b);
        String bookId = savedBook.getId();

        mockMvc.perform(post("/api/v1/books/borrow/" + bookId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Book 'BOOK OF THE YEAR' borrowed successfully by harryl423@gmail.com"));

        MvcResult response = mockMvc.perform(get("/api/v1/borrow-records/my-history")
                .header("Authorization", "Bearer " + token)
        )
                .andExpect(jsonPath("$[0].user.username")
                        .value("harryl423@gmail.com"))
                .andExpect(jsonPath("$[0].user.password")
                        .doesNotExist())
                .andReturn();

        String hashPrefix = response.getResponse().getContentAsString();
        assertFalse(hashPrefix.contains("$2a$"));
    }
}
