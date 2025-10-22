package com.example.lib.controller;

import com.example.lib.model.User;
import com.example.lib.repository.RoleRepository;
import com.example.lib.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/users")
public class UserController {

    private final UserService userService;
    private final RoleRepository roleRepository;

    public UserController(UserService userService, RoleRepository roleRepository) {
        this.userService = userService;
        this.roleRepository = roleRepository;
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
        model.addAttribute("allRoles", roleRepository.findAll()); // Gửi tất cả các role sang view
        return "admin/users/form";
    }

    // Xử lý cập nhật người dùng
    @PostMapping("/update")
    public String updateUser(@ModelAttribute("user") User user, RedirectAttributes redirectAttributes) {
        userService.updateUser(user);
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật người dùng thành công!");
        return "redirect:/admin/users";
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