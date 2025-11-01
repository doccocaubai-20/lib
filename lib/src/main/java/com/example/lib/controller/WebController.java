package com.example.lib.controller;

import com.example.lib.model.*;
import com.example.lib.repository.*;
import com.example.lib.service.BorrowService;
import com.example.lib.service.ReviewService;
import com.example.lib.service.UserService;
import com.example.lib.storage.StorageService;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Comparator; 
import java.util.List;
import java.util.Optional; 
import java.util.stream.Collectors;

@Controller
public class WebController {

    private final BookDAO bookRepo;
    private final UserDAO userRepo;
    private final AuthorDAO authorRepo;
    private final CategoryDAO categoryRepo;
    private final ReviewDAO reviewRepo;
    private final UserService userService;
    private final BorrowService borrowService;
    private final ReviewService reviewService;
    
    public WebController(BookDAO bookRepo, UserDAO userRepo,
                         AuthorDAO authorRepo, CategoryDAO categoryRepo,
                         ReviewDAO reviewRepo, UserService userService,
                         StorageService storageService,BorrowService borrowService,
                         ReviewService reviewService) {
        this.bookRepo = bookRepo;
        this.userRepo = userRepo;
        this.authorRepo = authorRepo;
        this.categoryRepo = categoryRepo;
        this.reviewRepo = reviewRepo;
        this.userService = userService;
        this.borrowService = borrowService;
        this.reviewService = reviewService;
    }

    // ==================== Trang chính và các trang chung ====================

    @GetMapping("/")
    public String root() {
        return "redirect:/index";
    }

    @GetMapping("/index")
    public String indexPage(Model model, Authentication authentication) {

        List<Book> allBooks = bookRepo.findAll();

        List<Book> latestBooks = allBooks.stream()
                .sorted(Comparator.comparing(Book::getId).reversed())
                .limit(4)
                .collect(Collectors.toList());
        model.addAttribute("latestBooks", latestBooks);

        List<Book> featuredBooks = allBooks.stream()
                .limit(4)
                .collect(Collectors.toList());
        model.addAttribute("featuredBooks", featuredBooks);

        return "index";
    }

    // ==================== Quản lý Sách (Book Management) ====================


    @GetMapping("/books")
    public String listBooks(Model model,
                            @RequestParam(name = "keyword", required = false) String keyword,
                            @RequestParam(name = "authorIds", required = false) List<Long> authorIds,
                            @RequestParam(name = "categoryIds", required = false) List<Long> categoryIds,
                            @RequestParam(name = "minRating", required = false) Double minRating) {


        List<Book> bookList = bookRepo.findWithFilters(keyword, authorIds, categoryIds, minRating);

        model.addAttribute("bookList", bookList); 

        model.addAttribute("keyword", keyword);
        model.addAttribute("authorIds", authorIds);
        model.addAttribute("categoryIds", categoryIds);
        model.addAttribute("minRating", minRating);

        return "books";
    }
    @GetMapping("/books/detail/{id}")
    public String viewBookDetail(@PathVariable Long id, Model model) {
        return bookRepo.findById(id)
                .map(book -> {
                    model.addAttribute("book", book);
                    model.addAttribute("reviews", reviewRepo.findByBookId(id));
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
                    return "book_read"; 
                })
                .orElse("redirect:/books");
    }

    @PostMapping("/books/borrow/{id}")
    public String handleBorrowRequest(@PathVariable("id") Long bookId,
                                      Authentication authentication,
                                      RedirectAttributes redirectAttributes) {
        
        // BƯỚC 1: Kiểm tra xem người dùng đã đăng nhập chưa
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof UserDetails)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn cần đăng nhập để mượn sách.");
            return "redirect:/login"; // Chuyển hướng đến trang đăng nhập
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User currentUser = userRepo.findByUsername(userDetails.getUsername())
                                   .orElse(null); // Giả sử userRepo là UserDao của bạn

        // BƯỚC 2: Kiểm tra xem user có tồn tại trong CSDL không
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: Không tìm thấy người dùng của bạn.");
            return "redirect:/login";
        }

        // BƯỚC 3: Bây giờ khối try...catch của bạn đã an toàn
        try {
            borrowService.createBorrowRequest(bookId, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Gửi yêu cầu mượn sách thành công! Vui lòng chờ Admin duyệt.");
        } catch (Exception e) {
            // Bắt các lỗi nghiệp vụ (ví dụ: hết sách, đã mượn)
            redirectAttributes.addFlashAttribute("errorMessage", "Yêu cầu thất bại: " + e.getMessage());
        }
        return "redirect:/books/detail/" + bookId;
    }
    @GetMapping("/my-borrows")
    public String showMyBorrows(Model model, Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User currentUser = userRepo.findByUsername(userDetails.getUsername())
                                   .orElse(null);
        
        if (currentUser == null) {
            return "redirect:/login";
        }

        List<Borrow> myBorrows = borrowService.findBorrowsByUser(currentUser);
        model.addAttribute("myBorrows", myBorrows);

        return "my_borrows";
    }

    // ==================== Quản lý Đánh giá (Review Management) ====================
    
    @PostMapping("/reviews/add")
    public String addReview(@ModelAttribute("newReview") Review review,
                            @RequestParam("bookId") Long bookId,
                            Authentication authentication,
                            RedirectAttributes redirectAttributes) {

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepo.findByUsername(userDetails.getUsername()).orElse(null);
        Book book = bookRepo.findById(bookId).orElse(null);

        if (user != null && book != null) {
            reviewService.saveReview(review, user, book);
            redirectAttributes.addFlashAttribute("successMessage", "Cảm ơn bạn đã gửi đánh giá!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Đã có lỗi xảy ra.");
        }

        return "redirect:/books/detail/" + bookId;
    }


    // ==================== Xác thực (Authentication) ====================

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

        if (userRepo.findByUsername(username).isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Tên đăng nhập đã tồn tại.");
            return "redirect:/register";
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(password);
        userService.register(newUser);

        redirectAttributes.addFlashAttribute("success", "Đăng ký thành công! Vui lòng đăng nhập.");
        return "redirect:/login";
    }

    @ModelAttribute("allCategories")
    public List<Category> getAllCategories() {
        return categoryRepo.findAll();
    }

    @ModelAttribute("allAuthors")
    public List<Author> getAllAuthors() {
        return authorRepo.findAll();
    }
    @GetMapping("/search")
    public String searchBooks(@RequestParam("query") String query,
                              Model model) { 

        List<Book> bookList = bookRepo.findByTitleContainingIgnoreCase(query);

     model.addAttribute("bookList", bookList); // <-- Tên mới là "bookList"

        model.addAttribute("isSearchResult", true);
        model.addAttribute("query", query);
        model.addAttribute("pageTitle", "Kết quả tìm kiếm cho: '" + query + "'");

        return "books"; 
    }
    
    @GetMapping("/profile")
    public String userProfile(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String username = authentication.getName();
        User currentUser = userRepo.findByUsername(username).orElse(null);

        model.addAttribute("user", currentUser);

        return "profile";
    }
}