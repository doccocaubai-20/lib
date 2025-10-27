package com.example.lib.controller;

import com.example.lib.model.Borrow;
import com.example.lib.repository.BookDAO;
import com.example.lib.repository.BorrowDAO;
import com.example.lib.repository.UserDAO;
import com.example.lib.service.BorrowService;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin") // Tất cả các URL trong controller này sẽ bắt đầu bằng /admin
public class AdminController {

    private final UserDAO userRepo;
    private final BookDAO bookRepo;
    private final BorrowService borrowService;
    private final BorrowDAO borrowRepo;
    public AdminController(UserDAO userRepo, BookDAO bookRepo,BorrowService borrowService,BorrowDAO borrowRepository) {
        this.userRepo = userRepo;
        this.bookRepo = bookRepo;
        this.borrowRepo = borrowRepository;
        this.borrowService = borrowService;
    }

    @GetMapping("/borrows")
    public String manageBorrows(Model model) {
        // SỬA: Gọi findAll() rồi tự sắp xếp bằng Stream
        List<Borrow> sortedBorrows = borrowRepo.findAll().stream()
                .sorted(Comparator.comparing(Borrow::getId).reversed())
                .collect(Collectors.toList());
        
        model.addAttribute("allBorrows", sortedBorrows);
        return "admin/borrows";
    }
// Xử lý chấp nhận đơn
@PostMapping("/borrows/approve/{id}")
public String approveBorrow(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    try {
        borrowService.approveBorrowRequest(id);
        redirectAttributes.addFlashAttribute("successMessage", "Duyệt đơn thành công!");
    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
    }
    return "redirect:/admin/borrows";
}

// Xử lý từ chối đơn
@PostMapping("/borrows/reject/{id}")
public String rejectBorrow(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    try {
        borrowService.rejectBorrowRequest(id);
        redirectAttributes.addFlashAttribute("successMessage", "Đã từ chối đơn mượn.");
    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
    }
    return "redirect:/admin/borrows";
}

    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        // Lấy các số liệu thống kê
        long totalUsers = userRepo.count();
        long totalBooks = bookRepo.count();

        // Gửi dữ liệu ra view
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalBooks", totalBooks);
        
        return "admin/dashboard"; // Trỏ đến file dashboard.html trong thư mục con 'admin'
    }

    // Bạn có thể thêm các trang quản lý khác ở đây, ví dụ:
    // @GetMapping("/users")
    // public String manageUsers(Model model) { ... }
}