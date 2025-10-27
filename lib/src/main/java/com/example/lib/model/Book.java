package com.example.lib.model;
import lombok.Data;
import java.time.LocalDate;

@Data
public class Book {
    private Long id;
    private String title;
    private Author author;
    private Category category;
    private LocalDate publishDate;
    private Integer pageCount;
    private int quantity;
    private String pdfPath;
    private String description;
    private String image; 
    private String language;
    private Double averageRating = 0.0; 
}