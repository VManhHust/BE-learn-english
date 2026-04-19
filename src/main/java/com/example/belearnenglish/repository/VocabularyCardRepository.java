package com.example.belearnenglish.repository;

import com.example.belearnenglish.entity.VocabularyCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VocabularyCardRepository extends JpaRepository<VocabularyCard, Long> {
}
