package com.librarysystem.Services;

import com.librarysystem.Dto.BookDto;
import com.librarysystem.Entities.Book;

import java.util.List;

public interface BookService {

    Book addBook(BookDto request);
    Book updateBook(String id, BookDto request);
    void deleteBook(String id);
    List<Book> getAllBooks();
    Book getBookById(String id);
    List<Book> getAvailableBooks();
    void decreaseAvailableCopies(Book book);
    void increaseAvailableCopies(Book book);
}
