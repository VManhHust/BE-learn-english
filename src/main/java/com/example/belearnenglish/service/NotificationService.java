package com.example.belearnenglish.service;

import com.example.belearnenglish.dto.NotificationItemResponse;
import com.example.belearnenglish.dto.NotificationListResponse;
import com.example.belearnenglish.dto.NotificationUnreadCountResponse;
import com.example.belearnenglish.dto.StreakResponse;
import com.example.belearnenglish.entity.User;
import com.example.belearnenglish.entity.UserNotification;
import com.example.belearnenglish.entity.enums.NotificationPriority;
import com.example.belearnenglish.entity.enums.NotificationType;
import com.example.belearnenglish.exception.ResourceNotFoundException;
import com.example.belearnenglish.repository.UserNotificationRepository;
import com.example.belearnenglish.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final int MAX_PAGE_SIZE = 50;

    private final UserNotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final StreakService streakService;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public NotificationListResponse getNotifications(
            Long userId,
            int offset,
            int limit,
            boolean unreadOnly,
            boolean includeExpired
    ) {
        ensureCurrentNotifications(userId);

        int safeLimit = Math.max(1, Math.min(limit, MAX_PAGE_SIZE));
        int safeOffset = Math.max(0, offset);
        int pageNumber = safeOffset / safeLimit;
        Instant now = Instant.now();
        Page<UserNotification> page = includeExpired
                ? notificationRepository.findRecentHistory(
                        userId,
                        unreadOnly,
                        now,
                        now.minusSeconds(30L * 24 * 60 * 60),
                        PageRequest.of(pageNumber, safeLimit)
                )
                : notificationRepository.findActive(
                        userId,
                        unreadOnly,
                        now,
                        PageRequest.of(pageNumber, safeLimit)
                );

        List<NotificationItemResponse> items = page.getContent().stream()
                .map(this::toResponse)
                .toList();
        long unreadCount = notificationRepository.countActiveUnread(userId, now);
        boolean hasMore = safeOffset + items.size() < page.getTotalElements();
        return new NotificationListResponse(items, unreadCount, hasMore);
    }

    public NotificationUnreadCountResponse getUnreadCount(Long userId) {
        ensureCurrentNotifications(userId);
        return new NotificationUnreadCountResponse(
                notificationRepository.countActiveUnread(userId, Instant.now())
        );
    }

    public NotificationItemResponse markAsRead(Long userId, Long notificationId) {
        UserNotification notification = notificationRepository.findById(notificationId)
                .filter(item -> Objects.equals(item.getUser().getId(), userId))
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (notification.getReadAt() == null) {
            Instant now = Instant.now();
            notification.setReadAt(now);
            notification.setUpdatedAt(now);
            notificationRepository.save(notification);
        }
        return toResponse(notification);
    }

    public NotificationUnreadCountResponse markAllAsRead(Long userId) {
        Instant now = Instant.now();
        notificationRepository.markAllActiveAsRead(userId, now);
        return new NotificationUnreadCountResponse(0);
    }

    private void ensureCurrentNotifications(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        Instant now = Instant.now();
        LocalDate today = LocalDate.now(DEFAULT_ZONE);
        Instant endOfDay = today.plusDays(1).atStartOfDay(DEFAULT_ZONE).toInstant();

        refreshVocabularyReminder(user, today, now, endOfDay);
        refreshStreakReminder(user, today, now, endOfDay);
        refreshContinueLessonReminder(user, today, now, endOfDay);
    }

    private void refreshVocabularyReminder(User user, LocalDate today, Instant now, Instant endOfDay) {
        NotificationType type = NotificationType.VOCABULARY_REVIEW_DUE;
        notificationRepository.expireActiveByType(user.getId(), type, now);

        Integer dueCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)::int
                FROM user_vocabulary_word_progress p
                WHERE p.user_id = ?
                  AND (
                    p.status = 'NOT_MASTERED'
                    OR (
                      p.status = 'MASTERED'
                      AND p.review_completed = FALSE
                      AND p.next_review_at <= NOW()
                    )
                  )
                """, Integer.class, user.getId());

        if (dueCount == null || dueCount <= 0) {
            return;
        }

        List<String> sampleWords = jdbcTemplate.queryForList("""
                SELECT w.word
                FROM user_vocabulary_word_progress p
                JOIN vocabulary_word w ON w.id = p.word_id
                WHERE p.user_id = ?
                  AND (
                    p.status = 'NOT_MASTERED'
                    OR (
                      p.status = 'MASTERED'
                      AND p.review_completed = FALSE
                      AND p.next_review_at <= NOW()
                    )
                  )
                ORDER BY p.next_review_at ASC NULLS FIRST, p.updated_at ASC
                LIMIT 3
                """, String.class, user.getId());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("count", dueCount);
        data.put("estimatedMinutes", Math.max(1, (int) Math.ceil(dueCount / 2.0)));
        data.put("sampleWords", sampleWords);

        upsert(
                user,
                type,
                NotificationPriority.HIGH,
                data,
                "/dashboard/vocabulary/review",
                type + ":" + today,
                now,
                endOfDay
        );
    }

    private void refreshStreakReminder(User user, LocalDate today, Instant now, Instant endOfDay) {
        NotificationType type = NotificationType.STREAK_REMINDER;
        notificationRepository.expireActiveByType(user.getId(), type, now);

        StreakResponse streak = streakService.getStatus(user.getId());
        if (Boolean.TRUE.equals(streak.getCheckedInToday()) || !hasLearningActivity(user.getId())) {
            return;
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("currentStreak", streak.getCurrentStreak());
        data.put("totalCheckIns", streak.getTotalCheckIns());

        upsert(
                user,
                type,
                streak.getCurrentStreak() != null && streak.getCurrentStreak() > 0
                        ? NotificationPriority.HIGH
                        : NotificationPriority.NORMAL,
                data,
                null,
                type + ":" + today,
                now,
                endOfDay
        );
    }

    private void refreshContinueLessonReminder(User user, LocalDate today, Instant now, Instant endOfDay) {
        NotificationType type = NotificationType.CONTINUE_LESSON;
        notificationRepository.expireActiveByType(user.getId(), type, now);

        List<LessonReminder> candidates = jdbcTemplate.query("""
                SELECT p.lesson_id, e.title, p.completion_percentage, p.updated_at
                FROM learning_progress p
                JOIN learning_exercise e ON e.id = p.lesson_id
                WHERE p.user_id = ?
                  AND p.is_completed = FALSE
                  AND p.completion_percentage > 0
                  AND e.status = 'PUBLISHED'
                  AND p.updated_at >= NOW() - INTERVAL '30 days'
                ORDER BY p.updated_at DESC
                LIMIT 1
                """, (rs, rowNum) -> new LessonReminder(
                rs.getLong("lesson_id"),
                rs.getString("title"),
                rs.getInt("completion_percentage"),
                rs.getTimestamp("updated_at")
        ), user.getId());

        if (candidates.isEmpty()) {
            return;
        }

        LessonReminder lesson = candidates.getFirst();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("lessonId", lesson.lessonId());
        data.put("lessonTitle", lesson.lessonTitle());
        data.put("completionPercentage", lesson.completionPercentage());

        upsert(
                user,
                type,
                NotificationPriority.NORMAL,
                data,
                "/dashboard/learn/dictation/" + lesson.lessonId(),
                type + ":" + lesson.lessonId() + ":" + today,
                now,
                endOfDay
        );
    }

    private boolean hasLearningActivity(Long userId) {
        Boolean hasActivity = jdbcTemplate.queryForObject("""
                SELECT EXISTS (
                    SELECT 1 FROM user_streak_checkins WHERE user_id = ?
                    UNION ALL
                    SELECT 1 FROM learning_progress WHERE user_id = ?
                    UNION ALL
                    SELECT 1 FROM user_vocabulary_word_progress WHERE user_id = ?
                )
                """, Boolean.class, userId, userId, userId);
        return Boolean.TRUE.equals(hasActivity);
    }

    private void upsert(
            User user,
            NotificationType type,
            NotificationPriority priority,
            Map<String, Object> data,
            String actionUrl,
            String dedupeKey,
            Instant now,
            Instant expiresAt
    ) {
        try {
            jdbcTemplate.update("""
                    INSERT INTO user_notifications (
                        user_id, type, priority, data, action_url, dedupe_key,
                        expires_at, created_at, updated_at
                    )
                    VALUES (?, ?, ?, CAST(? AS jsonb), ?, ?, ?, ?, ?)
                    ON CONFLICT (user_id, dedupe_key)
                    DO UPDATE SET
                        priority = EXCLUDED.priority,
                        data = EXCLUDED.data,
                        action_url = EXCLUDED.action_url,
                        expires_at = EXCLUDED.expires_at,
                        updated_at = EXCLUDED.updated_at
                    """,
                    user.getId(),
                    type.name(),
                    priority.name(),
                    objectMapper.writeValueAsString(data),
                    actionUrl,
                    dedupeKey,
                    Timestamp.from(expiresAt),
                    Timestamp.from(now),
                    Timestamp.from(now)
            );
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to serialize notification data", exception);
        }
    }

    private NotificationItemResponse toResponse(UserNotification notification) {
        return new NotificationItemResponse(
                notification.getId(),
                notification.getType().name(),
                notification.getPriority().name(),
                notification.getData(),
                notification.getActionUrl(),
                notification.getReadAt() == null
                        && (notification.getExpiresAt() == null || notification.getExpiresAt().isAfter(Instant.now())),
                notification.getReadAt(),
                notification.getCreatedAt(),
                notification.getExpiresAt()
        );
    }

    private record LessonReminder(
            long lessonId,
            String lessonTitle,
            int completionPercentage,
            Timestamp updatedAt
    ) {
    }
}
