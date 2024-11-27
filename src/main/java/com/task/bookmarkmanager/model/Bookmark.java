package com.task.bookmarkmanager.model;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "bookmarks")
public class Bookmark {

    @Id
    private String id;

    private String userId;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "URL is required")
    private String url;

    private String description;

    @CreatedDate
    private Long createdAt;
    @LastModifiedDate
    private Long modifiedAt;

    private List<String> tags;

    private String folderId;
}