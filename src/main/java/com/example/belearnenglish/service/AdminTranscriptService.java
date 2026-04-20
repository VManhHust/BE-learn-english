package com.example.belearnenglish.service;

import com.example.belearnenglish.dto.TranscriptSegmentRequest;
import com.example.belearnenglish.dto.TranscriptSegmentResponse;
import com.example.belearnenglish.entity.Lesson;
import com.example.belearnenglish.entity.TranscriptSegmentEntity;
import com.example.belearnenglish.repository.LessonRepository;
import com.example.belearnenglish.repository.TranscriptSegmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminTranscriptService {

    private final TranscriptSegmentRepository transcriptRepo;
    private final LessonRepository lessonRepository;

    public AdminTranscriptService(TranscriptSegmentRepository transcriptRepo, LessonRepository lessonRepository) {
        this.transcriptRepo = transcriptRepo;
        this.lessonRepository = lessonRepository;
    }

    @Transactional
    public List<TranscriptSegmentResponse> saveTranscript(Long lessonId, List<TranscriptSegmentRequest> requests) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found: " + lessonId));

        transcriptRepo.deleteByLessonId(lessonId);

        List<TranscriptSegmentEntity> entities = requests.stream()
                .map(r -> TranscriptSegmentEntity.builder()
                        .lesson(lesson)
                        .segmentIndex(r.segmentIndex())
                        .startTime(r.startTime())
                        .endTime(r.endTime())
                        .text(r.text())
                        .translation(r.translation())
                        .build())
                .toList();

        List<TranscriptSegmentEntity> saved = transcriptRepo.saveAll(entities);
        return saved.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<TranscriptSegmentResponse> getTranscript(Long lessonId) {
        if (!lessonRepository.existsById(lessonId)) {
            throw new IllegalArgumentException("Lesson not found: " + lessonId);
        }
        return transcriptRepo.findByLessonIdOrderBySegmentIndex(lessonId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public void deleteTranscript(Long lessonId) {
        if (!lessonRepository.existsById(lessonId)) {
            throw new IllegalArgumentException("Lesson not found: " + lessonId);
        }
        transcriptRepo.deleteByLessonId(lessonId);
    }

    private TranscriptSegmentResponse toResponse(TranscriptSegmentEntity e) {
        return new TranscriptSegmentResponse(
                e.getId(),
                e.getLesson().getId(),
                e.getSegmentIndex(),
                e.getStartTime(),
                e.getEndTime(),
                e.getText(),
                e.getTranslation()
        );
    }
}
