package com.example.effective_mobile.repository;

import com.example.effective_mobile.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
       Optional<User> findByEmail(String email);
       Optional<User> findByUsername(String username);
}
