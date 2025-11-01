package com.example.lib.controller;

import com.example.lib.model.Author;
import com.example.lib.model.Book;
import com.example.lib.model.Category;
import com.example.lib.repository.AuthorDAO;
import com.example.lib.repository.BookDAO;
import com.example.lib.repository.CategoryDAO;
import com.example.lib.storage.StorageService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Comparator; 
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/books")
public class BookAdminController {

    private final BookDAO bookRepo;
    private final AuthorDAO authorRepo;
    private final CategoryDAO categoryRepo;
    private final StorageService storageService;

    // (Hàm khởi tạo của bạn đã đúng)
    public BookAdminController(BookDAO bookRepo, AuthorDAO authorRepo, CategoryDAO categoryRepo, StorageService storageService) {
        this.bookRepo = bookRepo;
        this.authorRepo = authorRepo;
        this.categoryRepo = categoryRepo;
        this.storageService = storageService;
    }

    // Hiển thị danh sách sách cho Admin
    @GetMapping
    public String listBooks(Model model) {
        // === SỬA LỖI 1 ===
        // Bỏ 'Sort.by' và sắp xếp trong Java
        List<Book> sortedBooks = bookRepo.findAll().stream()
                .sorted(Comparator.comparing(Book::getId).reversed())
                .collect(Collectors.toList());
        
        model.addAttribute("books", sortedBooks);
        return "admin/books/list";
    }

    // (Hàm này đã đúng, giữ nguyên)
    @GetMapping("/add")
    public String showAddBookForm(Model model) {
        model.addAttribute("book", new Book());
        model.addAttribute("authors", authorRepo.findAll());
        model.addAttribute("categories", categoryRepo.findAll());
        return "admin/books/form";
    }

    // (Hàm này đã đúng, giữ nguyên)
    @GetMapping("/edit/{id}")
    public String showEditBookForm(@PathVariable Long id, Model model) {
        Book book = bookRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid book Id:" + id));
        model.addAttribute("book", book);
        model.addAttribute("authors", authorRepo.findAll());
        model.addAttribute("categories", categoryRepo.findAll());
        return "admin/books/form";
    }

  // === THAY THẾ HÀM CŨ BẰNG HÀM MỚI NÀY ===

    @PostMapping("/save")
    public String saveBook(
            // Nhận các giá trị từ form bằng @RequestParam
            @RequestParam(value = "id", required = false) Long id,
            @RequestParam("title") String title,
            @RequestParam("author") Long authorId, // Nhận author ID
            @RequestParam("category") Long categoryId, // Nhận category ID
            @RequestParam("quantity") Integer quantity,
            @RequestParam("publishDate") String publishDate,
            @RequestParam("description") String description,
            @RequestParam("language") String language,
            
            // Các file và redirect giữ nguyên
            @RequestParam("imageFile") MultipartFile imageFile,
            @RequestParam("pdfFile") MultipartFile pdfFile,
            RedirectAttributes redirectAttributes) {

        Book book;
        String oldImage = null;
        String oldPdf = null;

        // 1. Kiểm tra xem đây là Sửa hay Thêm mới
        if (id != null) {
            // Đây là Sửa -> Tải sách cũ từ CSDL
            book = bookRepo.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid book Id:" + id));
            oldImage = book.getImage();
            oldPdf = book.getPdfPath();
        } else {
            // Đây là Thêm mới
            book = new Book();
        }

        // 2. Tự gán các giá trị từ form vào đối tượng
        book.setTitle(title);
        book.setQuantity(quantity);
        book.setDescription(description);
        book.setLanguage(language);
        
        // Chuyển đổi ngày (nếu có)
        if (publishDate != null && !publishDate.isEmpty()) {
            book.setPublishDate(java.time.LocalDate.parse(publishDate));
        }

        // 3. Tự tìm đối tượng Author và Category từ ID
        Author author = authorRepo.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid author Id:" + authorId));
        Category category = categoryRepo.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid category Id:" + categoryId));
        
        book.setAuthor(author);
        book.setCategory(category);

        // 4. Xử lý file (giống như code cũ của bạn)
        if (!imageFile.isEmpty()) {
            storageService.delete(oldImage); // Xóa ảnh cũ
            String imageName = storageService.store(imageFile);
            book.setImage(imageName);
        }

        if (!pdfFile.isEmpty()) {
            storageService.delete(oldPdf); // Xóa file PDF cũ
            String pdfName = storageService.store(pdfFile);
            book.setPdfPath(pdfName);
            // Gán lại pageCount nếu PDF mới được tải lên
            int newPageCount = storageService.getPdfPageCount(pdfFile);
            book.setPageCount(newPageCount);
        }

        // 5. Lưu (phân biệt save và update)
        if (book.getId() == null) {
            bookRepo.save(book); // Tạo mới
        } else {
            bookRepo.update(book); // Cập nhật
        }

        redirectAttributes.addFlashAttribute("successMessage", "Lưu sách thành công!");
        return "redirect:/admin/books";
    }
    // (Hàm này đã đúng, giữ nguyên)
    @GetMapping("/delete/{id}")
    public String deleteBook(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
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