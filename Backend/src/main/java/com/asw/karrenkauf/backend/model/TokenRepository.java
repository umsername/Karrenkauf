package com.asw.karrenkauf.backend.model;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenRepository extends JpaRepository<TokenEntry, String> {
}
