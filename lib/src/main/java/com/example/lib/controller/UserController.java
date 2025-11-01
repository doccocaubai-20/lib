package com.example.lib.controller;

import com.example.lib.model.Role;
import com.example.lib.model.User;
import com.example.lib.repository.RoleDAO;
import com.example.lib.service.UserService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequestMapping("/admin/users")
public class UserController {

    private final UserService userService;
    private final RoleDAO roleDAO;

    public UserController(UserService userService, RoleDAO roleDAO) {
        this.userService = userService;
        this.roleDAO = roleDAO;
    }

    // Hiển thị danh sách người dùng
    @GetMapping
    public String listUsers(Model model) {
        model.addAttribute("users", userService.findAllUsers());
        return "admin/users/list";
    }

    // Hiển thị form chỉnh sửa người dùng
    @GetMapping("/edit/{id}")
    public String showEditUserForm(@PathVariable Long id, Model model) {
        User user = userService.findUserById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));
        model.addAttribute("user", user);
        model.addAttribute("allRoles", roleDAO.findAll()); // Gửi tất cả các role sang view
        return "admin/users/form";
    }
    @PostMapping("/update")
    public String saveUser(
            @RequestParam("id") Long id, // ID của user (từ trường input hidden)
            @RequestParam("fullName") String fullName, // Tên đầy đủ từ form
            // Nhận danh sách các ID vai trò (dạng String)
            @RequestParam(value = "roles", required = false) List<String> roleIds, 
            RedirectAttributes redirectAttributes) {

        // 1. Tải User gốc từ service
        User userToUpdate = userService.findUserById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));

        // 2. Cập nhật các trường đơn giản
        userToUpdate.setFullName(fullName);

        // 3. Xử lý Roles (Chuyển đổi List<String> ID thành Set<Role>)
        Set<Role> newRoles = new HashSet<>();
        if (roleIds != null) {
            for (String roleIdStr : roleIds) {
                // Chuyển đổi String ID thành Long
                Long roleId = Long.parseLong(roleIdStr); 
                
                // Tìm đối tượng Role từ CSDL
                Role role = roleDAO.findById(roleId) // Giả sử RoleDao có findById(Long)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid role Id:" + roleId));
                newRoles.add(role);
            }
        }
        userToUpdate.setRoles(newRoles); // Gán Set<Role> mới

        // 4. Gọi service để lưu (Service sẽ gọi DAO.update)
        // Hàm updateUser của bạn
        // sẽ nhận đối tượng userToUpdate đã được xây dựng hoàn chỉnh.
        userService.updateUser(userToUpdate);
        
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật người dùng thành công!");
        return "redirect:/admin/users"; // Chuyển hướng về trang danh sách
    }
   
    // Xử lý xóa người dùng
    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa người dùng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa người dùng này.");
        }
        return "redirect:/admin/users";
    }
}