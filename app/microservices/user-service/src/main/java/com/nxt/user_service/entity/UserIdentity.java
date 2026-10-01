package com.nxt.user_service.entity;

import com.nxt.user_service.auth.AuthProvider;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(
    name="user_identities",
    uniqueConstraints = {
            @UniqueConstraint(columnNames = { "provider", "externalId"})
    }
)
public class UserIdentity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider authProvider;

    @Column(nullable = false)
    private String externalId;

    @Column
    private String email;

    @CreatedDate
    private Instant createdAt;
}