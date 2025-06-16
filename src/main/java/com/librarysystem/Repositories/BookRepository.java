package com.librarysystem.Repositories;

import com.librarysystem.Entities.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book,String> {
    List<Book> findByAvailableCopiesGreaterThan(Integer count);
}
