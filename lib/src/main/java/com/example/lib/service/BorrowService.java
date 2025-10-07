package com.example.lib.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.lib.model.Borrow;
import com.example.lib.repository.BorrowRepository;

@Service
public class BorrowService {
    private final BorrowRepository repo;
    public BorrowService(BorrowRepository repo) { this.repo = repo; }

    public List<Borrow> getAll() { return repo.findAll(); }
    public Borrow save(Borrow b) { return repo.save(b); }
}