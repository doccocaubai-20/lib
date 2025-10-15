package com.example.lib.controller;

import com.example.lib.model.*;
import com.example.lib.repository.*;
import com.example.lib.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class WebController {

    private final BookRepository bookRepo;
    private final UserRepository userRepo;
    private final AuthorRepository authorRepo;
    private final CategoryRepository categoryRepo;
    private final ReviewRepository reviewRepo;
    private final UserService userService;

    // Sử dụng constructor injection để Spring tự động cung cấp các bean cần thiết
    public WebController(BookRepository bookRepo, UserRepository userRepo,
                         AuthorRepository authorRepo, CategoryRepository categoryRepo,
                         ReviewRepository reviewRepo, UserService userService) {
        this.bookRepo = bookRepo;
        this.userRepo = userRepo;
        this.authorRepo = authorRepo;
        this.categoryRepo = categoryRepo;
        this.reviewRepo = reviewRepo;
        this.userService = userService;
    }

    // ==================== Trang chính và các trang chung ====================

    @GetMapping("/")
    public String root() {
        return "redirect:/index";
    }

    @GetMapping("/index")
    public String indexPage() {
        return "index";
    }

    // ==================== Quản lý Sách (Book Management) ====================

    @GetMapping("/books")
    public String listBooks(Model model) {
        model.addAttribute("books", bookRepo.findAll());
        return "books";
    }

    @GetMapping("/books/detail/{id}")
    public String viewBookDetail(@PathVariable Long id, Model model) {
        return bookRepo.findById(id)
                .map(book -> {
                    model.addAttribute("book", book);
                    // Lấy tất cả review của sách này và gửi sang view
                    model.addAttribute("reviews", reviewRepo.findByBookId(id));
                    // Gửi một đối tượng review rỗng để form có thể binding
                    model.addAttribute("newReview", new Review());
                    return "book_detail";
                })
                .orElse("redirect:/books");
    }

    @GetMapping("/books/view/{id}")
    public String viewBookPdf(@PathVariable Long id, Model model) {
        return bookRepo.findById(id)
                .map(book -> {
                    model.addAttribute("book", book);
                    return "book_view";
                })
                .orElse("redirect:/books");
    }

    // Hiển thị form thêm sách (chỉ cho ADMIN)
    @GetMapping("/books/add")
    public String showAddBookForm(Model model) {
        model.addAttribute("book", new Book()); // Gửi một đối tượng Book rỗng
        model.addAttribute("authors", authorRepo.findAll()); // Gửi danh sách tác giả
        model.addAttribute("categories", categoryRepo.findAll()); // Gửi danh sách thể loại
        return "addbook";
    }

    // Xử lý việc thêm sách mới
    @PostMapping("/books/add")
    public String addBook(@ModelAttribute Book book, RedirectAttributes redirectAttributes) {
        bookRepo.save(book);
        redirectAttributes.addFlashAttribute("successMessage", "Thêm sách thành công!");
        return "redirect:/books";
    }

    // Hiển thị form sửa thông tin sách (chỉ cho ADMIN)
    @GetMapping("/books/edit/{id}")
    public String showEditBookForm(@PathVariable Long id, Model model) {
        return bookRepo.findById(id).map(book -> {
            model.addAttribute("book", book);
            model.addAttribute("authors", authorRepo.findAll());
            model.addAttribute("categories", categoryRepo.findAll());
            return "book_edit";
        }).orElse("redirect:/books");
    }

    // Xử lý việc cập nhật sách
    @PostMapping("/books/update")
    public String updateBook(@ModelAttribute Book book, RedirectAttributes redirectAttributes) {
        bookRepo.save(book);
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật sách thành công!");
        return "redirect:/books";
    }

    // Xóa sách (chỉ cho ADMIN)
    @GetMapping("/books/delete/{id}")
    public String deleteBook(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (bookRepo.existsById(id)) {
            bookRepo.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa sách thành công!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy sách để xóa.");
        }
        return "redirect:/books";
    }

    // ==================== Quản lý Đánh giá (Review Management) ====================
    
    @PostMapping("/reviews/add")
    public String addReview(@ModelAttribute("newReview") Review review,
                            @RequestParam("bookId") Long bookId,
                            Authentication authentication, // Lấy thông tin người dùng đang đăng nhập
                            RedirectAttributes redirectAttributes) {

        // Lấy username từ Principal
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();

        // Tìm user và book từ database
        User user = userRepo.findByUsername(username);
        Book book = bookRepo.findById(bookId).orElse(null);

        if (user != null && book != null) {
            review.setUser(user);
            review.setBook(book);
            reviewRepo.save(review);
            redirectAttributes.addFlashAttribute("successMessage", "Cảm ơn bạn đã gửi đánh giá!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Đã có lỗi xảy ra.");
        }

        return "redirect:/books/detail/" + bookId;
    }


    // ==================== Xác thực (Authentication) ====================
    // Spring Security sẽ xử lý logic, Controller chỉ cần trả về view

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String handleRegistration(@RequestParam String username,
                                     @RequestParam String password,
                                     RedirectAttributes redirectAttributes) {

        if (userRepo.findByUsername(username) != null) {
            redirectAttributes.addFlashAttribute("error", "Tên đăng nhập đã tồn tại.");
            return "redirect:/register";
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(password); // Mật khẩu sẽ được mã hóa trong service
        userService.register(newUser);

        redirectAttributes.addFlashAttribute("success", "Đăng ký thành công! Vui lòng đăng nhập.");
        return "redirect:/login";
    }
}