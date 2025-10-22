package com.example.lib.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.lib.model.Book;
import com.example.lib.model.Category;

public interface BookRepository extends JpaRepository<Book,Long>, JpaSpecificationExecutor<Book>{
    // Phương thức để tìm sách theo tiêu đề (không phân biệt hoa thường)
    List<Book> findByTitleContainingIgnoreCase(String title);
    List<Book> findTop4ByOrderByAverageRatingDesc();

    @Query("SELECT b FROM Book b WHERE b.category IN :categories AND b.id NOT IN :borrowedBookIds ORDER BY b.averageRating DESC, b.id ASC LIMIT 4")
    List<Book> findTop4ByCategoriesAndNotInIds(@Param("categories") List<Category> categories, @Param("borrowedBookIds") List<Long> borrowedBookIds);
}
