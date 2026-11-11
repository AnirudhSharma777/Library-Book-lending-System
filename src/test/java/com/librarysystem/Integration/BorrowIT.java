package com.librarysystem.Integration;

import com.jayway.jsonpath.JsonPath;
import com.librarysystem.Entities.Book;
import com.librarysystem.Entities.BorrowRecord;
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

    private String signUp_and_Login() throws Exception {
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

        return JsonPath.read(body, "$.token");
    }


    private String createBook_saveBook_returnSaveId(int savedCopies) {
        Book b = Book.builder()
                .title("BOOK OF THE YEAR")
                .author("Best Author")
                .imageUrl("an-image-url")
                .category("Horror")
                .totalCopies(savedCopies)
                .availableCopies(savedCopies).build();
        Book savedBook = bookRepository.save(b);
        return savedBook.getId();
    }
    // Save a book with bookRepository, member borrows it ->
    // 200, my-history has 1 record, and availableCopies in the database went down by 1
    @Test
    void borrowBook_createsRecordAndDecreasesCopiesInDatabase() throws Exception {

        String token = signUp_and_Login();
        int copies = 5;

        String bookId = createBook_saveBook_returnSaveId(copies);

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

        String token = signUp_and_Login();
        int copies = 5;
        String bookId = createBook_saveBook_returnSaveId(copies);

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


    @Test
    public void borrowBook_returnThatBook_andBorrowAgain_returnsOK() throws Exception {
        String token = signUp_and_Login();
        int copies = 5;
        String bookId = createBook_saveBook_returnSaveId(copies);

        mockMvc.perform(post("/api/v1/books/borrow/" + bookId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Book 'BOOK OF THE YEAR' borrowed successfully by harryl423@gmail.com"));

        //return it
        mockMvc.perform(post("/api/v1/books/return/" + bookId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Book 'BOOK OF THE YEAR' returned successfully by harryl423@gmail.com"));

        // Borrow the same book again
        mockMvc.perform(post("/api/v1/books/borrow/" + bookId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Book 'BOOK OF THE YEAR' borrowed successfully by harryl423@gmail.com"));

    }
    @Test
    public void borrowBook_borrowSameBookWithoutReturning_returnBadRequest() throws Exception {
        String token = signUp_and_Login();
        int copies = 5;
        String bookId = createBook_saveBook_returnSaveId(copies);

        mockMvc.perform(post("/api/v1/books/borrow/" + bookId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Book 'BOOK OF THE YEAR' borrowed successfully by harryl423@gmail.com"));

        // Borrow the same book again

        mockMvc.perform(post("/api/v1/books/borrow/" + bookId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("User harryl423@gmail.com has already borrowed 'BOOK OF THE YEAR'."));
    }

    @Test
    void returnBook_setsReturnDateAndRestoresCopies() throws Exception {
        int copies = 5;
        String token = signUp_and_Login();
        String bookId = createBook_saveBook_returnSaveId(copies);
        mockMvc.perform(post("/api/v1/books/borrow/" + bookId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());          // just make sure setup worked

        mockMvc.perform(post("/api/v1/books/return/" + bookId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        Book after = bookRepository.findById(bookId).orElseThrow();
        assertEquals(5, after.getAvailableCopies());  // back to where it started

        BorrowRecord record = borrowRecordRepository.findAll().get(0);
        assertNotNull(record.getReturnDate());
    }
    @Test
    void returnBook_returns400WhenNotBorrowed() throws Exception {
        int copies = 5;
        String token = signUp_and_Login();
        String bookId = createBook_saveBook_returnSaveId(copies);

        mockMvc.perform(post("/api/v1/books/return/" + bookId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());

        Book after = bookRepository.findById(bookId).orElseThrow();
        assertEquals(5, after.getAvailableCopies());

        assertEquals(0, borrowRecordRepository.findAll().size());
    }

}
