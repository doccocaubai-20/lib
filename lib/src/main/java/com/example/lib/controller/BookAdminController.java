package com.example.lib.controller;

import com.example.lib.model.Book;
import com.example.lib.repository.AuthorRepository;
import com.example.lib.repository.BookRepository;
import com.example.lib.repository.CategoryRepository;
import com.example.lib.storage.StorageService;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/books") // Tiền tố chung cho tất cả các URL trong controller này
public class BookAdminController {

    private final BookRepository bookRepo;
    private final AuthorRepository authorRepo;
    private final CategoryRepository categoryRepo;
    private final StorageService storageService;

    public BookAdminController(BookRepository bookRepo, AuthorRepository authorRepo, CategoryRepository categoryRepo, StorageService storageService) {
        this.bookRepo = bookRepo;
        this.authorRepo = authorRepo;
        this.categoryRepo = categoryRepo;
        this.storageService = storageService;
    }

    // Hiển thị danh sách sách cho Admin
    @GetMapping
    public String listBooks(Model model) {
        model.addAttribute("books", bookRepo.findAll(Sort.by("id").descending()));
        return "admin/books/list";
    }

    // Hiển thị form thêm sách
    @GetMapping("/add")
    public String showAddBookForm(Model model) {
        model.addAttribute("book", new Book());
        model.addAttribute("authors", authorRepo.findAll());
        model.addAttribute("categories", categoryRepo.findAll());
        return "admin/books/form";
    }

    // Hiển thị form sửa sách
    @GetMapping("/edit/{id}")
    public String showEditBookForm(@PathVariable Long id, Model model) {
        Book book = bookRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid book Id:" + id));
        model.addAttribute("book", book);
        model.addAttribute("authors", authorRepo.findAll());
        model.addAttribute("categories", categoryRepo.findAll());
        return "admin/books/form";
    }

    // Xử lý lưu sách (cả thêm mới và cập nhật)
    @PostMapping("/save")
    public String saveBook(@ModelAttribute("book") Book book,
                           @RequestParam("imageFile") MultipartFile imageFile,
                           @RequestParam("pdfFile") MultipartFile pdfFile,
                           RedirectAttributes redirectAttributes) {

        // Xử lý upload ảnh bìa MỚI (nếu có)
        if (!imageFile.isEmpty()) {
            storageService.delete(book.getImage()); // Xóa ảnh cũ nếu có
            String imageName = storageService.store(imageFile);
            book.setImage(imageName);
        }

        // Xử lý upload file PDF MỚI (nếu có)
        if (!pdfFile.isEmpty()) {
            storageService.delete(book.getPdfPath()); // Xóa file PDF cũ nếu có
            String pdfName = storageService.store(pdfFile);
            book.setPdfPath(pdfName);
            int pageCount = storageService.getPdfPageCount(pdfFile);
            book.setPageCount(pageCount);
        }

        bookRepo.save(book);
        redirectAttributes.addFlashAttribute("successMessage", "Lưu sách thành công!");
        return "redirect:/admin/books";
    }

    // Xử lý xóa sách
    @GetMapping("/delete/{id}")
    public String deleteBook(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            // Cân nhắc xóa cả file ảnh và pdf liên quan
            bookRepo.findById(id).ifPresent(book -> {
                storageService.delete(book.getImage());
                storageService.delete(book.getPdfPath());
            });
            bookRepo.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa sách thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa sách này.");
        }
        return "redirect:/admin/books";
    }
}