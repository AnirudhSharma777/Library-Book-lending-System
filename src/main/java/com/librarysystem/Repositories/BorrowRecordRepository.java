package com.librarysystem.Repositories;

import com.librarysystem.Entities.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BorrowRecordRepository extends JpaRepository<BorrowRecord,String> {

    Optional<BorrowRecord> findByUserAndBookAndReturnDateIsNull(User user, Book book);

    List<BorrowRecord> findByUser(User user);

    List<BorrowRecord> findByReturnDateIsNull();

    List<BorrowRecord> findByBook(Book book);
}
