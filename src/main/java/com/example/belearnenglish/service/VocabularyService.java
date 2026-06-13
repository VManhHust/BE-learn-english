package com.example.belearnenglish.service;

import com.example.belearnenglish.dto.VocabularyDeckDetailResponse;
import com.example.belearnenglish.dto.VocabularyDeckDetailResponse.DeckDetailDto;
import com.example.belearnenglish.dto.VocabularyDeckDetailResponse.TopicProgressDto;
import com.example.belearnenglish.dto.VocabularyDeckDetailResponse.WordCardDto;
import com.example.belearnenglish.dto.VocabularyDecksResponse;
import com.example.belearnenglish.dto.VocabularyDecksResponse.VocabularyDeckCardDto;
import com.example.belearnenglish.dto.VocabularyDecksResponse.VocabularyDeckCategoryDto;
import com.example.belearnenglish.dto.VocabularyResponse;
import com.example.belearnenglish.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VocabularyService {

    private final JdbcTemplate jdbcTemplate;

    public VocabularyResponse getVocabularyData(Long userId) {
        Integer totalWords = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM vocabulary_word", Integer.class);
        Integer learned = jdbcTemplate.queryForObject(
            """
            SELECT COUNT(*)
            FROM user_vocabulary_word_progress
            WHERE user_id = ? AND status IN ('GOOD', 'EASY', 'MASTERED')
            """,
            Integer.class,
            userId
        );
        Integer reviewing = jdbcTemplate.queryForObject(
            """
            SELECT COUNT(*)
            FROM user_vocabulary_word_progress
            WHERE user_id = ? AND status IN ('AGAIN', 'HARD')
            """,
            Integer.class,
            userId
        );
        BigDecimal accuracy = jdbcTemplate.queryForObject(
            """
            SELECT COALESCE(ROUND(100.0 * SUM(correct_count) / NULLIF(SUM(review_count), 0), 1), 0)
            FROM user_vocabulary_word_progress
            WHERE user_id = ?
            """,
            BigDecimal.class,
            userId
        );

        return new VocabularyResponse(
            valueOrZero(totalWords),
            valueOrZero(learned),
            valueOrZero(reviewing),
            accuracy == null ? 0.0 : accuracy.doubleValue()
        );
    }

    public VocabularyDecksResponse getDecks(Long userId) {
        List<VocabularyDeckCardDto> decks = jdbcTemplate.query(
            """
            SELECT
                d.id, d.slug, d.title, d.category, d.description, d.cover_color, d.is_premium,
                d.learner_count,
                COUNT(DISTINCT t.id)::int AS topic_count,
                COUNT(DISTINCT w.id)::int AS word_count,
                COUNT(DISTINCT CASE WHEN p.word_id IS NOT NULL THEN w.id END)::int AS learned_words
            FROM vocabulary_deck d
            LEFT JOIN vocabulary_topic t ON t.deck_id = d.id
            LEFT JOIN vocabulary_word w ON w.topic_id = t.id
            LEFT JOIN user_vocabulary_word_progress p ON p.word_id = w.id AND p.user_id = ?
            WHERE d.status = 'PUBLISHED'
            GROUP BY d.id, d.slug, d.title, d.category, d.description, d.cover_color, d.is_premium, d.learner_count, d.sort_order
            ORDER BY d.category, d.sort_order, d.id
            """,
            (rs, rowNum) -> {
                int wordCount = rs.getInt("word_count");
                int learnedWords = rs.getInt("learned_words");
                return new VocabularyDeckCardDto(
                    rs.getLong("id"),
                    rs.getString("slug"),
                    rs.getString("title"),
                    rs.getString("category"),
                    rs.getString("description"),
                    rs.getString("cover_color"),
                    rs.getBoolean("is_premium"),
                    rs.getInt("topic_count"),
                    wordCount,
                    rs.getInt("learner_count"),
                    learnedWords,
                    percentage(learnedWords, wordCount),
                    learnedWords == wordCount && wordCount > 0 ? "Hoàn thành" : learnedWords > 0 ? "Đang học" : "Bắt đầu"
                );
            },
            userId
        );

        Map<String, List<VocabularyDeckCardDto>> grouped = new LinkedHashMap<>();
        for (VocabularyDeckCardDto deck : decks) {
            grouped.computeIfAbsent(deck.category(), ignored -> new ArrayList<>()).add(deck);
        }

        List<VocabularyDeckCategoryDto> categories = grouped.entrySet().stream()
            .map(entry -> new VocabularyDeckCategoryDto(entry.getKey(), entry.getValue().size(), entry.getValue()))
            .toList();

        return new VocabularyDecksResponse(decks.size(), categories);
    }

    public VocabularyDeckDetailResponse getDeckDetail(Long userId, String deckSlug, String topicSlug) {
        return getDeckDetail(userId, deckSlug, topicSlug, null);
    }

    public VocabularyDeckDetailResponse getDeckDetail(Long userId, String deckSlug, String topicSlug, Integer cardNumber) {
        DeckDetailDto deck = findDeck(deckSlug);
        List<TopicProgressDto> topics = findTopics(userId, deck.id());
        if (topics.isEmpty()) {
            return new VocabularyDeckDetailResponse(deck, topics, null, null, 0, 0, 0, 0, 0);
        }

        TopicProgressDto activeTopic = selectActiveTopic(topics, topicSlug);
        int resolvedCardNumber = cardNumber == null
            ? Math.min(activeTopic.currentWordIndex() + 1, activeTopic.totalWords())
            : Math.max(1, Math.min(cardNumber, activeTopic.totalWords()));
        WordCardDto currentCard = cardNumber == null
            ? findCurrentCard(userId, activeTopic.id())
            : findCardAtPosition(userId, activeTopic.id(), resolvedCardNumber - 1);
        resolvedCardNumber = currentCard == null
            ? (activeTopic.completed() ? activeTopic.totalWords() : 0)
            : resolvedCardNumber;
        int totalDeckWords = topics.stream().mapToInt(TopicProgressDto::totalWords).sum();
        int learnedDeckWords = topics.stream().mapToInt(TopicProgressDto::learnedWords).sum();

        return new VocabularyDeckDetailResponse(
            deck,
            topics,
            activeTopic,
            currentCard,
            resolvedCardNumber,
            activeTopic.totalWords(),
            totalDeckWords,
            learnedDeckWords,
            percentage(learnedDeckWords, totalDeckWords)
        );
    }

    @Transactional
    public VocabularyDeckDetailResponse reviewWord(Long userId, Long wordId, String rating) {
        ReviewContext context = findReviewContext(wordId);
        String normalizedRating = normalizeRating(rating);
        boolean correct = List.of("GOOD", "EASY", "MASTERED").contains(normalizedRating);

        if (!List.of("AGAIN", "HARD", "GOOD", "EASY", "MASTERED").contains(normalizedRating)) {
            throw new IllegalArgumentException("Rating không hợp lệ");
        }

        jdbcTemplate.update(
            """
            INSERT INTO user_vocabulary_word_progress
                (user_id, word_id, status, last_rating, review_count, correct_count, ease_factor, next_review_at, learned_at, updated_at)
            VALUES (?, ?, ?, ?, 1, ?, ?, ?, CASE WHEN ? THEN NOW() ELSE NULL END, NOW())
            ON CONFLICT (user_id, word_id) DO UPDATE SET
                status = EXCLUDED.status,
                last_rating = EXCLUDED.last_rating,
                review_count = user_vocabulary_word_progress.review_count + 1,
                correct_count = user_vocabulary_word_progress.correct_count + EXCLUDED.correct_count,
                ease_factor = EXCLUDED.ease_factor,
                next_review_at = EXCLUDED.next_review_at,
                learned_at = CASE
                    WHEN EXCLUDED.learned_at IS NOT NULL THEN COALESCE(user_vocabulary_word_progress.learned_at, EXCLUDED.learned_at)
                    ELSE user_vocabulary_word_progress.learned_at
                END,
                updated_at = NOW()
            """,
            userId,
            wordId,
            normalizedRating,
            normalizedRating,
            correct ? 1 : 0,
            easeFactor(normalizedRating),
            nextReviewAt(normalizedRating),
            correct
        );

        refreshTopicProgress(userId, context.topicId());
        return getDeckDetail(userId, context.deckSlug(), context.topicSlug());
    }

    @Transactional
    public VocabularyDeckDetailResponse resetTopicProgress(Long userId, Long topicId) {
        TopicContext context = findTopicContext(topicId);

        jdbcTemplate.update(
            """
            DELETE FROM user_vocabulary_word_progress
            WHERE user_id = ?
              AND word_id IN (SELECT id FROM vocabulary_word WHERE topic_id = ?)
            """,
            userId,
            topicId
        );
        jdbcTemplate.update(
            "DELETE FROM user_vocabulary_topic_progress WHERE user_id = ? AND topic_id = ?",
            userId,
            topicId
        );

        return getDeckDetail(userId, context.deckSlug(), context.topicSlug());
    }

    private DeckDetailDto findDeck(String deckSlug) {
        List<DeckDetailDto> decks = jdbcTemplate.query(
            """
            SELECT id, slug, title, category, description, cover_color, is_premium
            FROM vocabulary_deck
            WHERE slug = ? AND status = 'PUBLISHED'
            """,
            (rs, rowNum) -> new DeckDetailDto(
                rs.getLong("id"),
                rs.getString("slug"),
                rs.getString("title"),
                rs.getString("category"),
                rs.getString("description"),
                rs.getString("cover_color"),
                rs.getBoolean("is_premium")
            ),
            deckSlug
        );
        if (decks.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy bộ từ vựng");
        }
        return decks.getFirst();
    }

    private List<TopicProgressDto> findTopics(Long userId, Long deckId) {
        return jdbcTemplate.query(
            """
            SELECT
                t.id, t.slug, t.title, t.description, t.thumbnail_url, t.sort_order,
                COUNT(w.id)::int AS total_words,
                COUNT(wp.word_id)::int AS learned_words,
                COUNT(CASE WHEN wp.status IN ('GOOD', 'EASY', 'MASTERED') THEN 1 END)::int AS mastered_words,
                COUNT(wp.word_id)::int AS current_word_index,
                COALESCE(ROUND(100.0 * COUNT(wp.word_id) / NULLIF(COUNT(w.id), 0)), 0)::int AS completion_percentage,
                COUNT(w.id) > 0 AND COUNT(wp.word_id) = COUNT(w.id) AS is_completed
            FROM vocabulary_topic t
            LEFT JOIN vocabulary_word w ON w.topic_id = t.id
            LEFT JOIN user_vocabulary_word_progress wp ON wp.word_id = w.id AND wp.user_id = ?
            WHERE t.deck_id = ?
            GROUP BY t.id, t.slug, t.title, t.description, t.thumbnail_url, t.sort_order
            ORDER BY t.sort_order, t.id
            """,
            (rs, rowNum) -> new TopicProgressDto(
                rs.getLong("id"),
                rs.getString("slug"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getString("thumbnail_url"),
                rs.getInt("sort_order"),
                rs.getInt("total_words"),
                rs.getInt("learned_words"),
                rs.getInt("mastered_words"),
                rs.getInt("current_word_index"),
                rs.getInt("completion_percentage"),
                rs.getBoolean("is_completed")
            ),
            userId,
            deckId
        );
    }

    private WordCardDto findCurrentCard(Long userId, Long topicId) {
        List<WordCardDto> cards = jdbcTemplate.query(
            """
            SELECT
                w.id, w.word, w.part_of_speech, w.ipa_us, w.ipa_uk, w.audio_us_url, w.audio_uk_url,
                w.english_definition, w.vietnamese_definition, w.vietnamese_translation,
                w.example_sentence, w.example_sentence_vi, w.image_url, w.sort_order,
                COALESCE(p.status, 'NEW') AS learning_status
            FROM vocabulary_word w
            LEFT JOIN user_vocabulary_word_progress p ON p.word_id = w.id AND p.user_id = ?
            WHERE w.topic_id = ? AND p.word_id IS NULL
            ORDER BY w.sort_order, w.id
            LIMIT 1
            """,
            this::mapWordCard,
            userId,
            topicId
        );
        return cards.isEmpty() ? null : cards.getFirst();
    }

    private WordCardDto findCardAtPosition(Long userId, Long topicId, int cardIndex) {
        List<WordCardDto> cards = jdbcTemplate.query(
            """
            SELECT
                w.id, w.word, w.part_of_speech, w.ipa_us, w.ipa_uk, w.audio_us_url, w.audio_uk_url,
                w.english_definition, w.vietnamese_definition, w.vietnamese_translation,
                w.example_sentence, w.example_sentence_vi, w.image_url, w.sort_order,
                COALESCE(p.status, 'NEW') AS learning_status
            FROM vocabulary_word w
            LEFT JOIN user_vocabulary_word_progress p ON p.word_id = w.id AND p.user_id = ?
            WHERE w.topic_id = ?
            ORDER BY w.sort_order, w.id
            LIMIT 1 OFFSET ?
            """,
            this::mapWordCard,
            userId,
            topicId,
            Math.max(cardIndex, 0)
        );
        return cards.isEmpty() ? null : cards.getFirst();
    }

    private TopicProgressDto selectActiveTopic(List<TopicProgressDto> topics, String topicSlug) {
        if (topicSlug != null && !topicSlug.isBlank()) {
            return topics.stream()
                .filter(topic -> topic.slug().equals(topicSlug))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chủ đề từ vựng"));
        }

        return topics.stream()
            .filter(topic -> !topic.completed())
            .findFirst()
            .orElse(topics.getFirst());
    }

    private ReviewContext findReviewContext(Long wordId) {
        List<ReviewContext> contexts = jdbcTemplate.query(
            """
            SELECT d.slug AS deck_slug, t.slug AS topic_slug, t.id AS topic_id
            FROM vocabulary_word w
            JOIN vocabulary_topic t ON t.id = w.topic_id
            JOIN vocabulary_deck d ON d.id = t.deck_id
            WHERE w.id = ?
            """,
            (rs, rowNum) -> new ReviewContext(
                rs.getString("deck_slug"),
                rs.getString("topic_slug"),
                rs.getLong("topic_id")
            ),
            wordId
        );
        if (contexts.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy thẻ từ vựng");
        }
        return contexts.getFirst();
    }

    private TopicContext findTopicContext(Long topicId) {
        List<TopicContext> contexts = jdbcTemplate.query(
            """
            SELECT d.slug AS deck_slug, t.slug AS topic_slug
            FROM vocabulary_topic t
            JOIN vocabulary_deck d ON d.id = t.deck_id
            WHERE t.id = ? AND d.status = 'PUBLISHED'
            """,
            (rs, rowNum) -> new TopicContext(
                rs.getString("deck_slug"),
                rs.getString("topic_slug")
            ),
            topicId
        );
        if (contexts.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy nhóm từ vựng");
        }
        return contexts.getFirst();
    }

    private void refreshTopicProgress(Long userId, Long topicId) {
        jdbcTemplate.update(
            """
            INSERT INTO user_vocabulary_topic_progress
                (user_id, topic_id, learned_words, current_word_index, completion_percentage, is_completed, completed_at, updated_at)
            SELECT
                ?,
                ?,
                COUNT(p.word_id)::int,
                COUNT(p.word_id)::int,
                COALESCE(ROUND(100.0 * COUNT(p.word_id) / NULLIF(COUNT(w.id), 0)), 0)::int,
                COUNT(w.id) > 0 AND COUNT(p.word_id) = COUNT(w.id),
                CASE
                    WHEN COUNT(w.id) > 0 AND COUNT(p.word_id) = COUNT(w.id)
                    THEN NOW()
                    ELSE NULL
                END,
                NOW()
            FROM vocabulary_word w
            LEFT JOIN user_vocabulary_word_progress p ON p.word_id = w.id AND p.user_id = ?
            WHERE w.topic_id = ?
            ON CONFLICT (user_id, topic_id) DO UPDATE SET
                learned_words = EXCLUDED.learned_words,
                current_word_index = EXCLUDED.current_word_index,
                completion_percentage = EXCLUDED.completion_percentage,
                is_completed = EXCLUDED.is_completed,
                completed_at = CASE WHEN EXCLUDED.is_completed THEN COALESCE(user_vocabulary_topic_progress.completed_at, EXCLUDED.completed_at) ELSE NULL END,
                updated_at = NOW()
            """,
            userId,
            topicId,
            userId,
            topicId
        );
    }

    private WordCardDto mapWordCard(ResultSet rs, int rowNum) throws SQLException {
        return new WordCardDto(
            rs.getLong("id"),
            rs.getString("word"),
            rs.getString("part_of_speech"),
            rs.getString("ipa_us"),
            rs.getString("ipa_uk"),
            rs.getString("audio_us_url"),
            rs.getString("audio_uk_url"),
            rs.getString("english_definition"),
            rs.getString("vietnamese_definition"),
            rs.getString("vietnamese_translation"),
            rs.getString("example_sentence"),
            rs.getString("example_sentence_vi"),
            rs.getString("image_url"),
            rs.getInt("sort_order"),
            rs.getString("learning_status")
        );
    }

    private String normalizeRating(String rating) {
        return rating.trim().toUpperCase(Locale.ROOT).replace('-', '_');
    }

    private BigDecimal easeFactor(String rating) {
        return switch (rating) {
            case "AGAIN" -> BigDecimal.valueOf(1.30);
            case "HARD" -> BigDecimal.valueOf(1.80);
            case "GOOD" -> BigDecimal.valueOf(2.50);
            case "EASY" -> BigDecimal.valueOf(2.80);
            case "MASTERED" -> BigDecimal.valueOf(3.00);
            default -> BigDecimal.valueOf(2.50);
        };
    }

    private OffsetDateTime nextReviewAt(String rating) {
        OffsetDateTime now = OffsetDateTime.now();
        return switch (rating) {
            case "AGAIN" -> now.plusMinutes(10);
            case "HARD" -> now.plusDays(1);
            case "GOOD" -> now.plusDays(4);
            case "EASY" -> now.plusDays(7);
            case "MASTERED" -> now.plusDays(30);
            default -> now.plusDays(1);
        };
    }

    private int percentage(int part, int total) {
        if (total <= 0) {
            return 0;
        }
        return Math.min(100, Math.round(part * 100f / total));
    }

    private int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }

    private record ReviewContext(String deckSlug, String topicSlug, Long topicId) {
    }

    private record TopicContext(String deckSlug, String topicSlug) {
    }
}
