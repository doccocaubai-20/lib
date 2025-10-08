package com.example.lib.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.lib.model.Book;
import com.example.lib.model.User;
import com.example.lib.repository.BookRepository;
import com.example.lib.repository.UserRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class WebController {

    private final BookRepository bookRepo;
    private final UserRepository userRepo;

    public WebController(BookRepository bookRepo, UserRepository userRepo) {
        this.bookRepo = bookRepo;
        this.userRepo = userRepo;
    }

    // ==================== Root & Index ====================
    @GetMapping("/")
    public String root(HttpSession session) {
        return (session.getAttribute("user") != null) ? "redirect:/index" : "redirect:/login";
    }

    @GetMapping("/index")
    public String index(HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }
        return "index";
    }

    // ==================== Book Management ====================
    @GetMapping("/books")
    public String books(Model model) {
        model.addAttribute("books", bookRepo.findAll());
        return "books";
    } 

    @GetMapping("/book_detail/{id}")
    public String viewBook(@PathVariable Long id, Model model) {
        return bookRepo.findById(id)
                .map(book -> {
                    model.addAttribute("book", book);
                    return "book_detail";
                })
                .orElse("redirect:/books");
    }

    @GetMapping("/books/view/{id}")
    public String viewBookPdf(@PathVariable Long id, Model model) {
        return bookRepo.findById(id)
                .filter(book -> book.getPdfPath() != null)
                .map(book -> {
                    model.addAttribute("book", book);
                    model.addAttribute("pdfUrl", book.getPdfPath());
                    return "book_view";
                })
                .orElse("redirect:/books");
    }

    @GetMapping("/books/add")
    public String showAddBookForm(HttpSession session) {
        User user = (User)session.getAttribute("user");
        // kiểm tra nếu chưa đăng nhập hoặc không phải admin
        if (user == null || !"ADMIN".equals(user.getRole())) {
        return "redirect:/index";
        }
        return "addbook"; // admin mới được thêm sách
    }   

    @PostMapping("/books")
    public String addBook(@ModelAttribute Book book) {
        bookRepo.save(book);
        return "redirect:/books";
    }

    @GetMapping("/books/edit/{id}")
    public String editBook(@PathVariable Long id, Model model){
        return bookRepo.findById(id).map(book -> {
            model.addAttribute("book", book);
            return "book_edit";
        }).orElse("redirect:/books");
    }

    @PostMapping("/books/update")
    public String updateBook(@ModelAttribute Book book) {
        bookRepo.save(book);
        return "redirect:/books";
    }

    @GetMapping("/books/delete/{id}")
    public String deleteBook(@PathVariable Long id) {
        bookRepo.deleteById(id);
        return "redirect:/books";
    }

    // ==================== Authentication ====================
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {

        var user = userRepo.findByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            session.setAttribute("user", user);
            return "redirect:/index";
        }

        model.addAttribute("error", "Sai tài khoản hoặc mật khẩu");
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           Model model) {

        if (userRepo.findByUsername(username) != null) {
            model.addAttribute("error", "Tên đăng nhập đã tồn tại");
            return "register";
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole("USER");
        userRepo.save(user);

        return "redirect:/login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
