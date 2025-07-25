package com.librarysystem.Controller;


import com.librarysystem.Dto.BookDto;
import com.librarysystem.Entities.Book;
import com.librarysystem.Entities.BorrowRecord;
import com.librarysystem.ResponseDto.BorrowReturnResponseDto;
import com.librarysystem.Services.BookServiceImpl;
import com.librarysystem.Services.BorrowRecordServiceImpl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
@Tag(name = "Books", description = "Book management and lending API")
@SecurityRequirement(name = "Bearer Authentication")
public class BookController {

    private static final Logger logger = LoggerFactory.getLogger(BookController.class);

    private final BookServiceImpl service;
    private final BorrowRecordServiceImpl borrowRecordService;


    @Operation(summary = "Add a new book", description = "Accessible only by ADMIN")
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Book> addBook(@Valid @ModelAttribute BookDto request) throws IOException {
        Book newBook = service.addBook(request);
        return new ResponseEntity<>(newBook, HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing book", description = "Accessible only by ADMIN")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Book> updateBook(@PathVariable("id") String id, @Valid @ModelAttribute BookDto request) throws IOException{
        Book updatedBook = service.updateBook(id, request);
        return ResponseEntity.ok(updatedBook);
    }

    @Operation(summary = "Get all books (available or not)", description = "Accessible by ADMIN and MEMBER")
    @GetMapping
    public ResponseEntity<List<Book>> getAllBooks() {
        return ResponseEntity.ok(service.getAllBooks());
    }

    @Operation(summary = "Get a book by ID", description = "Accessible by ADMIN and MEMBER")
    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable("id") String id) {
        return ResponseEntity.ok(service.getBookById(id));
    }

    @Operation(summary = "Get only available books", description = "Accessible by ADMIN and MEMBER")
    @GetMapping("/available")
    public ResponseEntity<List<Book>> getAvailableBooks() {
        return ResponseEntity.ok(service.getAvailableBooks());
    }

    @Operation(summary = "Delete a book by ID", description = "Accessible only by ADMIN")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteBook(@PathVariable("id") String id) {
        service.deleteBook(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Borrow a book", description = "Accessible only by MEMBER")
    @PostMapping("/borrow/{bookId}")
    @PreAuthorize("hasAuthority('ROLE_MEMBER')")
    public ResponseEntity<BorrowReturnResponseDto> borrowBook(@PathVariable("bookId") String bookId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName(); // Get the username of the authenticated user

        logger.info("User: {}",username);
        BorrowRecord record = borrowRecordService.borrowBook(bookId, username);

        return ResponseEntity.ok(new BorrowReturnResponseDto(
                "Book '" + record.getBook().getTitle() + "' borrowed successfully by " + username,
                record.getBook().getId(),
                record.getBook().getImageUrl(),
                record.getBook().getTitle(),
                username
        ));
    }

    @Operation(summary = "Return a borrowed book", description = "Accessible only by MEMBER")
    @PostMapping("/return/{bookId}")
    @PreAuthorize("hasAuthority('ROLE_MEMBER')")
    public ResponseEntity<BorrowReturnResponseDto> returnBook(@PathVariable("bookId") String bookId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName(); // Get the username of the authenticated user
        logger.info("User: {}",username);
        BorrowRecord record = borrowRecordService.returnBook(bookId, username);
        return ResponseEntity.ok(new BorrowReturnResponseDto(
                "Book '" + record.getBook().getTitle() + "' returned successfully by " + username,
                record.getBook().getId(),
                record.getBook().getImageUrl(),
                record.getBook().getTitle(),
                username
        ));
    }

}
