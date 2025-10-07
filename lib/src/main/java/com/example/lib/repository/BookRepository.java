package com.example.lib.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.lib.model.Book;

public interface BookRepository extends JpaRepository<Book,Long>{

}
