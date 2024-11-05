package com.task.bookmarkmanager.controllers;
import com.task.bookmarkmanager.model.Bookmark;
import com.task.bookmarkmanager.repository.BookmarkRepository;
import com.task.bookmarkmanager.services.BookmarkService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/bookmarks")
public class BookmarkController {

    @Autowired
    private BookmarkService bookmarkService;

    @PostMapping
    public ResponseEntity<?> createBookmark(@Valid @RequestBody Bookmark bookmark, @RequestHeader("userId") String userId) {
        bookmark.setUserId(userId);
        return ResponseEntity.ok(bookmarkService.createBookmark(bookmark));
    }

    @GetMapping
    public ResponseEntity<List<Bookmark>> queryBookmarks(
            @RequestHeader("userId") String userId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Long creationDateFrom,
            @RequestParam(required = false) Long creationDateTo,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "100") int limit,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {


        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortBy != null ? sortBy : "creationDate");

        List<Bookmark> bookmarks = bookmarkService.findBookmarksByCriteria(userId, title, description, creationDateFrom, creationDateTo, sort, page, limit);
        return ResponseEntity.ok(bookmarks);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Bookmark> updateBookmark(@PathVariable String id, @RequestHeader("userId") String userId,
                                                   @Valid @RequestBody Bookmark bookmark) {
        return ResponseEntity.ok(bookmarkService.updateBookmark(userId, id, bookmark));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBookmark(@PathVariable String id, @RequestHeader("userId") String userId) {
        bookmarkService.deleteBookmark(userId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBookmark(@PathVariable String id, @RequestHeader("userId") String userId) {
        Optional<Bookmark> bookmark = bookmarkService.getBookmarkById(userId, id);
        return bookmark.isPresent() ? ResponseEntity.ok(bookmark.get()) : ResponseEntity.notFound().build();
    }

    @PutMapping("/{bookmarkId}/move-to/{newFolderId}")
    public ResponseEntity<?> moveBookmark(
            @RequestHeader("userId") String userId,
            @PathVariable String bookmarkId,
            @PathVariable String newFolderId) {
        try {
            bookmarkService.moveBookmark(userId, bookmarkId, newFolderId);
            return ResponseEntity.ok("Bookmark moved successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @GetMapping("/search/tags")
    public ResponseEntity<List<Bookmark>> findBookmarksByTags(@RequestHeader("userId") String userId,
                                                              @RequestParam List<String> tags) {
        List<Bookmark> bookmarks = bookmarkService.findBookmarksByTags(userId, tags);
        return ResponseEntity.ok(bookmarks);
    }
}