package com.example.lib.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "books")
@Data
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @ManyToOne
    @JoinColumn(name = "author_id")
    private Author author;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    private LocalDate publishDate;

    private Integer pageCount;


    @Column(nullable = false, columnDefinition = "INT UNSIGNED DEFAULT 0")
    private int quantity;

    private String pdfPath;

    @Lob // Dùng cho các trường văn bản dài
    private String description;

    private String image; // Đường dẫn đến ảnh bìa sách

    @Column(length = 10)
    private String language;
}