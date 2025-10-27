package com.example.lib.security;

import org.springframework.beans.factory.annotation.Autowired; // <-- THÊM IMPORT
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider; // <-- THÊM IMPORT
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService; // <-- THÊM IMPORT (nếu chưa có)
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserDetailsService userDetailsService; 

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // === THÊM BEAN NÀY ===
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService); // Cung cấp UserDetailsService
        authProvider.setPasswordEncoder(passwordEncoder()); // Cung cấp PasswordEncoder
        return authProvider;
    }
    // === KẾT THÚC THÊM ===


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
             // Đăng ký AuthenticationProvider mà chúng ta vừa tạo
            .authenticationProvider(authenticationProvider()) 
            .authorizeHttpRequests(authorize -> authorize
                 // SỬA CẢNH BÁO: Thêm dấu /
                .requestMatchers("/register", "/css/**", "/js/**", "/lib/**").permitAll() 
                .requestMatchers("/books/add", "/books/edit/**", "/books/delete/**").hasRole("ADMIN") 
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login") 
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/index", true) 
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );
            // Bạn có thể thêm .csrf(csrf -> csrf.disable()) nếu gặp lỗi CSRF khi POST form
        return http.build();
    }
}