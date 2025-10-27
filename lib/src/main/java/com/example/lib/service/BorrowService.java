package com.example.lib.service;

import com.example.lib.model.Book;
import com.example.lib.model.Borrow;
import com.example.lib.model.User;
import com.example.lib.repository.BookDAO;
import com.example.lib.repository.BorrowDAO;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;


@Service
public class BorrowService {
    private final BorrowDAO borrowRepo;
    private final BookDAO bookRepo;
    public BorrowService(BorrowDAO borrowRepo, BookDAO bookRepo) {
        this.borrowRepo = borrowRepo;
        this.bookRepo = bookRepo;
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

        Book book = bookRepo.findById(borrow.getBook().getId()).orElseThrow(() -> new IllegalStateException("Không tồn tại sách."));
        if (book.getQuantity() <= 0) { throw new IllegalStateException("Sách đã hết hàng, không thể duyệt."); }

        book.setQuantity(book.getQuantity() - 1);
        bookRepo.update(book);

        borrow.setStatus("APPROVED"); // Gán chuỗi "APPROVED"
        borrow.setBorrowDate(LocalDate.now());
        borrow.setReturnDate(LocalDate.now().plusDays(14));
        borrowRepo.update(borrow);
    }

    @Transactional
    public void rejectBorrowRequest(Long borrowId) {
        Borrow borrow = borrowRepo.findById(borrowId).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn mượn."));
        if (!"PENDING".equals(borrow.getStatus())) { throw new IllegalStateException("Đơn mượn này đã được xử lý."); }
        
        borrow.setStatus("REJECTED"); // Gán chuỗi "REJECTED"
        borrowRepo.update(borrow);
    }
    
    public List<Borrow> findBorrowsByUser(User user) { return borrowRepo.findByUser(user); }
    public List<Borrow> findAllBorrows() { return borrowRepo.findAll(); }


}