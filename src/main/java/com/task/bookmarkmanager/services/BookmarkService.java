package com.task.bookmarkmanager.services;

import com.task.bookmarkmanager.model.Bookmark;
import com.task.bookmarkmanager.model.Folder;
import com.task.bookmarkmanager.repository.BookmarkRepository;
import com.task.bookmarkmanager.repository.FolderRepository;
import com.task.bookmarkmanager.utils.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Service
public class BookmarkService {

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Autowired
    private FolderService folderService;

    @Autowired
    FolderRepository folderRepository;

    @Cacheable("bookmarkCache")
    public Optional<Bookmark> getBookmarkById(String userId, String bookmarkId) {
        return bookmarkRepository.findByIdAndUserId(bookmarkId, userId);
    }

    public Bookmark createBookmark(Bookmark bookmark) {
        if(StringUtils.isEmpty(bookmark.getFolderId())) {
            bookmark.setFolderId("root_" + bookmark.getUserId());
        }
//        bookmark.setCreationDate(LocalDateTime.now());
        Bookmark created =  bookmarkRepository.save(bookmark);
        folderService.updateBookmarkFolderForUser(created.getUserId(), created.getFolderId(), created.getId());
        return created;
    }

    public Bookmark updateBookmark(String userId, String bookmarkId, Bookmark updatedBookmark) {
        Optional<Bookmark> existingBookmarkOpt = bookmarkRepository.findByIdAndUserId(bookmarkId, userId);

        if (existingBookmarkOpt.isEmpty()) {
            throw new IllegalArgumentException("Bookmark not found or access denied");
        }

        Bookmark existingBookmark = existingBookmarkOpt.get();


        EntityUtils.copyNonNullProperties(updatedBookmark, existingBookmark);


//        existingBookmark.setModifiedAt(System.currentTimeMillis());


        return bookmarkRepository.save(existingBookmark);
    }

    public void moveBookmark(String userId, String bookmarkId, String newFolderId) {

        Bookmark bookmark = bookmarkRepository.findByIdAndUserId(bookmarkId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Bookmark not found or access denied"));


        Folder newFolder = folderService.getFolderById(newFolderId, userId)
                .orElseThrow(() -> new IllegalArgumentException("New folder not found or access denied"));


        if (bookmark.getFolderId() != null) {
            Folder currentFolder = folderRepository.findById(bookmark.getFolderId()).orElse(null);
            if (currentFolder != null) {
                currentFolder.getBookmarkIds().remove(bookmarkId);
                folderRepository.save(currentFolder);
            }
        }


        bookmark.setFolderId(newFolderId);
        bookmarkRepository.save(bookmark);


        newFolder.getBookmarkIds().add(bookmarkId);
        folderRepository.save(newFolder);
    }

    public void deleteBookmark(String userId, String bookmarkId) {
        Optional<Bookmark> bookmark = bookmarkRepository.findByIdAndUserId(bookmarkId, userId);
        bookmark.ifPresent(b -> {
            //b.getFolderId() TODO : remove bookmark from folder
            bookmarkRepository.deleteById(bookmarkId);
            return;
        });
    }

    public List<Bookmark> findBookmarksByTags(String userId, List<String> tags) {
        return bookmarkRepository.findByUserIdAndTagsIn(userId, tags);
    }

    public List<Bookmark> findBookmarksByCriteria(String userId, String title, String description,
                                                  Long creationDateFrom, Long creationDateTo,
                                                  Sort sort, int page, int limit) {

        String titleRegex = (title != null && !title.isBlank()) ? ".*" + title + ".*" : ".*";
        String descriptionRegex = (description != null && !description.isBlank()) ? ".*" + description + ".*" : ".*";


        creationDateFrom = (creationDateFrom != null) ? creationDateFrom : 0L;
        creationDateTo = (creationDateTo != null) ? creationDateTo : System.currentTimeMillis();


        Pageable pageable = PageRequest.of(page, limit, sort);

        return bookmarkRepository.findBookmarksByCriteria(userId, titleRegex, descriptionRegex, creationDateFrom, creationDateTo, pageable);
    }

    public List<Bookmark> getAllBookmarksForFolder(String userId, String folderId) {
        return bookmarkRepository.findByFolderIdAndUserId(folderId, userId);
    }
}