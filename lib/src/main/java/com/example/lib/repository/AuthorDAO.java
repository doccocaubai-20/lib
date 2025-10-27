package com.example.lib.repository;

import com.example.lib.model.Author;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class AuthorDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * RowMapper để ánh xạ một hàng CSDL sang đối tượng Author.
     */
    private static final class AuthorRowMapper implements RowMapper<Author> {
        @Override
        public Author mapRow(ResultSet rs, int rowNum) throws SQLException {
            Author author = new Author();
            author.setId(rs.getLong("id"));
            author.setName(rs.getString("name"));
            return author;
        }
    }

    // === 1. PHƯƠNG THỨC TÌM KIẾM (READ) ===

    /**
     * Tìm một tác giả (Author) bằng ID của họ.
     */
    public Optional<Author> findById(Long id) {
        String sql = "SELECT * FROM authors WHERE id = ?";
        try {
            Author author = jdbcTemplate.queryForObject(sql, new AuthorRowMapper(), id);
            return Optional.ofNullable(author);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty(); // Không tìm thấy
        }
    }

    /**
     * Lấy tất cả các tác giả có trong hệ thống.
     */
    public List<Author> findAll() {
        String sql = "SELECT * FROM authors";
        return jdbcTemplate.query(sql, new AuthorRowMapper());
    }

    // === 2. PHƯƠNG THỨC LƯU (CREATE) ===

    /**
     * Lưu một tác giả (Author) MỚI vào CSDL.
     * Trả về đối tượng đã được gán ID tự động tăng.
     */
    public Author save(Author author) {
        String sql = "INSERT INTO authors (name) VALUES (?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, author.getName());
            return ps;
        }, keyHolder);

        // Lấy ID vừa tạo và gán lại cho đối tượng
        if (keyHolder.getKey() != null) {
            author.setId(keyHolder.getKey().longValue());
        }
        return author;
    }

    // === 3. PHƯƠNG THỨC CẬP NHẬT (UPDATE) ===

    /**
     * Cập nhật một tác giả (Author) đã tồn tại.
     */
    public Author update(Author author) {
        String sql = "UPDATE authors SET name = ? WHERE id = ?";
        jdbcTemplate.update(sql, author.getName(), author.getId());
        return author;
    }

    // === 4. PHƯƠNG THỨC XÓA (DELETE) ===

    /**
     * Xóa một tác giả (Author) khỏi CSDL bằng ID.
     * LƯU Ý: Tương tự như Category, nếu tác giả này đang được gán cho bất kỳ
     * cuốn sách (books) nào, CSDL sẽ ném lỗi Foreign Key vì schema
     * không định nghĩa 'ON DELETE CASCADE' cho books.
     */
    public void deleteById(Long id) {
        String sql = "DELETE FROM authors WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}