package com.example.lib.repository;

import com.example.lib.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {

    // Spring Data JPA sẽ tự động tạo ra câu lệnh query
    // để tìm kiếm Role dựa trên trường 'name'
    Role findByName(String name);
}