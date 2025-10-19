package com.example.lib.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.lib.model.Book;
import com.example.lib.model.Borrow;

import com.example.lib.model.User;

public interface BorrowRepository extends JpaRepository<Borrow,Long>{
    List<Borrow> findByUser(User user);
// Trong file BorrowRepository.java
boolean existsByBookAndUserAndStatus(Book book, User user, String status);
}
