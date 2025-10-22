package com.example.lib.service;

import org.springframework.stereotype.Service;

import com.example.lib.model.Book;
import com.example.lib.model.Review;
import com.example.lib.model.User;
import com.example.lib.repository.ReviewRepository;

import jakarta.transaction.Transactional;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepo;
    private final BookService bookService;

    public ReviewService(ReviewRepository reviewRepository,BookService bookService){
        this.reviewRepo = reviewRepository;
        this.bookService = bookService;
    }

    @Transactional
    public void saveReview(Review review,User user,Book book){
        review.setUser(user);
        review.setBook(book);
        reviewRepo.save(review);
        bookService.calculateAndSetAverageRating(book.getId());
    }
}
