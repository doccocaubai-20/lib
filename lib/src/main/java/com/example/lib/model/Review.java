package com.example.lib.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

@Entity
@Table(name = "reviews")
@Data
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(nullable = false)
    private int rating; // SQL CHECK constraint sẽ được Hibernate Validator xử lý ở tầng ứng dụng

    @Lob
    private String comment;

    @Column(updatable = false)
    private Instant createdAt = Instant.now();
}