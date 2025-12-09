package com.asw.karrenkauf.backend.controller;

import com.asw.karrenkauf.backend.dto.ShareListRequest;
import com.asw.karrenkauf.backend.dto.SharedUsersResponse;
import com.asw.karrenkauf.backend.model.ListData;
import com.asw.karrenkauf.backend.model.ListItem;
import com.asw.karrenkauf.backend.model.ListShare;
import com.asw.karrenkauf.backend.model.TokenEntry;
import com.asw.karrenkauf.backend.model.User;
import com.asw.karrenkauf.backend.repository.ListRepository;
import com.asw.karrenkauf.backend.repository.ListShareRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/api")
public class ListController {

    private final ListRepository listRepo;
    private final ListShareRepository listShareRepo;

    public ListController(ListRepository listRepo, ListShareRepository listShareRepo) {
        this.listRepo = listRepo;
        this.listShareRepo = listShareRepo;
    }

    @PostMapping("/lists/sync")
    public String syncLists(
            @RequestParam(required = false) String token,
            @RequestBody Map<String, Map<String, Object>> payload
    ) {
        // --- TOKEN CHECK ---
        String username = "unknown";

        if (token != null && !token.isEmpty()) {
            var entryOpt = AuthController.tokenRepo.findById(token);

            if (entryOpt.isPresent()) {
                TokenEntry entry = entryOpt.get();

                if (!entry.isExpired()) {
                    var userOpt = AuthController.userRepo.findById(entry.getUsername());
                    username = userOpt.map(u -> u.getUserName()).orElse("unknown-user");
                } else {
                	return "Not synced, token has expired. Please login again.";
                }
            } else {
            	return "Not synced, token invalid, please login.";
            }
        }

        // --- LIST SYNC LOGIC ---
        Map<String, Object> lists = payload.get("lists");
        if (lists == null) return "❌ No lists found";

        lists.forEach((id, rawList) -> {
            Map<String, Object> listMap = (Map<String, Object>) rawList;

            ListData listData = new ListData(
                    (String) listMap.get("id"),
                    (String) listMap.get("name"),
                    (String) listMap.get("owner"),
                    ((Number) listMap.get("createdAt")).longValue(),
                    ((Number) listMap.get("updatedAt")).longValue()
            );

            var itemsRaw = (Iterable<Map<String, Object>>) listMap.get("items");
            if (itemsRaw != null) {
                for (Map<String, Object> itemMap : itemsRaw) {
                    ListItem item = new ListItem(
                            (String) itemMap.get("id"),
                            (String) itemMap.get("name"),
                            (String) itemMap.get("beschreibung"),
                            ((Number) itemMap.get("menge")).intValue(),
                            (String) itemMap.get("unit"),
                            ((Number) itemMap.get("preis")).longValue(),
                            (Boolean) itemMap.get("done"),
                            (Boolean) itemMap.get("checked"),
                            (String) itemMap.get("category"),
                            ((Number) itemMap.get("createdAt")).longValue(),
                            ((Number) itemMap.get("updatedAt")).longValue()
                    );
                    listData.addItem(item);
                }
            }

            listRepo.save(listData);
        });

        return "👍 Lists synced by: " + username;
    }

    // Get a list by ID
    @GetMapping("/lists/{id}")
    public ListData getList(@PathVariable String id) {
        return listRepo.findById(id).orElse(null);
    }

    // Get all lists
    @GetMapping("/lists")
    public Iterable<ListData> getAllLists() {
        return listRepo.findAll();
    }

    // Share a list with another user
    @PostMapping("/lists/{listId}/share")
    public ResponseEntity<String> shareList(
            @PathVariable String listId,
            @RequestBody ShareListRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        // Extract username from token
        String currentUsername = extractUsernameFromToken(authHeader);
        if (currentUsername == null) {
            return ResponseEntity.status(401).body("❌ Unauthorized - Please login");
        }

        // Check if list exists
        Optional<ListData> listOpt = listRepo.findById(listId);
        if (listOpt.isEmpty()) {
            return ResponseEntity.status(404).body("❌ List not found");
        }

        ListData list = listOpt.get();

        // Check if current user is the owner
        if (!list.getOwner().equals(currentUsername)) {
            return ResponseEntity.status(403).body("❌ Only the list owner can share it");
        }

        // Check if target user exists
        String targetUsername = request.getUsername();
        if (targetUsername == null || targetUsername.trim().isEmpty()) {
            return ResponseEntity.status(400).body("❌ Username cannot be empty");
        }

        Optional<User> targetUserOpt = AuthController.userRepo.findByUserName(targetUsername);
        if (targetUserOpt.isEmpty()) {
            return ResponseEntity.status(404).body("❌ User not found: " + targetUsername);
        }

        // Check if already shared
        Optional<ListShare> existingShare = listShareRepo.findByListIdAndSharedWithUsername(listId, targetUsername);
        if (existingShare.isPresent()) {
            return ResponseEntity.status(400).body("❌ List already shared with " + targetUsername);
        }

        // Create share
        ListShare share = new ListShare(listId, targetUsername, System.currentTimeMillis());
        listShareRepo.save(share);

        return ResponseEntity.ok("✅ List shared with " + targetUsername);
    }

    // Get shared users for a list
    @GetMapping("/lists/{listId}/shared")
    public ResponseEntity<?> getSharedUsers(
            @PathVariable String listId,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        // Extract username from token
        String currentUsername = extractUsernameFromToken(authHeader);
        if (currentUsername == null) {
            return ResponseEntity.status(401).body("❌ Unauthorized - Please login");
        }

        // Check if list exists
        Optional<ListData> listOpt = listRepo.findById(listId);
        if (listOpt.isEmpty()) {
            return ResponseEntity.status(404).body("❌ List not found");
        }

        ListData list = listOpt.get();

        // Get all shares for this list
        List<ListShare> shares = listShareRepo.findByListId(listId);
        List<String> sharedWithUsernames = shares.stream()
                .map(ListShare::getSharedWithUsername)
                .collect(Collectors.toList());

        SharedUsersResponse response = new SharedUsersResponse(listId, list.getOwner(), sharedWithUsernames);
        return ResponseEntity.ok(response);
    }

    // Remove a user from list sharing
    @DeleteMapping("/lists/{listId}/share/{username}")
    public ResponseEntity<String> unshareList(
            @PathVariable String listId,
            @PathVariable String username,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        // Extract username from token
        String currentUsername = extractUsernameFromToken(authHeader);
        if (currentUsername == null) {
            return ResponseEntity.status(401).body("❌ Unauthorized - Please login");
        }

        // Check if list exists
        Optional<ListData> listOpt = listRepo.findById(listId);
        if (listOpt.isEmpty()) {
            return ResponseEntity.status(404).body("❌ List not found");
        }

        ListData list = listOpt.get();

        // Check if current user is the owner
        if (!list.getOwner().equals(currentUsername)) {
            return ResponseEntity.status(403).body("❌ Only the list owner can unshare");
        }

        // Find and delete the share
        Optional<ListShare> shareOpt = listShareRepo.findByListIdAndSharedWithUsername(listId, username);
        if (shareOpt.isEmpty()) {
            return ResponseEntity.status(404).body("❌ List not shared with " + username);
        }

        listShareRepo.delete(shareOpt.get());
        return ResponseEntity.ok("✅ List unshared with " + username);
    }

    // Get all lists accessible by the user (owned + shared)
    @GetMapping("/lists/accessible")
    public ResponseEntity<?> getAccessibleLists(
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        // Extract username from token
        String currentUsername = extractUsernameFromToken(authHeader);
        if (currentUsername == null) {
            return ResponseEntity.status(401).body("❌ Unauthorized - Please login");
        }

        // Get owned lists
        List<ListData> allLists = StreamSupport.stream(listRepo.findAll().spliterator(), false)
                .collect(Collectors.toList());
        
        List<ListData> ownedLists = allLists.stream()
                .filter(list -> list.getOwner().equals(currentUsername))
                .collect(Collectors.toList());

        // Get shared lists
        List<ListShare> shares = listShareRepo.findBySharedWithUsername(currentUsername);
        Set<String> sharedListIds = shares.stream()
                .map(ListShare::getListId)
                .collect(Collectors.toSet());

        List<ListData> sharedLists = allLists.stream()
                .filter(list -> sharedListIds.contains(list.getId()))
                .collect(Collectors.toList());

        // Combine both lists
        Map<String, Object> result = new HashMap<>();
        result.put("owned", ownedLists);
        result.put("shared", sharedLists);

        return ResponseEntity.ok(result);
    }

    // Helper method to extract username from JWT token
    private String extractUsernameFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }

        String token = authHeader.substring(7);
        Optional<TokenEntry> entryOpt = AuthController.tokenRepo.findById(token);

        if (entryOpt.isEmpty()) {
            return null;
        }

        TokenEntry entry = entryOpt.get();
        if (entry.isExpired()) {
            return null;
        }

        Optional<User> userOpt = AuthController.userRepo.findById(entry.getUsername());
        return userOpt.map(User::getUserName).orElse(null);
    }
}
