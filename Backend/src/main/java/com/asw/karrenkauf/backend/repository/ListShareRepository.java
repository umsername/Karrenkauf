package com.asw.karrenkauf.backend.repository;

import com.asw.karrenkauf.backend.model.ListShare;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ListShareRepository extends JpaRepository<ListShare, Long> {
    List<ListShare> findBySharedWithUsername(String username);
    List<ListShare> findByListId(String listId);
    Optional<ListShare> findByListIdAndSharedWithUsername(String listId, String username);
    void deleteByListIdAndSharedWithUsername(String listId, String username);
}
