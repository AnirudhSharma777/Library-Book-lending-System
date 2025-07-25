package com.librarysystem.Services;

import com.librarysystem.Dto.BookDto;
import com.librarysystem.Entities.Book;

import java.io.IOException;
import java.util.List;

public interface BookService {

    Book addBook(BookDto request)throws IOException;
    Book updateBook(String id, BookDto request) throws IOException;
    void deleteBook(String id);
    List<Book> getAllBooks();
    Book getBookById(String id);
    List<Book> getAvailableBooks();
    void decreaseAvailableCopies(Book book);
    void increaseAvailableCopies(Book book);
}
