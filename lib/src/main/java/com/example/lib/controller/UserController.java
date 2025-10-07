package com.example.lib.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.lib.model.User;
import com.example.lib.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService service;
    public UserController(UserService service) { this.service = service; }

    @PostMapping("/register") public User register(@RequestBody User u) { return service.register(u); }
    @PostMapping("/login") public User login(@RequestParam String username, @RequestParam String password) {
        return service.login(username, password);
    }
}