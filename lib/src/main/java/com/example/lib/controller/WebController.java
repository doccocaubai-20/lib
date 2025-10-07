package com.example.lib.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.lib.model.Book;
import com.example.lib.model.User;
import com.example.lib.repository.BookRepository;
import com.example.lib.repository.UserRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.ui.Model;


@Controller
public class WebController {
    private final BookRepository bookRepo;
    private final UserRepository userRepo;

    public WebController(BookRepository bookRepo, UserRepository userRepo) {
        this.bookRepo = bookRepo;
        this.userRepo = userRepo;
    }

    @GetMapping("/")
public String root(HttpSession session) {
    if (session.getAttribute("user") != null) {
        return "redirect:/index"; // nếu đã login
    } else {
        return "redirect:/login"; // nếu chưa login
    }
}
    @GetMapping("/index")
    public String index(HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login"; // chưa login thì redirect
        }
        return "index"; // tên file Thymeleaf: index.html
    }       
    @GetMapping("/books")
    public String books(Model model) {
        model.addAttribute("books", bookRepo.findAll());
        return "books";
    }

    @GetMapping("/books/new")
    public String showAddForm(Model model){
        model.addAttribute("book", new Book()); // Tao book moi, dien form
        return "addbook"; // .html
    }
   @PostMapping("/books")
public String addBook(@ModelAttribute Book book) {
    bookRepo.save(book);
    return "redirect:/books"; 
}
    @GetMapping("/login") public String loginPage() { return "login"; }

    @PostMapping("/login")
public String login(@RequestParam String username,
                    @RequestParam String password,
                    HttpSession session,
                    Model model) {

    var u = userRepo.findByUsername(username);
    if (u != null && u.getPassword().equals(password)) {
        session.setAttribute("user", u); // Lưu user vào session
        return "redirect:/index";         // Redirect về trang chính
    }

    model.addAttribute("error", "Sai tài khoản hoặc mật khẩu");
    return "login"; // Hiển thị lại form login
}


    @GetMapping("/register") public String registerPage() { return "register"; }

    @PostMapping("/register")
    public String register(@RequestParam String username, @RequestParam String password, Model model) {
         var existing = userRepo.findByUsername(username);
        if (existing != null) {
            model.addAttribute("error", "Tên đăng nhập đã tồn tại");
            return "register"; // Hiển thị lại form với lỗi
        }


        var u = new User();
        u.setUsername(username);
        u.setPassword(password);
        u.setRole("user"); // Mặc định là user
        userRepo.save(u);
        return "redirect:/login";
}

    @GetMapping("/logout")
    public String logout(HttpSession session){
        session.invalidate();
        return "redirect:/login";
    }

}