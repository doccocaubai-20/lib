package com.example.lib.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.lib.model.Book;

public interface BookRepository extends JpaRepository<Book,Long>{
    // Phương thức để tìm sách theo tiêu đề (không phân biệt hoa thường)
    List<Book> findByTitleContainingIgnoreCase(String title);
}
