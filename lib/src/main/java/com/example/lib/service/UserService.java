package com.example.lib.service;

import org.springframework.stereotype.Service;

import com.example.lib.model.User;
import com.example.lib.repository.UserRepository;

@Service
public class UserService {
    private final UserRepository repo;
    public UserService(UserRepository repo){
        this.repo = repo;
    }
    public User register(User u) { return repo.save(u); }

    public User login(String username, String password) {
        User u = repo.findByUsername(username);
        if (u != null && u.getPassword().equals(password)) return u;
        return null;
    }
}
