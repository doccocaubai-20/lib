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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


@Controller
public class WebController {

    private final BookRepository bookRepo;
    private final UserRepository userRepo;
    private final AuthorRepository authorRepo;
    private final CategoryRepository categoryRepo;
    private final ReviewRepository reviewRepo;
    private final UserService userService;
    private final StorageService storageService;
    private final BorrowService borrowService;
    private final ReviewService reviewService;
    // Sử dụng constructor injection để Spring tự động cung cấp các bean cần thiết
    public WebController(BookRepository bookRepo, UserRepository userRepo,
                         AuthorRepository authorRepo, CategoryRepository categoryRepo,
                         ReviewRepository reviewRepo, UserService userService,StorageService storageService,BorrowService borrowService,ReviewService reviewService) {
        this.bookRepo = bookRepo;
        this.userRepo = userRepo;
        this.authorRepo = authorRepo;
        this.categoryRepo = categoryRepo;
        this.reviewRepo = reviewRepo;
        this.userService = userService;
        this.storageService = storageService; 
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

        List<Book> latestBooks = bookRepo.findAll(PageRequest.of(0, 4, Sort.by("id").descending())).getContent();
        model.addAttribute("latestBooks", latestBooks);

        List<Book> featuredBooks = bookRepo.findAll(PageRequest.of(0, 4)).getContent();
        model.addAttribute("featuredBooks", featuredBooks);

        // LOGIC GỢI Ý SÁCH (PHẦN THÊM MỚI)
        if (authentication != null && authentication.isAuthenticated()) {
            User currentUser = userRepo.findByUsername(authentication.getName());
            // Gọi phương thức mới từ BorrowService
            List<Book> recommendedBooks = borrowService.recommendBooksForUser(currentUser);
            model.addAttribute("recommendedBooks", recommendedBooks);
        }

        return "index";
    }

    // ==================== Quản lý Sách (Book Management) ====================

    // Thay thế hoàn toàn phương thức listBooks cũ
    @GetMapping("/books")
    public String listBooks(Model model,
                            @RequestParam(name = "page", defaultValue = "1") int page,
                            @RequestParam(name = "size", defaultValue = "8") int size,
                            @RequestParam(name = "keyword", required = false) String keyword,
                            @RequestParam(name = "authorIds", required = false) List<Long> authorIds,
                            @RequestParam(name = "categoryIds", required = false) List<Long> categoryIds,
                            @RequestParam(name = "minRating", required = false) Double minRating) {

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("id").descending());
        
        // Tạo specification từ các tham số lọc
        Specification<Book> spec = BookSpecification.filterBy(keyword, authorIds, categoryIds, minRating);

        // Lấy dữ liệu đã lọc và phân trang
        Page<Book> bookPage = bookRepo.findAll(spec, pageable);

        model.addAttribute("bookPage", bookPage);

        // Gửi lại các giá trị lọc để hiển thị trên form
        model.addAttribute("keyword", keyword);
        model.addAttribute("authorIds", authorIds);
        model.addAttribute("categoryIds", categoryIds);
        model.addAttribute("minRating", minRating);

        // ... code tạo pageNumbers như cũ ...
        int totalPages = bookPage.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                    .boxed()
                    .collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }

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
                // Trả về trang đọc sách mới thay vì trang cũ
                return "book_read"; 
            })
            .orElse("redirect:/books");
}

    @PostMapping("/books/borrow/{id}")
public String handleBorrowRequest(@PathVariable("id") Long bookId,
                                  Authentication authentication,
                                  RedirectAttributes redirectAttributes) {
    // ... lấy currentUser như cũ ...
    UserDetails userDetails = (UserDetails) authentication.getPrincipal();
    User currentUser = userRepo.findByUsername(userDetails.getUsername());

    try {
        // Gọi service để tạo yêu cầu
        borrowService.createBorrowRequest(bookId, currentUser);
        redirectAttributes.addFlashAttribute("successMessage", "Gửi yêu cầu mượn sách thành công! Vui lòng chờ Admin duyệt.");
    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("errorMessage", "Yêu cầu thất bại: " + e.getMessage());
    }
    return "redirect:/books/detail/" + bookId;
}    

    // == THÊM PHƯƠNG THỨC MỚI ĐỂ HIỂN THỊ TRANG "SÁCH CỦA TÔI" ==
    @GetMapping("/my-borrows")
    public String showMyBorrows(Model model, Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User currentUser = userRepo.findByUsername(userDetails.getUsername());

        List<Borrow> myBorrows = borrowService.findBorrowsByUser(currentUser);
        model.addAttribute("myBorrows", myBorrows);

        return "my_borrows"; // Trỏ đến file my_borrows.html
    }



    // ==================== Quản lý Đánh giá (Review Management) ====================
    
    @PostMapping("/reviews/add")
    public String addReview(@ModelAttribute("newReview") Review review,
                            @RequestParam("bookId") Long bookId,
                            Authentication authentication,
                            RedirectAttributes redirectAttributes) {

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepo.findByUsername(userDetails.getUsername());
        Book book = bookRepo.findById(bookId).orElse(null);

        if (user != null && book != null) {
            // Gọi service mới để xử lý tất cả logic
            reviewService.saveReview(review, user, book);
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

    // ======

    @ModelAttribute("allCategories")
    public List<Category> getAllCategories() {
        // Lấy tất cả thể loại và gửi ra mọi view
        return categoryRepo.findAll();
    }

    @ModelAttribute("allAuthors")
    public List<Author> getAllAuthors() {
        // Lấy tất cả tác giả và gửi ra mọi view
        return authorRepo.findAll();
    }

    // Thêm một phương thức mới để xử lý tìm kiếm
    @GetMapping("/search")
public String searchBooks(@RequestParam("query") String query,
                          @RequestParam(name = "page", required = false, defaultValue = "1") int page,
                          @RequestParam(name = "size", required = false, defaultValue = "8") int size,
                          Model model) {

    // 1. Tìm kiếm tất cả sách khớp với query
    List<Book> fullResults = bookRepo.findByTitleContainingIgnoreCase(query);

    // 2. Tạo đối tượng Pageable
    Pageable pageable = PageRequest.of(page - 1, size);

    // 3. Chuyển List thành Page
    int start = (int) pageable.getOffset();
    int end = Math.min((start + pageable.getPageSize()), fullResults.size());

    List<Book> pageContent = (start > fullResults.size()) 
                             ? Collections.emptyList() 
                             : fullResults.subList(start, end);

    Page<Book> bookPage = new PageImpl<>(pageContent, pageable, fullResults.size());

    // 4. Gửi dữ liệu ra view (tương tự listBooks)
    model.addAttribute("bookPage", bookPage);
    int totalPages = bookPage.getTotalPages();
    if (totalPages > 0) {
        List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                .boxed()
                .collect(Collectors.toList());
        model.addAttribute("pageNumbers", pageNumbers);
    }

    // Thêm các thuộc tính để hiển thị tiêu đề tìm kiếm
    model.addAttribute("isSearchResult", true);
    model.addAttribute("query", query);
    model.addAttribute("pageTitle", "Kết quả tìm kiếm cho: '" + query + "'");

    return "books"; // Tái sử dụng template books.html
}

    
    @GetMapping("/profile")
    public String userProfile(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login"; // Nếu chưa đăng nhập, chuyển về trang login
        }

        // Lấy username một cách an toàn
        String username = authentication.getName();
        User currentUser = userRepo.findByUsername(username);

        // Gửi thông tin người dùng sang view
        model.addAttribute("user", currentUser);

        return "profile";
    }

}