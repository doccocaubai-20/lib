package com.example.lib.repository;

import com.example.lib.model.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class RoleDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * RowMapper để ánh xạ một hàng CSDL sang đối tượng Role.
     */
    private static final class RoleRowMapper implements RowMapper<Role> {
        @Override
        public Role mapRow(ResultSet rs, int rowNum) throws SQLException {
            Role role = new Role();
            // Lấy ID từ CSDL (kiểu INT) và gán vào model (kiểu Integer)
            role.setId(rs.getLong("id")); 
            role.setName(rs.getString("name"));
            return role;
        }
    }

    // === PHƯƠNG THỨC BẠN YÊU CẦU ===

    /**
     * Tìm một vai trò (Role) bằng tên của nó (ví dụ: "ROLE_USER").
     * Đây là phương thức quan trọng cho UserService khi đăng ký người dùng mới.
     *
     * @param name Tên vai trò cần tìm.
     * @return Optional chứa Role nếu tìm thấy, ngược lại trả về Optional rỗng.
     */
    public Optional<Role> findByName(String name) {
        String sql = "SELECT id, name FROM roles WHERE name = ?";
        try {
            // Dùng queryForObject vì 'name' là UNIQUE
            Role role = jdbcTemplate.queryForObject(sql, new RoleRowMapper(), name);
            return Optional.ofNullable(role);
        } catch (EmptyResultDataAccessException e) {
            // queryForObject ném exception này nếu không tìm thấy
            return Optional.empty();
        }
    }

    // === CÁC PHƯƠNG THỨC HỮU ÍCH KHÁC ===

    /**
     * Tìm một vai trò (Role) bằng ID của nó.
     */
    public Optional<Role> findById(Long id) { 
        String sql = "SELECT id, name FROM roles WHERE id = ?";
        try {
            Role role = jdbcTemplate.queryForObject(sql, new RoleRowMapper(), id);
            return Optional.ofNullable(role);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * Lấy tất cả các vai trò có trong hệ thống.
     * (Hữu ích cho trang form quản lý User của Admin)
     */
    public List<Role> findAll() {
        String sql = "SELECT id, name FROM roles";
        return jdbcTemplate.query(sql, new RoleRowMapper());
    }

    // Các hàm save, update, delete cho Role thường không cần thiết
    // vì chúng thường được chèn cố định vào CSDL (như trong schema của bạn).
}