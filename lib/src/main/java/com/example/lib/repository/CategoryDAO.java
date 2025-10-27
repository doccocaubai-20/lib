package com.example.lib.repository;

import com.example.lib.model.Category;
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
public class CategoryDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * RowMapper để ánh xạ một hàng CSDL sang đối tượng Category.
     */
    private static final class CategoryRowMapper implements RowMapper<Category> {
        @Override
        public Category mapRow(ResultSet rs, int rowNum) throws SQLException {
            Category category = new Category();
            // Lấy ID từ CSDL (BIGINT) và gán vào model (Long)
            category.setId(rs.getLong("id"));
            category.setName(rs.getString("name"));
            return category;
        }
    }

    // === 1. PHƯƠNG THỨC findById ===

    /**
     * Tìm một thể loại (Category) bằng ID của nó.
     */
    public Optional<Category> findById(Long id) {
        String sql = "SELECT * FROM categories WHERE id = ?";
        try {
            // Dùng queryForObject vì 'id' là khóa chính
            Category category = jdbcTemplate.queryForObject(sql, new CategoryRowMapper(), id);
            return Optional.ofNullable(category);
        } catch (EmptyResultDataAccessException e) {
            // queryForObject ném exception này nếu không tìm thấy
            return Optional.empty();
        }
    }

    // === 2. PHƯƠNG THỨC findAll ===

    /**
     * Lấy tất cả các thể loại có trong hệ thống.
     * (Hữu ích cho trang quản lý admin và form thêm/sửa sách)
     */
    public List<Category> findAll() {
        String sql = "SELECT * FROM categories";
        return jdbcTemplate.query(sql, new CategoryRowMapper());
    }

    // === 3. PHƯƠNG THỨC save (INSERT) ===

    /**
     * Lưu một thể loại (Category) MỚI vào CSDL.
     * Phương thức này sẽ lấy ID tự động tăng và gán lại vào đối tượng.
     */
    public Category save(Category category) {
        String sql = "INSERT INTO categories (name) VALUES (?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, category.getName());
            return ps;
        }, keyHolder);

        // Lấy ID vừa tạo và gán lại cho đối tượng
        if (keyHolder.getKey() != null) {
            category.setId(keyHolder.getKey().longValue());
        }
        return category;
    }

    // === (BỔ SUNG) PHƯƠNG THỨC update ===

    /**
     * Cập nhật một thể loại (Category) đã tồn tại trong CSDL.
     */
    public Category update(Category category) {
        String sql = "UPDATE categories SET name = ? WHERE id = ?";
        jdbcTemplate.update(sql, category.getName(), category.getId());
        return category;
    }


    // === 4. PHƯƠNG THỨC deleteById ===

    /**
     * Xóa một thể loại (Category) khỏi CSDL bằng ID.
     * LƯU Ý: Nếu thể loại này đang được gán cho bất kỳ cuốn sách (books) nào,
     * CSDL sẽ ném lỗi Foreign Key Constraint Violation, vì schema
     * không định nghĩa 'ON DELETE CASCADE' cho books.
     */
    public void deleteById(Long id) {
        String sql = "DELETE FROM categories WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}