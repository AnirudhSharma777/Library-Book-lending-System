package com.librarysystem.Services;

import com.librarysystem.Entities.BorrowRecord;

import java.util.List;

public interface BorrowRecordService {

    BorrowRecord borrowBook(String bookId,String email);
    BorrowRecord returnBook(String bookId,String email);
    List<BorrowRecord> getAllBorrowHistory();
    List<BorrowRecord> getUserBorrowHistory(String email);

}
