package com.example.lib.service;

import com.example.lib.model.Role;
import com.example.lib.model.User;
import com.example.lib.repository.RoleRepository; // Tạo repository này
import com.example.lib.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
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

    public List<User> findAllUsers() {
    return userRepo.findAllByRoles_NameNot("ROLE_ADMIN");
    }
    public Optional<User> findUserById(Long id) {
    return userRepo.findById(id);
    }

    public void updateUser(User userForm) {
    User existingUser = userRepo.findById(userForm.getId()).orElse(null);
    if (existingUser != null) {
        existingUser.setFullName(userForm.getFullName());
        existingUser.setRoles(userForm.getRoles()); // Cập nhật roles
        // Không cập nhật username và password ở đây để đảm bảo an toàn
        userRepo.save(existingUser);
    }
    }
    public void deleteUser(Long id) {
          userRepo.deleteById(id);
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