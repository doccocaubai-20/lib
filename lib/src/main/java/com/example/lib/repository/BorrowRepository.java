package com.example.lib.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.lib.model.Borrow;

public interface BorrowRepository extends JpaRepository<Borrow,Long>{

}
