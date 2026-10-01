package com.example.product_catalog_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@MappedSuperclass
public class BaseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private Date createdAt;

    @Column(nullable = false)
    private Date lastUpdatedAt;

    @Column(nullable = false)
    private State state;

    @PrePersist
    private void onCreate() {
        this.createdAt = new Date();
        this.lastUpdatedAt = new Date();
    }

    @PreUpdate
    private void onUpdate() {
        this.lastUpdatedAt = new Date();
    }
}