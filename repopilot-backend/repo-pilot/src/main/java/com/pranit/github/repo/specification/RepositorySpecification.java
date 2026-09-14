package com.pranit.github.repo.specification;

import com.pranit.github.entities.entity.Repository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public final class RepositorySpecification {

    private RepositorySpecification() {
    }

    public static Specification<Repository> searchKeywordAndUserId(final UUID userId, final String keyword) {
        return (root, query, cb) -> {
            final Predicate userPredicate = cb.equal(root.get("userId"), userId);
            if (keyword == null || keyword.trim().isEmpty()) {
                return userPredicate;
            }
            final String likePattern = "%" + keyword.trim().toLowerCase() + "%";
            final Predicate keywordPredicate = cb.or(
                    cb.like(cb.lower(root.get("name")), likePattern),
                    cb.like(cb.lower(root.get("fullName")), likePattern),
                    cb.like(cb.lower(root.get("description")), likePattern),
                    cb.like(cb.lower(root.get("language")), likePattern)
            );
            return cb.and(userPredicate, keywordPredicate);
        };
    }
}
