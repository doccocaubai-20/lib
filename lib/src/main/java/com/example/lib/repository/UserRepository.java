package com.example.lib.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.lib.model.User;

public interface UserRepository extends JpaRepository<User,Long>{
    User findByUsername(String username);
}
