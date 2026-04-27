package com.example.belearnenglish.service;

import com.example.belearnenglish.dto.SaveVocabularyRequest;
import com.example.belearnenglish.dto.VocabularyBankEntryResponse;
import com.example.belearnenglish.entity.User;
import com.example.belearnenglish.entity.VocabularyBank;
import com.example.belearnenglish.exception.DuplicateVocabularyException;
import com.example.belearnenglish.exception.ForbiddenResourceException;
import com.example.belearnenglish.exception.ResourceNotFoundException;
import com.example.belearnenglish.repository.UserRepository;
import com.example.belearnenglish.repository.VocabularyBankRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class VocabularyBankService {

    private final VocabularyBankRepository vocabularyBankRepository;
    private final UserRepository userRepository;

    public VocabularyBankEntryResponse save(Long userId, String word) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        String normalizedWord = word.toLowerCase().trim();

        if (vocabularyBankRepository.existsByUserIdAndWord(userId, normalizedWord)) {
            throw new DuplicateVocabularyException(normalizedWord);
        }

        VocabularyBank entry = VocabularyBank.builder()
                .user(user)
                .word(normalizedWord)
                .addedAt(Instant.now())
                .build();

        VocabularyBank saved = vocabularyBankRepository.save(entry);
        log.info("Saved word '{}' to vocabulary bank for user={}", normalizedWord, userId);

        return new VocabularyBankEntryResponse(saved.getId(), saved.getWord(), saved.getAddedAt());
    }

    @Transactional(readOnly = true)
    public Page<VocabularyBankEntryResponse> findByUser(Long userId, Pageable pageable) {
        return vocabularyBankRepository.findByUserIdOrderByAddedAtDesc(userId, pageable)
                .map(entry -> new VocabularyBankEntryResponse(entry.getId(), entry.getWord(), entry.getAddedAt()));
    }

    public void delete(Long userId, Long entryId) {
        VocabularyBank entry = vocabularyBankRepository.findByIdAndUserId(entryId, userId)
                .orElseThrow(() -> {
                    if (vocabularyBankRepository.existsById(entryId)) {
                        throw new ForbiddenResourceException("You do not have permission to delete this vocabulary entry");
                    }
                    throw new ResourceNotFoundException("Vocabulary entry not found with ID: " + entryId);
                });

        vocabularyBankRepository.delete(entry);
        log.info("Deleted vocabulary entry id={} for user={}", entryId, userId);
    }
}
