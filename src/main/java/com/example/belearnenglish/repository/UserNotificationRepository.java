package com.example.belearnenglish.repository;

import com.example.belearnenglish.entity.UserNotification;
import com.example.belearnenglish.entity.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {

    Optional<UserNotification> findByUserIdAndDedupeKey(Long userId, String dedupeKey);

    @Query("""
            SELECT n
            FROM UserNotification n
            WHERE n.user.id = :userId
              AND (:unreadOnly = false OR n.readAt IS NULL)
              AND (n.expiresAt IS NULL OR n.expiresAt > :now)
            ORDER BY CASE WHEN n.readAt IS NULL THEN 0 ELSE 1 END, n.createdAt DESC
            """)
    Page<UserNotification> findActive(
            @Param("userId") Long userId,
            @Param("unreadOnly") boolean unreadOnly,
            @Param("now") Instant now,
            Pageable pageable
    );

    @Query("""
            SELECT n
            FROM UserNotification n
            WHERE n.user.id = :userId
              AND n.createdAt >= :cutoff
              AND (
                :unreadOnly = false
                OR (
                  n.readAt IS NULL
                  AND (n.expiresAt IS NULL OR n.expiresAt > :now)
                )
              )
            ORDER BY
              CASE
                WHEN n.readAt IS NULL
                  AND (n.expiresAt IS NULL OR n.expiresAt > :now)
                THEN 0
                ELSE 1
              END,
              n.createdAt DESC
            """)
    Page<UserNotification> findRecentHistory(
            @Param("userId") Long userId,
            @Param("unreadOnly") boolean unreadOnly,
            @Param("now") Instant now,
            @Param("cutoff") Instant cutoff,
            Pageable pageable
    );

    @Query("""
            SELECT COUNT(n)
            FROM UserNotification n
            WHERE n.user.id = :userId
              AND n.readAt IS NULL
              AND (n.expiresAt IS NULL OR n.expiresAt > :now)
            """)
    long countActiveUnread(@Param("userId") Long userId, @Param("now") Instant now);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE UserNotification n
            SET n.expiresAt = :now, n.updatedAt = :now
            WHERE n.user.id = :userId
              AND n.type = :type
              AND (n.expiresAt IS NULL OR n.expiresAt > :now)
            """)
    void expireActiveByType(
            @Param("userId") Long userId,
            @Param("type") NotificationType type,
            @Param("now") Instant now
    );

    @Modifying
    @Query("""
            UPDATE UserNotification n
            SET n.readAt = :now, n.updatedAt = :now
            WHERE n.user.id = :userId
              AND n.readAt IS NULL
              AND (n.expiresAt IS NULL OR n.expiresAt > :now)
            """)
    int markAllActiveAsRead(@Param("userId") Long userId, @Param("now") Instant now);
}
