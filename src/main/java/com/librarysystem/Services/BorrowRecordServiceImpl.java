package com.librarysystem.Services;

import com.librarysystem.Entities.Book;
import com.librarysystem.Entities.BorrowRecord;
import com.librarysystem.Entities.User;
import com.librarysystem.Exception.AlreadyBorrowedException;
import com.librarysystem.Exception.BookNotBorrowedException;
import com.librarysystem.Repositories.BorrowRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BorrowRecordServiceImpl implements BorrowRecordService{

    private final BorrowRecordRepository repository;
    private final UserServiceImpl userService;
    private final BookServiceImpl bookService;

    @Override
    public BorrowRecord borrowBook(String bookId, String email) {
        User user = userService.getUserByUsername(email);
        Book book = bookService.getBookById(bookId);

        // Find the active borrow record for this user and book
        repository.findByUserAndBookAndReturnDateIsNull(user, book)
        .ifPresent(b -> {
            throw new AlreadyBorrowedException("User " + email + " has already borrowed '" + book.getTitle() + "'.");
        });

        bookService.decreaseAvailableCopies(book);

        BorrowRecord record = BorrowRecord.builder()
                                .user(user)
                                .book(book)
                                .borrowDate(LocalDate.now())
                                .build();


        return repository.save(record);
    }

    @Override
    public BorrowRecord returnBook(String bookId, String email) {
        User user = userService.getUserByUsername(email);
        Book book = bookService.getBookById(bookId);

        // Find the active borrow record for this user and book
        BorrowRecord borrowRecord = repository.findByUserAndBookAndReturnDateIsNull(user, book)
                .orElseThrow(() -> new BookNotBorrowedException("Book '" + book.getTitle() + "' was not borrowed by user " + email + "."));

        // Increase available copies
        bookService.increaseAvailableCopies(book);

        borrowRecord.setReturnDate(LocalDate.now());
        return repository.save(borrowRecord);
    }

    @Override
    public List<BorrowRecord> getAllBorrowHistory() {
        return repository.findAll();
    }

    @Override
    public List<BorrowRecord> getUserBorrowHistory(String email) {
        User user = userService.getUserByUsername(email);
        return repository.findByUser(user);
    }
}
