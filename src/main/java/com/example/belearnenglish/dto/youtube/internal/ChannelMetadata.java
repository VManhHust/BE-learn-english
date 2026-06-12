package com.example.belearnenglish.dto.youtube.internal;

import com.example.belearnenglish.dto.youtube.ChannelStatistics;
import com.example.belearnenglish.dto.youtube.ThumbnailInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelMetadata {
    private String channelId;
    private String channelHandle;
    private String channelName;
    private String channelDescription;
    private Map<String, ThumbnailInfo> thumbnails;
    private ChannelStatistics statistics;
}
