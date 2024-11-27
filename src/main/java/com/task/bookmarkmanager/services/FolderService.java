package com.task.bookmarkmanager.services;

import com.task.bookmarkmanager.model.Folder;
import com.task.bookmarkmanager.repository.BookmarkRepository;
import com.task.bookmarkmanager.repository.FolderRepository;
import com.task.bookmarkmanager.utils.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class FolderService {

    private static final int MAX_TOTAL_FOLDERS = 1000;

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Cacheable("folderCache")
    public Optional<Folder> getFolderById(String userId, String folderId) {
        return folderRepository.findByIdAndUserId(folderId, userId);
    }

    public Folder createFolder(Folder folder) {
        validateFolderLimit();
        if(StringUtils.isEmpty(folder.getParentId())) {
            folder.setParentId("root_" + folder.getUserId());
        }
        if (folderRepository.existsByNameAndUserId(folder.getName(), folder.getUserId())) {
            throw new IllegalStateException("Folder name must be unique.");
        }
        Folder created = folderRepository.save(folder);
        updateParentFolderForUser(created.getUserId(), created.getParentId(), created.getId());
        return created;
    }

    private void updateParentFolderForUser(String userId, String parentId, String id) {
        Folder parentFolder = null;

            parentFolder = folderRepository.findByIdAndUserId(parentId, userId)
                    .orElseThrow(() -> new IllegalStateException("Parent folder not found or access denied"));

        if(parentFolder == null) {
            parentFolder = new Folder();
            parentFolder.setId("root_" + userId);
            parentFolder.setUserId(userId);
            parentFolder.setName("__root__");
        }
        if(parentFolder.getSubfolderIds() == null) {
            parentFolder.setSubfolderIds(Set.of(id));
        } else {
            parentFolder.getSubfolderIds().add(id);
        }
        folderRepository.save(parentFolder);
    }

    public void updateBookmarkFolderForUser(String userId, String folderId, String bookmarkId) {
        Folder folder = folderRepository.findByIdAndUserId(folderId, userId)
                .orElseThrow(() -> new IllegalStateException("Folder not found or access denied"));

        folder.getBookmarkIds().add(bookmarkId);
        folderRepository.save(folder);
    }

    public Folder updateFolder(String userId, String folderId, Folder updatedFolder) {
        Optional<Folder> existingFolderOpt = folderRepository.findByIdAndUserId(folderId, userId);

        if (existingFolderOpt.isEmpty()) {
            throw new IllegalArgumentException("Folder not found or access denied");
        }

        Folder existingFolder = existingFolderOpt.get();


        EntityUtils.copyNonNullProperties(updatedFolder, existingFolder);


        return folderRepository.save(existingFolder);
    }

    public void moveFolder(String userId, String folderId, String newParentFolderId) {
        // Validate the folder to be moved
        Folder folder = folderRepository.findByIdAndUserId(folderId, userId)
                .orElseThrow(() -> new IllegalStateException("Folder not found or access denied"));


        if (newParentFolderId.equals(folderId)) {
            throw new IllegalArgumentException("A folder cannot be its own parent");
        }

        Folder newParentFolder = folderRepository.findByIdAndUserId(newParentFolderId, userId)
                .orElseThrow(() -> new IllegalStateException("New parent folder not found or access denied"));


        if (folder.getParentId() != null) {
            Folder currentParentFolder = folderRepository.findById(folder.getParentId()).orElse(null);
            if (currentParentFolder != null) {
                currentParentFolder.getSubfolderIds().remove(folderId);
                folderRepository.save(currentParentFolder);
            }
        }


        newParentFolder.getSubfolderIds().add(folderId);
        folderRepository.save(newParentFolder);


        folder.setParentId(newParentFolderId);
        folderRepository.save(folder);
    }

    public void deleteFolderRecursively(String userId, String folderId) {
        Folder folder = folderRepository.findByIdAndUserId(folderId, userId)
                .orElseThrow(() -> new IllegalStateException("Folder not found"));

        deleteSubfoldersAndBookmarks(folder);
        unlinkFromParentFolder(userId, folder.getParentId(), folderId);
        folderRepository.deleteById(folderId);

    }

    private void unlinkFromParentFolder(String userId, String parentFolderId, String folderId) {

        Folder parentFolder = folderRepository.findByIdAndUserId(parentFolderId, userId)
                .orElseThrow(() -> new IllegalStateException("Parent folder not found"));

        parentFolder.getSubfolderIds().remove(folderId);
        folderRepository.save(parentFolder);
    }

    private void deleteSubfoldersAndBookmarks(Folder folder) {
        if(!CollectionUtils.isEmpty(folder.getBookmarkIds())) {
            bookmarkRepository.deleteAllById(folder.getBookmarkIds());
        }

        if(CollectionUtils.isEmpty(folder.getSubfolderIds())) {
            return;
        }

        for (String subfolderId : folder.getSubfolderIds()) {
            Optional<Folder> subfolder = folderRepository.findById(subfolderId);
            subfolder.ifPresent(this::deleteSubfoldersAndBookmarks);
            subfolder.ifPresent(f -> folderRepository.deleteById(f.getId()));
        }
    }

    private void validateFolderLimit() {
        long folderCount = folderRepository.count();
        if (folderCount >= MAX_TOTAL_FOLDERS) {
            throw new IllegalStateException("Maximum folder limit reached. Cannot create more folders.");
        }
    }
}