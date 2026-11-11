package com.librarysystem.Controller;

import com.jayway.jsonpath.JsonPath;
import com.librarysystem.Config.SecurityConfig;
import com.librarysystem.Dto.BookDto;
import com.librarysystem.Entities.Book;
import com.librarysystem.Entities.BorrowRecord;
import com.librarysystem.Exception.ResourceNotFoundException;
import com.librarysystem.Services.BookServiceImpl;
import com.librarysystem.Services.BorrowRecordServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
@Import(SecurityConfig.class) // load the app's real security rules instead of Spring's defaults
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookServiceImpl bookService;

    @MockitoBean
    private BorrowRecordServiceImpl borrowRecordService;

    @MockitoBean
    private AuthenticationProvider authenticationProvider; // SecurityConfig needs one; nothing calls it

    @Captor
    ArgumentCaptor<BookDto> bookDtoCaptor;

    // The image part of a multipart request. Field name "imageUrl" must match BookDto.
    // Any bytes work, because FileUpload is mocked.
    private MockMultipartFile coverImage() {
        return new MockMultipartFile("imageUrl", "cover.png", "image/png", "fake-image".getBytes());
    }

    private List<Book> buildBookLibrary() {
        List<Book> books = new ArrayList<>();
        for (int i =0; i <3; i++) {
            String id = "b" + i;
            String title = "Book" + i;
            String author = "Author" + i;
            String url = "/url/" + i;
            String category = "Category" + i;
            Book b = new Book(id, title, author, url, category, 1, 1);
            books.add(b);
        }
        return books;
    }
    // 1. No login, GET /api/v1/books -> 200 with the list of books (public endpoint)
    @Test
    void getAllBooks_returns200WithoutLogin() throws Exception {
        List<Book> bookList = buildBookLibrary();
        when(bookService.getAllBooks()).thenReturn(bookList);

        mockMvc.perform(get("/api/v1/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].id").value("b0"))
                .andExpect(jsonPath("$[0].title").value("Book0"))
                .andExpect(jsonPath("$[0].author").value("Author0"))
                .andExpect(jsonPath("$[0].imageUrl").value("/url/0"))
                .andExpect(jsonPath("$[0].category").value("Category0"))
                .andExpect(jsonPath("$[0].totalCopies").value(1))
                .andExpect(jsonPath("$[0].availableCopies").value(1));
    }

    // 2. No login, GET /api/v1/books/1, service throws ResourceNotFoundException -> 404 + $.message
    @Test
    void getBookById_returns404WhenBookNotFound() throws Exception {
        when(bookService.getBookById("1")).thenThrow(new ResourceNotFoundException("Book not found with ID: 1"));
        mockMvc.perform(get("/api/v1/books/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Book not found with ID: 1"));

    }

    // 3. Admin, DELETE /api/v1/books/1 -> 204, and the service deletes "1"
    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void deleteBook_returns204ForAdmin() throws Exception {
        mockMvc.perform(delete("/api/v1/books/1"))
                .andExpect(status().isNoContent());

        verify(bookService).deleteBook("1");
    }

    // 4. Member, DELETE /api/v1/books/1 -> 403, and the service is never called
    @Test
    @WithMockUser(authorities = "ROLE_MEMBER")
    void deleteBook_returns403ForMember() throws Exception {
        mockMvc.perform(delete("/api/v1/books/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message")
                        .value("You do not have permission to perform this action"));

        verify(bookService, never()).deleteBook(any());
    }

    // 5. No login, DELETE /api/v1/books/1 -> 401, and the service is never called
    @Test
    void deleteBook_returns401WithoutLogin() throws Exception {
        mockMvc.perform(delete("/api/v1/books/1"))
                .andExpect(status().isUnauthorized());

        verify(bookService, never()).deleteBook(any());
    }

    // 6. Member, POST /api/v1/books/borrow/book-1 -> 200 + $.message,
    //    and the service is called with the logged-in user's email
    @Test
    @WithMockUser(username = "harryl423@gmail.com", authorities = "ROLE_MEMBER")
    void borrowBook_returns200AndUsesLoggedInUser() throws Exception {
        BorrowRecord borrowRecord = new BorrowRecord();
        borrowRecord.setBook(Book.builder().title("Book of the year").build());
        when(borrowRecordService.borrowBook("book-1", "harryl423@gmail.com"))
                .thenReturn(borrowRecord);

        mockMvc.perform(post("/api/v1/books/borrow/book-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Book 'Book of the year' borrowed successfully by harryl423@gmail.com"));

        verify(borrowRecordService).borrowBook("book-1","harryl423@gmail.com");
    }

    @Test
    @WithMockUser(username = "harryl423@gmail.com", authorities = "ROLE_MEMBER")
    void returnBook_returns200AndUsesLoggedInUser() throws Exception {

        BorrowRecord returnRecord = new BorrowRecord();
        returnRecord.setBook(Book.builder().title("Book of the year").build());
        when(borrowRecordService.returnBook("book-1", "harryl423@gmail.com")).thenReturn(returnRecord);
        mockMvc.perform(post("/api/v1/books/return/book-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Book 'Book of the year' returned successfully by harryl423@gmail.com"));

        verify(borrowRecordService).returnBook("book-1","harryl423@gmail.com");
    }

    // ---------- add / update book ----------
    // Both take form fields + an image file, so requests use multipart(...).

    // 1. Admin, POST /api/v1/books with all fields + image -> 201 with the book JSON,
    //    and an ArgumentCaptor shows the form fields were copied into BookDto
    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void addBook_returns201AndPassesFormFieldsToService() throws Exception {
        MockMultipartFile image = coverImage();

        Book b = Book.builder()
                .id("c1")
                .title("Clean Code")
                .author("Robert Martin")
                .category("Programming")
                .totalCopies(3)
                .availableCopies(3).build();

        when(bookService.addBook(any(BookDto.class))).thenReturn(b);
        mockMvc.perform(multipart("/api/v1/books")      // a POST by default
                        .file(image)
                        .param("title", "Clean Code")   // every other field is a text form field
                        .param("author", "Robert Martin")
                        .param("category", "Programming")
                        .param("totalCopies", "3"))     // always a String; Spring converts it to Integer
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Clean Code"))
                .andExpect(jsonPath("$.author").value("Robert Martin"))
                .andExpect(jsonPath("$.category").value("Programming"))
                .andExpect(jsonPath("$.totalCopies").value(3));

        verify(bookService).addBook(bookDtoCaptor.capture());
        assertEquals("Clean Code", bookDtoCaptor.getValue().title());
        assertEquals("Robert Martin", bookDtoCaptor.getValue().author());
        assertEquals("Programming", bookDtoCaptor.getValue().category());
        assertEquals(3, bookDtoCaptor.getValue().totalCopies());
        assertEquals("cover.png", bookDtoCaptor.getValue().imageUrl().getOriginalFilename());

    }

    // 2. Member, POST /api/v1/books -> 403, and the service is never called
    @Test
    @WithMockUser(authorities = "ROLE_MEMBER")
    void addBook_returns403ForMember() throws Exception {

        MockMultipartFile image = coverImage();

        mockMvc.perform(multipart("/api/v1/books")      // a POST by default
                        .file(image)
                        .param("title", "Clean Code")   // every other field is a text form field
                        .param("author", "Robert Martin")
                        .param("category", "Programming")
                        .param("totalCopies", "3"))     // always a String; Spring converts it to Integer
                .andExpect(status().isForbidden());
        verify(bookService, never()). addBook(any());
    }

    // 3. Admin, POST /api/v1/books with blank title and totalCopies = 0 ->
    //    400, $.errors.title and $.errors.totalCopies exist, and the service is never called
    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void addBook_returns400WhenFieldsInvalid() throws Exception {
        MockMultipartFile image = coverImage();

        mockMvc.perform(multipart("/api/v1/books")      // a POST by default
                        .file(image)
                        .param("title", "")   // every other field is a text form field
                        .param("author", "Robert Martin")
                        .param("category", "Programming")
                        .param("totalCopies", "0"))     // always a String; Spring converts it to Integer
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").exists())
                .andExpect(jsonPath("$.errors.totalCopies").exists());

        verify(bookService, never()).addBook(any());
    }

    // 4. Admin, PUT /api/v1/books/1 -> 200 with the updated book,
    //    and the service is called with id "1"
    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void updateBook_returns200ForAdmin() throws Exception {
        MockMultipartFile image = coverImage();

        Book b = Book.builder()
                .id("1")
                .title("Clean Architecture")
                .author("Robert Roy")
                .category("Programming")
                .totalCopies(5)
                .availableCopies(5).build();

        when(bookService.updateBook(eq("1"), any(BookDto.class))).thenReturn(b);
        mockMvc.perform(multipart(HttpMethod.PUT, "/api/v1/books/1")
                        .file(image)
                        .param("title", "Clean Architecture")   // every other field is a text form field
                        .param("author", "Robert Roy")
                        .param("category", "Programming")
                        .param("totalCopies", "2"))     // always a String; Spring converts it to Integer
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Clean Architecture"))
                .andExpect(jsonPath("$.author").value("Robert Roy"))
                .andExpect(jsonPath("$.category").value("Programming"))
                .andExpect(jsonPath("$.totalCopies").value(5));

        verify(bookService).updateBook(eq("1"), bookDtoCaptor.capture());
        assertEquals("Clean Architecture", bookDtoCaptor.getValue().title());
        assertEquals("Robert Roy", bookDtoCaptor.getValue().author());
        assertEquals("Programming", bookDtoCaptor.getValue().category());
        assertEquals(2, bookDtoCaptor.getValue().totalCopies());
        assertEquals("cover.png", bookDtoCaptor.getValue().imageUrl().getOriginalFilename());

    }

    // 5. Admin, PUT /api/v1/books/1, service throws ResourceNotFoundException -> 404 + $.message
    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void updateBook_returns404WhenBookNotFound() throws Exception {
        MockMultipartFile image = coverImage();

        when(bookService.updateBook(eq("1"), any(BookDto.class)))
                .thenThrow(new ResourceNotFoundException("Book not found with ID: 1"));

        mockMvc.perform(multipart(HttpMethod.PUT, "/api/v1/books/1")
                        .file(image)
                        .param("title", "Clean Architecture")   // every other field is a text form field
                        .param("author", "Robert Roy")
                        .param("category", "Programming")
                        .param("totalCopies", "2"))     // always a String; Spring converts it to Integer
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Book not found with ID: 1"));

        verify(bookService).updateBook(eq("1"), bookDtoCaptor.capture());
        assertEquals("Clean Architecture", bookDtoCaptor.getValue().title());
        assertEquals("Robert Roy", bookDtoCaptor.getValue().author());
        assertEquals("Programming", bookDtoCaptor.getValue().category());
        assertEquals(2, bookDtoCaptor.getValue().totalCopies());
        assertEquals("cover.png", bookDtoCaptor.getValue().imageUrl().getOriginalFilename());
    }

    // 6. Member, PUT /api/v1/books/1 -> 403, and the service is never called
    @Test
    @WithMockUser(authorities = "ROLE_MEMBER")
    void updateBook_returns403ForMember() throws Exception {
        MockMultipartFile image = coverImage();

        mockMvc.perform(multipart(HttpMethod.PUT, "/api/v1/books/1")      
                        .file(image)
                        .param("title", "Clean Architecture")   // every other field is a text form field
                        .param("author", "Robert Roy")
                        .param("category", "Programming")
                        .param("totalCopies", "2"))       // always a String; Spring converts it to Integer
                .andExpect(status().isForbidden());
        verify(bookService, never()). updateBook(any(), any());
    }
}
