package com.example.lib.service;

import com.example.lib.model.Role;
import com.example.lib.model.User;
import com.example.lib.repository.RoleRepository; // Tạo repository này
import com.example.lib.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Set;

@Service
public class UserService {
    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepo, RoleRepository roleRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(User user) {
        // Mã hóa mật khẩu
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Gán vai trò mặc định là USER
        Role userRole = roleRepo.findByName("ROLE_USER");
        user.setRoles(Set.of(userRole));

        return userRepo.save(user);
    }

    // Phương thức login sẽ do Spring Security xử lý, bạn có thể xóa nó đi.
}