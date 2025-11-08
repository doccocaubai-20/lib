package com.example.lib.repository;

import com.example.lib.model.Book;
import com.example.lib.model.Review;
import com.example.lib.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.Instant;
import java.util.List;

@Repository
public class ReviewDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * RowMapper chung cho Review.
     * Ánh xạ các trường cơ bản và ID của các đối tượng liên quan.
     */
    private static final class ReviewRowMapper implements RowMapper<Review> {
        @Override
        public Review mapRow(ResultSet rs, int rowNum) throws SQLException {
            Review review = new Review();
            review.setId(rs.getLong("id"));
            review.setRating(rs.getInt("rating"));
            review.setComment(rs.getString("comment"));

            Timestamp ts = rs.getTimestamp("created_at");
            if (ts != null) {
                review.setCreatedAt(ts.toInstant());
            }

            // Tạo đối tượng User và Book "placeholder" chỉ chứa ID
            User user = new User();
            user.setId(rs.getLong("user_id"));
            user.setUsername(rs.getString("user_username"));
            review.setUser(user);

            Book book = new Book();
            book.setId(rs.getLong("book_id"));
            review.setBook(book);

            return review;
        }
    }

    // === 1. PHƯƠNG THỨC findByBookId ===

    /**
     * Tìm tất cả đánh giá cho một cuốn sách cụ thể.
     * Tương đương: List<Review> findByBookId(Long bookId);
     */
    public List<Review> findByBookId(Long bookId) {
        String sql = "SELECT r.*, u.username as user_username " + 
                     "FROM reviews r " +
                     "JOIN users u ON r.user_id = u.id " +
                     "WHERE r.book_id = ?";
        return jdbcTemplate.query(sql, new ReviewRowMapper(),bookId);
    }
    // === CÁC PHƯƠNG THỨC CRUD CƠ BẢN (NÊN CÓ) ===

    /**
     * Lưu một đánh giá mới.
     */
    public Review save(Review review) {
        String sql = "INSERT INTO reviews (user_id, book_id, rating, comment, created_at) " +
                     "VALUES (?, ?, ?, ?, ?)";
        
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, review.getUser().getId());
            ps.setLong(2, review.getBook().getId());
            ps.setInt(3, review.getRating());
            ps.setString(4, review.getComment());
            ps.setTimestamp(5, Timestamp.from(review.getCreatedAt() != null ? review.getCreatedAt() : Instant.now()));
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            review.setId(keyHolder.getKey().longValue());
        }
        return review;
    }

    /**
     * Xóa một đánh giá (ví dụ: admin xóa).
     */
    public void delete(Long reviewId) {
        String sql = "DELETE FROM reviews WHERE id = ?";
        jdbcTemplate.update(sql, reviewId);
    }
}