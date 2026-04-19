package com.example.belearnenglish.service;

import com.example.belearnenglish.dto.CreateRoomRequest;
import com.example.belearnenglish.dto.RoomDto;
import com.example.belearnenglish.dto.SpeakingResponse;
import com.example.belearnenglish.dto.UserStatsDto;
import com.example.belearnenglish.entity.SpeakingRoom;
import com.example.belearnenglish.entity.User;
import com.example.belearnenglish.repository.SpeakingRoomRepository;
import com.example.belearnenglish.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SpeakingService {

    private final SpeakingRoomRepository speakingRoomRepository;
    private final UserRepository userRepository;

    public SpeakingService(SpeakingRoomRepository speakingRoomRepository, UserRepository userRepository) {
        this.speakingRoomRepository = speakingRoomRepository;
        this.userRepository = userRepository;
    }

    public SpeakingResponse getSpeakingData(Long userId) {
        String displayName = userRepository.findById(userId)
                .map(User::getDisplayName)
                .orElse("");
        UserStatsDto stats = new UserStatsDto(0, 0, 0, displayName);
        return new SpeakingResponse(stats, 0);
    }

    public List<RoomDto> getRooms() {
        return speakingRoomRepository.findAll().stream()
                .map(r -> new RoomDto(r.getId(), r.getRoomName(), r.getCurrentMembers(), r.getMaxMembers(), r.isPublic()))
                .toList();
    }

    public RoomDto createRoom(Long userId, CreateRoomRequest request) {
        User creator = userRepository.findById(userId).orElse(null);
        SpeakingRoom room = SpeakingRoom.builder()
                .roomName(request.roomName())
                .maxMembers(request.maxMembers())
                .isPublic(request.isPublic())
                .currentMembers(0)
                .creator(creator)
                .createdAt(LocalDateTime.now())
                .build();
        SpeakingRoom saved = speakingRoomRepository.save(room);
        return new RoomDto(saved.getId(), saved.getRoomName(), saved.getCurrentMembers(), saved.getMaxMembers(), saved.isPublic());
    }
}
