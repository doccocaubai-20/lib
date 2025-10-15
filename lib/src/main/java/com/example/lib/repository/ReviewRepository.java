package com.example.lib.repository;

import com.example.lib.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Phương thức tùy chỉnh để tìm tất cả các đánh giá cho một cuốn sách cụ thể
    List<Review> findByBookId(Long bookId);

    // Phương thức tùy chỉnh để tìm tất cả các đánh giá của một người dùng cụ thể
    List<Review> findByUserId(Long userId);
}