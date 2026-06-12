"""
Data transformer for converting between YouTube and Backend API formats
"""
import logging
from typing import List, Dict

logger = logging.getLogger(__name__)


class DataTransformer:
    """Transforms data between YouTube and Backend API formats"""
    
    @staticmethod
    def transform_channel_data(youtube_metadata: dict) -> dict:
        """
        Transform YouTube channel metadata to YoutubeChannelDto format
        
        Args:
            youtube_metadata: Dict from YouTubeMetadataFetcher.fetch_channel_metadata
            
        Returns:
            dict matching YoutubeChannelDto structure (without id field)
        """
        return {
            'channelYoutubeId': youtube_metadata['channel_id'],
            'channelName': youtube_metadata['name'],
            'channelImgUrl': youtube_metadata['thumbnail_url'],
            'channelDescription': youtube_metadata['description'],
            'subscriberCount': youtube_metadata['subscriber_count']
        }
    
    @staticmethod
    def transform_exercise_data(youtube_metadata: dict) -> List[dict]:
        """
        Transform YouTube video metadata to SaveExerciseRequest format
        
        Args:
            youtube_metadata: Dict from YouTubeMetadataFetcher.fetch_video_metadata
            
        Returns:
            List containing single SaveExerciseRequest dict
        """
        return [{
            'videoId': youtube_metadata['video_id'],
            'title': youtube_metadata['title'],
            'thumbnailUrl': youtube_metadata['thumbnail_url'],
            'durationSeconds': youtube_metadata['duration_seconds'],
            'vocabularyLevel': None
        }]
    
    @staticmethod
    def transform_module_data(transcript_segments: List[dict]) -> List[dict]:
        """
        Transform transcript segments to SaveModuleRequest format
        
        Args:
            transcript_segments: List of segments from download_transcript
                Each segment has: id, t_start_ms, t_end_ms, caption
            
        Returns:
            List of SaveModuleRequest dicts (without id field)
        """
        return [
            {
                'timeStartMs': segment['t_start_ms'],
                'timeEndMs': segment['t_end_ms'],
                'content': segment['caption']
            }
            for segment in transcript_segments
        ]
