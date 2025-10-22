package com.example.lib.repository;

import com.example.lib.model.Book;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class BookSpecification {

    public static Specification<Book> filterBy(String keyword, List<Long> authorIds, List<Long> categoryIds, Double minRating) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Lọc theo từ khóa (title)
            if (keyword != null && !keyword.isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), "%" + keyword.toLowerCase() + "%"));
            }

            // Lọc theo tác giả
            if (authorIds != null && !authorIds.isEmpty()) {
                predicates.add(root.get("author").get("id").in(authorIds));
            }

            // Lọc theo thể loại
            if (categoryIds != null && !categoryIds.isEmpty()) {
                predicates.add(root.get("category").get("id").in(categoryIds));
            }

            // Lọc theo điểm đánh giá
            if (minRating != null && minRating > 0) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("averageRating"), minRating));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}