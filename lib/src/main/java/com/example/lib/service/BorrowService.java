package com.example.lib.service;

import com.example.lib.model.Book;
import com.example.lib.model.Borrow;
import com.example.lib.model.Category;
import com.example.lib.model.User;
import com.example.lib.repository.BookRepository;
import com.example.lib.repository.BorrowRepository;
import com.example.lib.repository.ReviewRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;


@Service
public class BorrowService {
    private final BorrowRepository borrowRepo;
    private final BookRepository bookRepo;
    private final ReviewRepository reviewRepo;
    public BorrowService(BorrowRepository borrowRepo, BookRepository bookRepo,ReviewRepository reviewRepo) {
        this.borrowRepo = borrowRepo;
        this.bookRepo = bookRepo;
        this.reviewRepo = reviewRepo;
    }

    @Transactional
    public Borrow createBorrowRequest(Long bookId, User user) {
        Book book = bookRepo.findById(bookId).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sách."));
        if (book.getQuantity() <= 0) { throw new IllegalStateException("Sách đã hết hàng."); }
        
        // Dùng chuỗi "PENDING"
        if (borrowRepo.existsByBookAndUserAndStatus(book, user, "PENDING")) {
            throw new IllegalStateException("Bạn đã có một yêu cầu đang chờ cho cuốn sách này.");
        }

        Borrow newBorrow = new Borrow();
        newBorrow.setBook(book);
        newBorrow.setUser(user);
        newBorrow.setStatus("PENDING"); // Gán chuỗi "PENDING"

        return borrowRepo.save(newBorrow);
    }

    @Transactional
    public void approveBorrowRequest(Long borrowId) {
        Borrow borrow = borrowRepo.findById(borrowId).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn mượn."));
        if (!"PENDING".equals(borrow.getStatus())) { throw new IllegalStateException("Đơn mượn này đã được xử lý."); }

        Book book = borrow.getBook();
        if (book.getQuantity() <= 0) { throw new IllegalStateException("Sách đã hết hàng, không thể duyệt."); }

        book.setQuantity(book.getQuantity() - 1);
        bookRepo.save(book);

        borrow.setStatus("APPROVED"); // Gán chuỗi "APPROVED"
        borrow.setBorrowDate(LocalDate.now());
        borrow.setReturnDate(LocalDate.now().plusDays(14));
        borrowRepo.save(borrow);
    }

    @Transactional
    public void rejectBorrowRequest(Long borrowId) {
        Borrow borrow = borrowRepo.findById(borrowId).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn mượn."));
        if (!"PENDING".equals(borrow.getStatus())) { throw new IllegalStateException("Đơn mượn này đã được xử lý."); }
        
        borrow.setStatus("REJECTED"); // Gán chuỗi "REJECTED"
        borrowRepo.save(borrow);
    }
    
    public List<Borrow> findBorrowsByUser(User user) { return borrowRepo.findByUser(user); }
    public List<Borrow> findAllBorrows() { return borrowRepo.findAll(); }

    // Thêm phương thức này vào cuối lớp BorrowService
public List<Book> recommendBooksForUser(User user) {
    // Lấy danh sách các thể loại mà người dùng đã đánh giá từ 4 sao trở lên
    List<Category> favoriteCategories = reviewRepo.findFavoriteCategoriesByUser(user.getId());

    // Lấy danh sách ID các sách người dùng đã từng mượn
    List<Long> borrowedBookIds = borrowRepo.findBorrowedBookIdsByUserId(user.getId());
    if (borrowedBookIds.isEmpty()) {
        // Thêm một giá trị không thể có để tránh lỗi SQL khi danh sách rỗng
        borrowedBookIds.add(-1L); 
    }

    // Nếu người dùng không có thể loại yêu thích, gợi ý sách được đánh giá cao nhất chung
    if (favoriteCategories.isEmpty()) {
        return bookRepo.findTop4ByOrderByAverageRatingDesc();
    }

    // Ngược lại, tìm sách được đánh giá cao trong các thể loại yêu thích mà người dùng CHƯA MƯỢN
    return bookRepo.findTop4ByCategoriesAndNotInIds(favoriteCategories, borrowedBookIds);
}

}