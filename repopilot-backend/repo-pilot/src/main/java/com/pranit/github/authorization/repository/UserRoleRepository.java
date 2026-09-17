package com.pranit.github.authorization.repository;

import com.pranit.github.entities.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    @Query(value = """
                SELECT CONCAT('ROLE_', r.role_name) AS authority
                FROM auth.user_roles ur
                JOIN auth.roles r
                    ON ur.role_id = r.role_id
                JOIN auth.users u
                    ON ur.user_id = u.user_id
                WHERE u.user_id = :userId
                  AND ur.status = 'ACTIVE'
            """, nativeQuery = true)
    List<String> findAuthoritiesByUserId(@Param("userId") UUID userId);

    boolean existsByUser_UserIdAndRole_RoleId(UUID userId, Long roleId);
}
