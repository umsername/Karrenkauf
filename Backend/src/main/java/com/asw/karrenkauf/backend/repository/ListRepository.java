package com.asw.karrenkauf.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.asw.karrenkauf.backend.model.ListData;

public interface ListRepository extends JpaRepository<ListData, String> {
}
