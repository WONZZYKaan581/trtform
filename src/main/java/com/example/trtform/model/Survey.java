package com.example.trtform.model;

import jakarta.persistence.*;

@Entity
@Table(name = "surveys")
public class Survey extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "creator_username")
    private String creatorUsername; // Yeni eklenen alan

    // Getter ve Setter metotları
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCreatorUsername() { return creatorUsername; }
    public void setCreatorUsername(String creatorUsername) { this.creatorUsername = creatorUsername; }
}