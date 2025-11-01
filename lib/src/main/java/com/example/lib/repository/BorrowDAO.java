package com.example.lib.repository;

import com.example.lib.model.Book;
import com.example.lib.model.Borrow;
import com.example.lib.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date; // Sử dụng java.sql.Date
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class BorrowDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * RowMapper để ánh xạ một hàng CSDL sang đối tượng Borrow.
     */
    private static final class BorrowRowMapper implements RowMapper<Borrow> {
        @Override
        public Borrow mapRow(ResultSet rs, int rowNum) throws SQLException {
            Borrow borrow = new Borrow();
            borrow.setId(rs.getLong("id"));

            // Chuyển đổi java.sql.Date (từ CSDL) sang java.time.LocalDate (trong Model)
            Date borrowDateSql = rs.getDate("borrow_date");
            if (borrowDateSql != null) {
                borrow.setBorrowDate(borrowDateSql.toLocalDate());
            }

            Date returnDateSql = rs.getDate("return_date");
            if (returnDateSql != null) {
                borrow.setReturnDate(returnDateSql.toLocalDate());
            }
            
            // Lấy 'status' từ schema V3
            borrow.setStatus(rs.getString("status")); 

            // Tạo đối tượng User và Book "placeholder" chỉ chứa ID
            User user = new User();
            user.setId(rs.getLong("user_id"));
            user.setUsername(rs.getString("user_username"));
            borrow.setUser(user);

            Book book = new Book();
            book.setId(rs.getLong("book_id"));
            book.setTitle(rs.getString("book_title"));
            book.setImage(rs.getString("book_image"));
            borrow.setBook(book);

            return borrow;
        }
    }

    private final String BASE_SELECT_SQL_JOINED = "SELECT " +     
                                                 "br.*, " +
                                                 "b.title as book_title,b.image as book_image, " +
                                                 "u.username as user_username " + 
                                                 "FROM borrows br " +
                                                 "JOIN books b ON br.book_id = b.id " +
                                                 "JOIN users u  ON br.user_id = u.id ";

    public List<Borrow> findByUser(User user) {
        String sql = BASE_SELECT_SQL_JOINED + "WHERE br.user_id = ?";
        return jdbcTemplate.query(sql,new BorrowRowMapper(),user.getId());
    }

    public List<Borrow> findAll(){
        String sql = BASE_SELECT_SQL_JOINED + " ORDER BY br.id DESC";
        return jdbcTemplate.query(sql, new BorrowRowMapper());
    }

    public Optional<Borrow> findById(Long borrowId) {
        String sql = BASE_SELECT_SQL_JOINED + " WHERE br.id = ?";
        try {
            Borrow borrow = jdbcTemplate.queryForObject(sql, new BorrowRowMapper(), borrowId);
            return Optional.ofNullable(borrow);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty(); 
        }
    }
    // === 2. PHƯƠNG THỨC existsByBookAndUserAndStatus ===

    /**
     * Kiểm tra xem có tồn tại một lượt mượn với sách, người dùng, và trạng thái cụ thể.
     * (Thường dùng để kiểm tra xem user có đang mượn sách này không).
     * Tương đương: boolean existsByBookAndUserAndStatus(Book book, User user, String status);
     */
    public boolean existsByBookAndUserAndStatus(Book book, User user, String status) {
        String sql = "SELECT COUNT(*) FROM borrows WHERE book_id = ? AND user_id = ? AND status = ?";
        
        // queryForObject trả về 1 giá trị duy nhất (số lượng)
        Integer count = jdbcTemplate.queryForObject(sql, 
                Integer.class, 
                book.getId(), 
                user.getId(), 
                status);
        
        return (count != null && count > 0);
    }


    // === CÁC PHƯƠNG THỨC CRUD CƠ BẢN (NÊN CÓ) ===

    /**
     * Lưu một lượt mượn (Borrow) MỚI vào CSDL.
     */
    public Borrow save(Borrow borrow) {
        // SQL này bao gồm cột 'status' từ V3
        String sql = "INSERT INTO borrows (user_id, book_id, borrow_date, return_date, status) " +
                     "VALUES (?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, borrow.getUser().getId());
            ps.setLong(2, borrow.getBook().getId());
            
            if (borrow.getBorrowDate() != null){
                ps.setDate(3, Date.valueOf(borrow.getBorrowDate()));
            }else {
                ps.setNull(3,java.sql.Types.DATE); // Gan NULL cho DB
            }
            
            // Xử lý return_date có thể là null
            if (borrow.getReturnDate() != null) {
                ps.setDate(4, Date.valueOf(borrow.getReturnDate()));
            } else {
                ps.setNull(4, java.sql.Types.DATE);
            }
            
            ps.setString(5, borrow.getStatus());
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            borrow.setId(keyHolder.getKey().longValue());
        }
        return borrow;
    }

    /**
     * Cập nhật một lượt mượn (ví dụ: khi trả sách).
     */
    public Borrow update(Borrow borrow) {
        String sql = "UPDATE borrows SET " +
                     "borrow_date = ?, " +
                     "return_date = ?, " +
                     "status = ? " +
                     "WHERE id = ?";

        jdbcTemplate.update(sql,
                Date.valueOf(borrow.getBorrowDate()),
                (borrow.getReturnDate() != null ? Date.valueOf(borrow.getReturnDate()) : null),
                borrow.getStatus(),
                borrow.getId());
        
        return borrow;
    }
}