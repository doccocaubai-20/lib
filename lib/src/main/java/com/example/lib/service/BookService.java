package com.example.lib.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.lib.model.Book;
import com.example.lib.repository.BookDAO;

@Service
public class BookService {
    private final BookDAO repo;
    public BookService(BookDAO repo) { 
        this.repo = repo; 
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
            return repo.update(b);
        }).orElse(null);
    }
    public void delete(Long id) { repo.deleteById(id); }

}

