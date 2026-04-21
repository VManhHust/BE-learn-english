package com.example.belearnenglish.repository;

import com.example.belearnenglish.entity.ExerciseModule;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface ExerciseModuleRepository extends JpaRepository<ExerciseModule, Long> {

    @Query("""
        SELECT m FROM ExerciseModule m
        JOIN m.youtubeModuleExtension ext
        WHERE m.learningExercise.id = :exerciseId
        ORDER BY ext.timeStartMs ASC
        """)
    List<ExerciseModule> findByExerciseIdOrderByTimeStart(Long exerciseId, Pageable pageable);

    @Query("SELECT m.id FROM ExerciseModule m JOIN m.youtubeModuleExtension ext WHERE m.learningExercise.id = :exerciseId ORDER BY ext.timeStartMs ASC")
    List<Long> findIdsByExerciseId(Long exerciseId);
}
