package com.example.lib.service;

import com.example.lib.model.Role;
import com.example.lib.model.User;
import com.example.lib.repository.RoleDAO;
import com.example.lib.repository.UserDAO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService {
    
    // Đổi tên biến cho nhất quán (Repo -> Dao)
    private final UserDAO userDao;
    private final RoleDAO roleDao;
    private final PasswordEncoder passwordEncoder;

    // Sửa hàm khởi tạo để nhận DAO
    public UserService(UserDAO userDao, RoleDAO roleDao, PasswordEncoder passwordEncoder) {
        this.userDao = userDao;
        this.roleDao = roleDao;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> findAllUsers() {
        // Gọi hàm của DAO mới
        return userDao.findAllByRoles_NameNot("ROLE_ADMIN");
    }
    
    public Optional<User> findUserById(Long id) {
        // Gọi hàm của DAO mới
        return userDao.findById(id);
    }

    public void updateUser(User userForm) {
        // Dùng userDao
        User existingUser = userDao.findById(userForm.getId()).orElse(null);
        if (existingUser != null) {
            existingUser.setFullName(userForm.getFullName());
            existingUser.setRoles(userForm.getRoles()); // Cập nhật roles
            
            // === SỬA LỖI 1 ===
            // Dùng hàm update() của DAO, không dùng save()
            userDao.update(existingUser);
        }
    }
    
    public void deleteUser(Long id) {
        // Gọi hàm của DAO mới (chúng ta đặt tên là delete)
        userDao.delete(id);
    }
    
    public User register(User user) {
        // Mã hóa mật khẩu
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // === SỬA LỖI 2 ===
        // Xử lý Optional trả về từ RoleDao
        Role userRole = roleDao.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Lỗi: Không tìm thấy ROLE_USER trong CSDL."));
        
        user.setRoles(Set.of(userRole));

        // Dùng hàm save() của DAO
        return userDao.save(user);
    }
}