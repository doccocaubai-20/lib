package com.example.lib.controller;

import com.example.lib.model.Author;
import com.example.lib.repository.AuthorDAO;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/authors") // Tất cả URL sẽ bắt đầu bằng /admin/authors
public class AuthorController {

    private final AuthorDAO authorRepo;

    public AuthorController(AuthorDAO authorRepo) {
        this.authorRepo = authorRepo;
    }

    // Hiển thị danh sách tác giả
    @GetMapping
    public String listAuthors(Model model) {
        model.addAttribute("authors", authorRepo.findAll());
        return "admin/authors/list";
    }

    // Hiển thị form thêm mới
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("author", new Author());
        return "admin/authors/form";
    }

    // Hiển thị form chỉnh sửa
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        Author author = authorRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid author Id:" + id));
        model.addAttribute("author", author);
        return "admin/authors/form";
    }

    // Xử lý lưu (thêm mới hoặc cập nhật)
    @PostMapping("/save")
    public String saveAuthor(@ModelAttribute("author") Author author, RedirectAttributes redirectAttributes) {
        
        if (author.getId() == null) {
            authorRepo.save(author); 
        } else {
            authorRepo.update(author);
        }
        
        redirectAttributes.addFlashAttribute("successMessage", "Lưu tác giả thành công!");
        return "redirect:/admin/authors";
    }
    // Xử lý xóa
    @GetMapping("/delete/{id}")
    public String deleteAuthor(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            authorRepo.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa tác giả thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa tác giả này vì có sách đang liên kết.");
        }
        return "redirect:/admin/authors";
    }
}