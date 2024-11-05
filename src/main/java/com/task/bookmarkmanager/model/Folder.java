package com.task.bookmarkmanager.model;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Set;

@Data
@Document(collection = "folders")
public class Folder {

    @Id
    private String id;

    private String userId;

    @NotBlank(message = "Folder name is required")
    private String name;
    private String parentId;

    @CreatedDate
    private Long createdAt;

    @LastModifiedDate
    private Long modifiedAt;

    private Set<String> bookmarkIds;
    private Set<String> subfolderIds;
}