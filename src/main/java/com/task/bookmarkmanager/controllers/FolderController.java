package com.task.bookmarkmanager.controllers;

import com.task.bookmarkmanager.model.Folder;
import com.task.bookmarkmanager.services.FolderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

@RestController
@RequestMapping("/api/folders")
public class FolderController {

    @Autowired
    private FolderService folderService;

    @PostMapping
    public ResponseEntity<?> createFolder(@Valid @RequestBody Folder folder, @RequestHeader("userId") String userId) {
        folder.setUserId(userId);
        Folder createdFolder = folderService.createFolder(folder);
        return ResponseEntity.ok(createdFolder);
    }

    @PutMapping("/{folderId}/move-to/{newParentFolderId}")
    public ResponseEntity<?> moveFolder(
            @RequestHeader("userId") String userId,
            @PathVariable String folderId,
            @PathVariable String newParentFolderId) {
        try {
            folderService.moveFolder(userId, folderId, newParentFolderId);
            return ResponseEntity.ok("Folder moved successfully");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getFolder(@PathVariable String id, @RequestHeader("userId") String userId) {
        Optional<Folder> folder = folderService.getFolderById(userId, id);
        return folder.isPresent() ? ResponseEntity.ok(folder.get()) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFolder(@PathVariable String id, @RequestHeader("userId") String userId) {
        folderService.deleteFolderRecursively(userId, id);
        return ResponseEntity.noContent().build();
    }
}
