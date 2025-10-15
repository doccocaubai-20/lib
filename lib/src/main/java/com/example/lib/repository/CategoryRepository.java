package com.example.lib.repository;

import com.example.lib.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    // JpaRepository đã cung cấp đủ các phương thức CRUD cơ bản.
    // Bạn có thể thêm các phương thức truy vấn tùy chỉnh ở đây nếu cần,
    // ví dụ: Category findByName(String name);
}