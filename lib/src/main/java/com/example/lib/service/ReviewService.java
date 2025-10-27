package com.example.lib.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.lib.model.Book;
import com.example.lib.model.Review;
import com.example.lib.model.User;
import com.example.lib.repository.ReviewDAO;
import com.example.lib.repository.BookDAO;

@Service
public class ReviewService {
    private final ReviewDAO reviewRepo;
    private final BookDAO bookDao; 

    public ReviewService(ReviewDAO reviewRepository, BookDAO bookDao){ 
        this.reviewRepo = reviewRepository;
        this.bookDao = bookDao;
    }

    @Transactional
    public void saveReview(Review review, User user, Book book){
        review.setUser(user);
        review.setBook(book);
        reviewRepo.save(review);
        bookDao.updateAverageRating(book.getId()); 
    }
}