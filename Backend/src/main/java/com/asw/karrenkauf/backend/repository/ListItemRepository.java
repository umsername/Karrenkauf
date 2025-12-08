package com.asw.karrenkauf.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.asw.karrenkauf.backend.model.ListItem;

public interface ListItemRepository extends JpaRepository<ListItem, String> {
}
