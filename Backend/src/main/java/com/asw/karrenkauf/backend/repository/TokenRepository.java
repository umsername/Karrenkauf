package com.asw.karrenkauf.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.asw.karrenkauf.backend.model.TokenEntry;

public interface TokenRepository extends JpaRepository<TokenEntry, String> {
}
