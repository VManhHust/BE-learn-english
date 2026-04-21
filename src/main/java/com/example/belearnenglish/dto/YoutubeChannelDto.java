package com.example.belearnenglish.dto;

public record YoutubeChannelDto(
        Long id,
        String channelYoutubeId,
        String channelName,
        String channelImgUrl,
        String channelDescription,
        Long subscriberCount
) {}
