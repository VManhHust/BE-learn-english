package com.example.belearnenglish.controller;

import com.example.belearnenglish.dto.TopicDto;
import com.example.belearnenglish.service.TopicService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/topics")
public class TopicController {

    private final TopicService topicService;

    public TopicController(TopicService topicService) {
        this.topicService = topicService;
    }

    @GetMapping
    public ResponseEntity<List<TopicDto>> getAllTopics() {
        return ResponseEntity.ok(topicService.getAllTopics());
    }
}
