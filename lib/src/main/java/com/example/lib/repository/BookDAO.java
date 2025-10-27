package com.example.lib.repository;

import com.example.lib.model.Author;
import com.example.lib.model.Book;
import com.example.lib.model.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date; // Dùng java.sql.Date
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class BookDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // --- CÂU SQL CƠ SỞ VÀ ROWMAPPER ---

    /**
     * Định nghĩa câu SELECT cơ sở với JOIN.
     * Chúng ta luôn JOIN để lấy Author và Category,
     * mô phỏng lại hành vi EAGER loading của JPA.
     */
    private final String BASE_SELECT_SQL = 
        "SELECT b.*, " +
        "a.name as author_name, " +
        "c.name as category_name " +
        "FROM books b " +
        "LEFT JOIN authors a ON b.author_id = a.id " +
        "LEFT JOIN categories c ON b.category_id = c.id ";

    /**
     * RowMapper này ánh xạ dữ liệu từ câu SELECT cơ sở (đã JOIN).
     */
    private static final class BookRowMapper implements RowMapper<Book> {
        @Override
        public Book mapRow(ResultSet rs, int rowNum) throws SQLException {
            Book book = new Book();
            book.setId(rs.getLong("id"));
            book.setTitle(rs.getString("title"));
            
            // Chuyển đổi java.sql.Date sang java.time.LocalDate
            Date publishDateSql = rs.getDate("publish_date");
            if (publishDateSql != null) {
                book.setPublishDate(publishDateSql.toLocalDate());
            }

            book.setQuantity(rs.getInt("quantity"));
            book.setPdfPath(rs.getString("pdf_path"));
            book.setDescription(rs.getString("description"));
            book.setImage(rs.getString("image"));
            book.setLanguage(rs.getString("language"));
            
            // Từ migration V2
            book.setPageCount(rs.getInt("page_count"));
            
            // Từ migration V4
            book.setAverageRating(rs.getDouble("average_rating"));

            // --- Xử lý Author (từ JOIN) ---
            Author author = new Author();
            author.setId(rs.getLong("author_id"));
            author.setName(rs.getString("author_name")); // Lấy từ JOIN
            book.setAuthor(author);

            // --- Xử lý Category (từ JOIN) ---
            Category category = new Category();
            category.setId(rs.getLong("category_id"));
            category.setName(rs.getString("category_name")); // Lấy từ JOIN
            book.setCategory(category);
            
            return book;
        }
    }


    // === 1. PHƯƠNG THỨC BẠN YÊU CẦU ===

    /**
     * Tương đương: List<Book> findByTitleContainingIgnoreCase(String title);
     */
    public List<Book> findByTitleContainingIgnoreCase(String title) {
        String sql = BASE_SELECT_SQL + "WHERE LOWER(b.title) LIKE ?";
        String likeParam = "%" + (title != null ? title.toLowerCase() : "") + "%";
        
        return jdbcTemplate.query(sql, new BookRowMapper(),likeParam);
    }

    
    // === 2. CÁC PHƯƠNG THỨC CRUD CƠ BẢN ===

    /**
     * Tìm sách bằng ID.
     */
    public Optional<Book> findById(Long id) {
        String sql = BASE_SELECT_SQL + "WHERE b.id = ?";
        try {
            Book book = jdbcTemplate.queryForObject(sql, new BookRowMapper(), id);
            return Optional.ofNullable(book);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * Tìm tất cả sách.
     */
    public List<Book> findAll() {
        return jdbcTemplate.query(BASE_SELECT_SQL, new BookRowMapper());
    }

    /**
     * Lưu một sách MỚI.
     */
    public Book save(Book book) {
        // SQL này không bao gồm 'average_rating' vì nó nên được tính toán
        String sql = "INSERT INTO books " +
                     "(title, author_id, category_id, publish_date, quantity, pdf_path, description, image, language, page_count) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, book.getTitle());
            ps.setLong(2, book.getAuthor().getId());
            ps.setLong(3, book.getCategory().getId());
            ps.setDate(4, book.getPublishDate() != null ? Date.valueOf(book.getPublishDate()) : null);
            ps.setInt(5, book.getQuantity());
            ps.setString(6, book.getPdfPath());
            ps.setString(7, book.getDescription());
            ps.setString(8, book.getImage());
            ps.setString(9, book.getLanguage());
            ps.setInt(10, book.getPageCount());
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            book.setId(keyHolder.getKey().longValue());
        }
        return book;
    }

    /**
     * Cập nhật một sách đã có.
     */
    public Book update(Book book) {
        // Cập nhật mọi thứ ngoại trừ ID và average_rating
        String sql = "UPDATE books SET " +
                     "title = ?, author_id = ?, category_id = ?, publish_date = ?, " +
                     "quantity = ?, pdf_path = ?, description = ?, image = ?, " +
                     "language = ?, page_count = ? " +
                     "WHERE id = ?";

        jdbcTemplate.update(sql,
                book.getTitle(),
                book.getAuthor().getId(),
                book.getCategory().getId(),
                book.getPublishDate() != null ? Date.valueOf(book.getPublishDate()) : null,
                book.getQuantity(),
                book.getPdfPath(),
                book.getDescription(),
                book.getImage(),
                book.getLanguage(),
                book.getPageCount(),
                book.getId()
        );
        return book;
    }

    /**
     * Xóa sách bằng ID.
     */
    public void deleteById(Long id) {
        // Nhờ 'ON DELETE CASCADE' cho 'reviews' trong V1, 
        // các review liên quan sẽ tự động bị xóa.
        String sql = "DELETE FROM books WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
    
    // === 3. THAY THẾ CHO BookSpecification ===

    /**
     * Tái hiện logic của BookSpecification (lọc động).
     * Đây là nơi chúng ta tự xây dựng câu lệnh SQL.
     */
    public List<Book> findWithFilters(String keyword, List<Long> authorIds, List<Long> categoryIds, Double minRating) {
        
        // Dùng StringBuilder để xây dựng câu SQL
        StringBuilder sqlBuilder = new StringBuilder(BASE_SELECT_SQL);
        sqlBuilder.append("WHERE 1=1 "); // Mẹo để luôn bắt đầu mệnh đề WHERE

        // List để chứa các tham số động
        List<Object> params = new ArrayList<>();

        // Lọc theo từ khóa (title)
        if (keyword != null && !keyword.isEmpty()) {
            sqlBuilder.append("AND LOWER(b.title) LIKE ? ");
            params.add("%" + keyword.toLowerCase() + "%");
        }

        // Lọc theo tác giả
        if (authorIds != null && !authorIds.isEmpty()) {
            // Xây dựng chuỗi (?...?)
            String inClause = String.join(",", java.util.Collections.nCopies(authorIds.size(), "?"));
            sqlBuilder.append("AND b.author_id IN (").append(inClause).append(") ");
            params.addAll(authorIds);
        }

        // Lọc theo thể loại
        if (categoryIds != null && !categoryIds.isEmpty()) {
            String inClause = String.join(",", java.util.Collections.nCopies(categoryIds.size(), "?"));
            sqlBuilder.append("AND b.category_id IN (").append(inClause).append(") ");
            params.addAll(categoryIds);
        }

        // Lọc theo điểm đánh giá (từ V4)
        if (minRating != null && minRating > 0) {
            sqlBuilder.append("AND b.average_rating >= ? ");
            params.add(minRating);
        }

        // Thực thi câu SQL động
       return jdbcTemplate.query(
            sqlBuilder.toString(),
            new BookRowMapper(), // <-- 1. RowMapper đứng thứ 2
            params.toArray()     // <-- 2. Tham số (args) đứng cuối cùng
        );
    }

    public void updateAverageRating(Long bookId) {
        
        // Câu SQL này tính AVG từ 'reviews' và UPDATE 'books' trong 1 lệnh
        String sql = "UPDATE books b SET b.average_rating = " +
                     "(SELECT AVG(r.rating) FROM reviews r WHERE r.book_id = ?) " +
                     "WHERE b.id = ?";
        
        // Cung cấp bookId 2 lần: 
        // 1. Cho mệnh đề WHERE của subquery (SELECT AVG)
        // 2. Cho mệnh đề WHERE của (UPDATE books)
        jdbcTemplate.update(sql, bookId, bookId);
    }
    public long count() {
        String sql = "SELECT COUNT(*) FROM books";
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return (count != null) ? count : 0L;
    }
}