package com.example.lib.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.lib.model.Book;
import com.example.lib.model.Review;
import com.example.lib.repository.BookRepository;
import com.example.lib.repository.ReviewRepository;

@Service
public class BookService {
    private final BookRepository repo;
    private final ReviewRepository reviewRepo;
    public BookService(BookRepository repo, ReviewRepository reviewRepo) { 
        this.repo = repo; 
        this.reviewRepo = reviewRepo;
    }
    public List<Book> getAll() { return repo.findAll(); }
    public Book save(Book b) { return repo.save(b); }
    public Book update(Long id, Book newBook) {
        return repo.findById(id).map(b -> {
            b.setTitle(newBook.getTitle());
            b.setAuthor(newBook.getAuthor());
            b.setCategory(newBook.getCategory());
            b.setQuantity(newBook.getQuantity());
            b.setPublishDate(newBook.getPublishDate());
            b.setPdfPath(newBook.getPdfPath());
            return repo.save(b);
        }).orElse(null);
    }
    public void delete(Long id) { repo.deleteById(id); }
    public void calculateAndSetAverageRating(Long bookId){
        Book book = repo.findById(bookId).orElse(null);
        if (book != null){
            List<Review> reviews = reviewRepo.findByBookId(bookId);
            if (reviews.isEmpty()){
                book.setAverageRating(0.0);
            }else{
                double average = reviews.stream()
                                        .mapToInt(Review::getRating)
                                        .average()
                                        .orElse(0.0);
                double roundedAverage = Math.round(average * 10.0) / 10.0;
                book.setAverageRating(roundedAverage);
            }   
            repo.save(book);
        }
    }


}

