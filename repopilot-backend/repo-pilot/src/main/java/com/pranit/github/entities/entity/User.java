package com.pranit.github.entities.entity;

import com.pranit.github.audit.entity.AuditEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "users",
        schema = "auth",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_github_id", columnNames = "github_id"),
                @UniqueConstraint(name = "uk_users_github_username", columnNames = "github_username")
        },
        indexes = {
                @Index(name = "idx_users_github_id", columnList = "github_id"),
                @Index(name = "idx_users_github_username", columnList = "github_username")
        }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@SuperBuilder
public class User extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID userId;

    @Column(name = "github_id", nullable = false)
    private Long githubId;

    @Column(name = "github_username", nullable = false, length = 100)
    private String githubUsername;

    @Column(name = "display_name", nullable = false, length = 200)
    private String displayName;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "access_token", nullable = false, columnDefinition = "TEXT")
    private String accessToken;

    @Column(name = "token_scopes", length = 500)
    private String tokenScopes;

    @OneToMany(
            mappedBy = "user",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<UserRole> userRoles = new ArrayList<>();

    @Version
    @Builder.Default
    private Long version = 0L;
}
