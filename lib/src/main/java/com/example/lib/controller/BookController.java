package com.example.lib.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.lib.model.Book;
import com.example.lib.service.BookService;

@RestController
@RequestMapping("api/books")
public class BookController {
    private final BookService bookService;
    public BookController(BookService bookService){
        this.bookService = bookService;
    }
    @GetMapping 
    public List<Book> getAll(){
        return bookService.getAll();
    }
    @PostMapping public Book add(@RequestBody Book b) { return bookService.save(b); }
    @PutMapping("/{id}") public Book update(@PathVariable Long id, @RequestBody Book b) { return bookService.update(id, b); }
    @DeleteMapping("/{id}") public void delete(@PathVariable Long id) { bookService.delete(id); }
}
