package com.example.lib.repository;


import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.example.lib.model.Role;
import com.example.lib.model.User;

@Repository
public class UserDAO {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final class RoleRowMapper implements RowMapper<Role> {
        @Override
        public Role mapRow(ResultSet rs, int rowNum) throws SQLException {
            Role role = new Role();
            // Giả sử bảng roles có cột 'id' và 'name'
            role.setId(rs.getLong("id"));
            role.setName(rs.getString("name"));
            return role;
        }
    }

    private static final class UserRowMapper implements RowMapper<User> {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            User user = new User();
            user.setId(rs.getLong("id"));
            user.setUsername(rs.getString("username"));
            user.setPassword(rs.getString("password"));
            user.setFullName(rs.getString("full_name"));
            user.setActive(rs.getBoolean("is_active"));
            
            Timestamp createdAtTime= rs.getTimestamp("created_at");
            if (createdAtTime != null){
                user.setCreatedAt(createdAtTime.toInstant());
            }
            user.setRoles(new HashSet<>());
            return user;
        }
    }

    private Set<Role> findRolesByUserId(Long userId){
        String sql = "Select r.id, r.name from roles r " +
                     "JOIN user_roles ur ON r.id = ur.role_id "+
                     "WHERE ur.user_id = ?";
        List<Role> roles = jdbcTemplate.query(sql, new RoleRowMapper(),userId);
        return new HashSet<>(roles);
    }

    public Optional<User> findByUsername(String username) {
        String userSql = "SELECT * FROM users WHERE username = ?";
        
        try {
            List<User> us = jdbcTemplate.query(userSql, new UserRowMapper(),username);
            User user = (us != null && !us.isEmpty()) ? us.get(0) : null;   
            if (user != null) {
                Set<Role> roles = findRolesByUserId(user.getId());
                
                // Bước 3: Gán roles vào đối tượng user
                user.setRoles(roles);
            }
            
            return Optional.ofNullable(user);

        } catch (Exception e) {
            return Optional.empty();
        }
    }
    public Optional<User> findById(Long userId){
        String sql = "SELECT * FROM users WHERE id = ?";
        try {
            User user = jdbcTemplate.queryForObject(sql, new UserRowMapper(),userId);
            if (user != null){
                user.setRoles(findRolesByUserId(user.getId()));
            }
            return Optional.ofNullable(user);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    public List<User> findAllByRoles_NameNot(String roleName){
        String sql = "SELECT * FROM users u " +
                     "WHERE u.id NOT IN("   +
                     "SELECT ur.user_id "    +
                     "FROM user_roles ur "   +
                     "JOIN roles r ON  ur.role_id = r.id "+
                     "WHERE r.name = ?" + ")";
        List<User> users = jdbcTemplate.query(sql,new UserRowMapper(), roleName);
        for (User user: users){
            Set<Role> roles = findRolesByUserId(user.getId());
            user.setRoles(roles);
        }
        return users;
    }

public User save(User user) {
        // Câu SQL cho bảng 'users'
        // (Bao gồm các trường bạn yêu cầu + các trường khác từ schema)
        String sql = "INSERT INTO users (username, password, full_name, is_active, created_at) " +
                     "VALUES (?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        // 1. Lưu thông tin cơ bản của User
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getFullName());
            ps.setBoolean(4, user.isActive());
            ps.setTimestamp(5, Timestamp.from(user.getCreatedAt() != null ? user.getCreatedAt() : Instant.now()));
            return ps;
        }, keyHolder);

        // 2. Lấy ID vừa tạo và gán lại cho đối tượng
        if (keyHolder.getKey() != null) {
            user.setId(keyHolder.getKey().longValue());
        }

        // 3. Lưu các vai trò (roles) vào bảng user_roles
        updateUserRoles(user); // Gọi hàm helper bên dưới

        return user;
    }

    // === 2. PHƯƠNG THỨC UPDATE ===

    /**
     * Cập nhật thông tin của một User và đồng bộ hóa vai trò của họ.
     */
    public User update(User user) {
        // Câu SQL cập nhật các trường bạn yêu cầu
        // (Lưu ý: Thường không cập nhật 'created_at' khi update)
        String sql = "UPDATE users SET " +
                     "username = ?, " +
                     "password = ?, " +
                     "full_name = ?, " +
                     "is_active = ? " +
                     "WHERE id = ?";

        // 1. Cập nhật bảng 'users'
        jdbcTemplate.update(sql,
                user.getUsername(),
                user.getPassword(),
                user.getFullName(),
                user.isActive(),
                user.getId());

        // 2. Đồng bộ hóa vai trò (xóa cũ, thêm mới)
        updateUserRoles(user);

        return user;
    }

    /**
     * Phương thức helper để đồng bộ hóa bảng 'user_roles'.
     * Xóa tất cả vai trò hiện tại của user và thêm lại các vai trò từ đối tượng User.
     */
    private void updateUserRoles(User user) {
        if (user.getId() == null) return; // Không thể cập nhật roles nếu không có user ID

        // a. Xóa tất cả vai trò cũ
        jdbcTemplate.update("DELETE FROM user_roles WHERE user_id = ?", user.getId());

        // b. Thêm các vai trò mới
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            String insertRoleSql = "INSERT INTO user_roles (user_id, role_id) VALUES (?, ?)";
            
            for (Role role : user.getRoles()) {
                if(role.getId() != null) { // Đảm bảo Role có ID
                    jdbcTemplate.update(insertRoleSql, user.getId(), role.getId());
                }
            }
        }
    }


    // === 3. PHƯƠNG THỨC DELETE ===

    /**
     * Xóa một User khỏi CSDL.
     * Nhờ 'ON DELETE CASCADE' trong V1__Init_Schema.sql, các bản ghi
     * trong 'user_roles', 'borrows', 'reviews' sẽ tự động bị xóa.
     */
    public void delete(Long userId) {
        String sql = "DELETE FROM users WHERE id = ?";
        jdbcTemplate.update(sql, userId);
    }

    public long count() {
            String sql = "SELECT COUNT(*) FROM users";
          
            Long count = jdbcTemplate.queryForObject(sql, Long.class);
            return (count != null) ? count : 0L;
    }

}
