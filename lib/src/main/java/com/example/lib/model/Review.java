package com.example.lib.model;

import lombok.Data;
import java.time.Instant;

@Data
public class Review {
    private Long id;
    private User user;
    private Book book;
    private int rating;
    private String comment;
    private Instant createdAt = Instant.now();
}