package com.asw.karrenkauf.backend.repository;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.asw.karrenkauf.backend.model.User;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUserName(String userName); // search by login name
}