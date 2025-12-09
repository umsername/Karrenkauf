package com.asw.karrenkauf.backend.controller;

import com.asw.karrenkauf.backend.model.ListData;
import com.asw.karrenkauf.backend.model.ListItem;
import com.asw.karrenkauf.backend.model.TokenEntry;
import com.asw.karrenkauf.backend.repository.ListRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class ListController {

    private final ListRepository listRepo;

    public ListController(ListRepository listRepo) {
        this.listRepo = listRepo;
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
}
