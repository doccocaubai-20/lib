package com.example.lib.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.lib.model.Borrow;
import com.example.lib.service.BorrowService;

@RestController
@RequestMapping("/api/borrows")
public class BorrowController {
    private final BorrowService service;
    public BorrowController(BorrowService service) { this.service = service; }

    @GetMapping public List<Borrow> getAll() { return service.getAll(); }
    @PostMapping public Borrow borrow(@RequestBody Borrow b) { return service.save(b); }
}