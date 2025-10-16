package com.example.lib.controller;

import com.example.lib.model.*;
import com.example.lib.repository.*;
import com.example.lib.service.UserService;
import com.example.lib.storage.StorageService;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.util.List;
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

    // Sử dụng constructor injection để Spring tự động cung cấp các bean cần thiết
    public WebController(BookRepository bookRepo, UserRepository userRepo,
                         AuthorRepository authorRepo, CategoryRepository categoryRepo,
                         ReviewRepository reviewRepo, UserService userService,StorageService storageService) {
        this.bookRepo = bookRepo;
        this.userRepo = userRepo;
        this.authorRepo = authorRepo;
        this.categoryRepo = categoryRepo;
        this.reviewRepo = reviewRepo;
        this.userService = userService;
        this.storageService = storageService; 
    }

    // ==================== Trang chính và các trang chung ====================

    @GetMapping("/")
    public String root() {
        return "redirect:/index";
    }

    @GetMapping("/index")
    public String indexPage(Model model) {

        // Find 4 new Book
        List<Book> latestBooks = bookRepo.findAll(PageRequest.of(0, 4,Sort.by("id").descending())).getContent();
        model.addAttribute("latestBooks", latestBooks);

        // Get 4 feature Book
        List<Book> featuredBooks = bookRepo.findAll(PageRequest.of(0, 4)).getContent();
        model.addAttribute("featuredBooks", featuredBooks);
        return "index";
    }

    // ==================== Quản lý Sách (Book Management) ====================

    @GetMapping("/books")
public String listBooks(Model model,
                        @RequestParam(name = "page", required = false, defaultValue = "1") int page,
                        @RequestParam(name = "size", required = false, defaultValue = "8") int size) {
    // Pageable đánh số trang từ 0, nên chúng ta trừ đi 1
    Pageable pageable = PageRequest.of(page - 1, size, Sort.by("id").descending());
    Page<Book> bookPage = bookRepo.findAll(pageable);

    model.addAttribute("bookPage", bookPage);

    // Tạo danh sách các số trang để hiển thị trên thanh phân trang
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
    public String addBook(@ModelAttribute Book book,
                        @RequestParam("imageFile") MultipartFile imageFile,
                        @RequestParam("pdfFile") MultipartFile pdfFile,
                        RedirectAttributes redirectAttributes) {

        if (!imageFile.isEmpty()) {
            String imageName = storageService.store(imageFile);
            book.setImage(imageName);
        }
        if (!pdfFile.isEmpty()) {
            String pdfName = storageService.store(pdfFile);
            book.setPdfPath(pdfName);
            int pageCount = storageService.getPdfPageCount(pdfFile);
            book.setPageCount(pageCount);
        }
        
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
    // Thay thế hoàn toàn phương thức updateBook cũ bằng phương thức này

@PostMapping("/books/update")
public String updateBook(@ModelAttribute Book bookFromForm,
                         @RequestParam("imageFile") MultipartFile imageFile,
                         @RequestParam("pdfFile") MultipartFile pdfFile,
                         RedirectAttributes redirectAttributes) {

    // 1. Tìm cuốn sách gốc trong database
    Book existingBook = bookRepo.findById(bookFromForm.getId()).orElse(null);
    if (existingBook == null) {
        redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy sách để cập nhật.");
        return "redirect:/books";
    }

    // 2. Cập nhật các thông tin từ form (title, author, category...)
    existingBook.setTitle(bookFromForm.getTitle());
    existingBook.setAuthor(bookFromForm.getAuthor());
    existingBook.setCategory(bookFromForm.getCategory());
    existingBook.setPublishDate(bookFromForm.getPublishDate());
    existingBook.setPageCount(bookFromForm.getPageCount());
    existingBook.setQuantity(bookFromForm.getQuantity());
    existingBook.setDescription(bookFromForm.getDescription());
    existingBook.setLanguage(bookFromForm.getLanguage());
    
    // 3. Xử lý upload ảnh bìa MỚI (nếu có)
    if (imageFile != null && !imageFile.isEmpty()) {
        String imageName = storageService.store(imageFile);
        existingBook.setImage(imageName);
    }
    // Nếu không có ảnh mới, chúng ta không làm gì cả, giữ nguyên ảnh cũ của `existingBook`

    // 4. Xử lý upload file PDF MỚI (nếu có)
    if (pdfFile != null && !pdfFile.isEmpty()) {
        String pdfName = storageService.store(pdfFile);
        existingBook.setPdfPath(pdfName);
        int pageCount = storageService.getPdfPageCount(pdfFile);
        existingBook.setPageCount(pageCount);
    }
    // Nếu không có file PDF mới, chúng ta không làm gì cả, giữ nguyên file PDF cũ

    // 5. Lưu lại cuốn sách đã được cập nhật
    bookRepo.save(existingBook);
    redirectAttributes.addFlashAttribute("successMessage", "Cập nhật sách thành công!");
    return "redirect:/books/detail/" + existingBook.getId();
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
    public String searchBooks(@RequestParam("query") String query, Model model) {
        // Đây là logic tìm kiếm đơn giản, bạn có thể cải tiến sau
        List<Book> results = bookRepo.findByTitleContainingIgnoreCase(query);
        model.addAttribute("books", results);
        model.addAttribute("pageTitle", "Kết quả tìm kiếm cho: '" + query + "'");
        model.addAttribute("isSearchResult", true);
        model.addAttribute("query", query);
        return "books"; // Tái sử dụng trang books.html để hiển thị kết quả
    }
}