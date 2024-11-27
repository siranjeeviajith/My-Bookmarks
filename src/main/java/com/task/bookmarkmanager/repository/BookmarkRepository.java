package com.task.bookmarkmanager.repository;

import com.task.bookmarkmanager.model.Bookmark;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookmarkRepository extends MongoRepository<Bookmark, String> {

    @Query("{ 'userId': ?0, " +
            "'title': { $regex: ?1, $options: 'i' }, " +
            "'description': { $regex: ?2, $options: 'i' }, " +
            "'createdAt': { $gte: ?3, $lte: ?4 } }")
    List<Bookmark> findBookmarksByCriteria(String userId, String titleRegex, String descriptionRegex,
                                           Long creationDateFrom, Long creationDateTo, Pageable pageable);

    List<Bookmark> findByFolderIdAndUserId(String folderId, String userId);

    Optional<Bookmark> findByIdAndUserId(String id, String userId);


    List<Bookmark> findByUserIdAndTagsIn(String userId, List<String> tags);
}