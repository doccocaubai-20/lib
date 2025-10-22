package com.example.lib.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.lib.model.User;

public interface UserRepository extends JpaRepository<User,Long>{
    User findByUsername(String username);
    List<User> findAllByRoles_NameNot(String roleName);
}
