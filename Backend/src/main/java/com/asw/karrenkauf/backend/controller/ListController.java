package com.asw.karrenkauf.backend.controller;

import com.asw.karrenkauf.backend.model.User;
import com.asw.karrenkauf.backend.repository.ListDataRepository;
import com.asw.karrenkauf.backend.repository.UserRepository;
import com.asw.karrenkauf.backend.model.ListData;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class ListController {

    private final UserRepository userRepo;
    private final ListDataRepository listRepo;

    public ListController(UserRepository userRepo, ListDataRepository listRepo) {
        this.userRepo = userRepo;
        this.listRepo = listRepo;
    }

    // 1️⃣ Create account
    @PostMapping("/user")
    public String createUser(@RequestParam String username, @RequestParam String password) {
    	if (userRepo.findByUserName(username).isPresent()) {
            return "❌ Username already exists. Pick a different one.";
        }
    	
        String id = UUID.randomUUID().toString();
        User u = new User(id, password, username);
        userRepo.save(u);
        return "Created user " + username + " with id " + id;
    }

    // 2️⃣ Push list JSON (save or update)
    @PostMapping("/lists")
    public String pushLists(@RequestParam String listId, @RequestParam String userId, @RequestBody String jsonData) {
        Optional<ListData> opt = listRepo.findById(listId);
        if (opt.isPresent()) {
            ListData existing = opt.get();
            existing.setData(jsonData);
            existing.setUpdatedAt(System.currentTimeMillis());
            listRepo.save(existing);
            return "Updated list " + listId;
        } else {
            ListData newList = new ListData(listId, userId, userId, jsonData);
            listRepo.save(newList);
            return "Created list " + listId;
        }
    }

    // 3️⃣ Check if list is up-to-date
    @GetMapping("/lists/check")
    public String checkList(@RequestParam String listId, @RequestParam long clientTimestamp) {
        Optional<ListData> opt = listRepo.findById(listId);
        if (opt.isEmpty()) return "List unknown → please fetch full list";
        if (opt.get().getUpdatedAt() <= clientTimestamp) return "Up-to-date";
        else return "Out-of-date → please fetch latest list";
    }
}
