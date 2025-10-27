package com.example.lib.model;

import lombok.Data;
import java.time.LocalDate;

@Data
public class Borrow {
    private Long id;
    private User user;
    private Book book;
    private LocalDate borrowDate;
    private LocalDate returnDate;
    private String status;
}