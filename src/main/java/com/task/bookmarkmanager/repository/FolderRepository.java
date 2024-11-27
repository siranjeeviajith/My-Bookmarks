package com.task.bookmarkmanager.repository;

import com.task.bookmarkmanager.model.Folder;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FolderRepository extends MongoRepository<Folder, String> {

    List<Folder> findByUserId(String userId);
    Optional<Folder> findByNameAndUserId(String name, String userId);
    boolean existsByNameAndUserId(String name, String userId);
    Optional<Folder> findByIdAndUserId(String id, String userId);
}