package com.example.lib.model;
import lombok.Data;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Data
public class User {
    private Long id;
    private String username;
    private String password;
    private String fullName;
    private boolean isActive = true;
    private Instant createdAt = Instant.now();
    private Set<Role> roles = new HashSet<>();
}