package com.example.lib.repository;

import com.example.lib.model.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {
    // Tương tự, JpaRepository đã cung cấp sẵn các hàm cần thiết.
    // Có thể thêm: Author findByName(String name);
}