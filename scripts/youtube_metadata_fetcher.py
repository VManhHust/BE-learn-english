"""
YouTube metadata fetcher using YouTube Data API v3
"""
import re
import logging
from googleapiclient.discovery import build
from googleapiclient.errors import HttpError
from exceptions import YouTubeAPIError

logger = logging.getLogger(__name__)


class YouTubeMetadataFetcher:
    """Fetches metadata from YouTube Data API v3"""
    
    def __init__(self, api_key: str):
        """
        Initialize with YouTube API key
        
        Args:
            api_key: YouTube Data API v3 key
        """
        self.api_key = api_key
        self.youtube = build('youtube', 'v3', developerKey=api_key)
    
    def _convert_duration_to_seconds(self, iso_duration: str) -> int:
        """
        Convert ISO 8601 duration (PT1H2M3S) to seconds
        
        Args:
            iso_duration: ISO 8601 duration string (e.g., PT1H2M10S)
            
        Returns:
            Duration in seconds
        """
        # Parse ISO 8601 duration format: PT#H#M#S
        pattern = r'PT(?:(\d+)H)?(?:(\d+)M)?(?:(\d+)S)?'
        match = re.match(pattern, iso_duration)
        
        if not match:
            return 0
        
        hours = int(match.group(1) or 0)
        minutes = int(match.group(2) or 0)
        seconds = int(match.group(3) or 0)
        
        return hours * 3600 + minutes * 60 + seconds
    
    def fetch_video_metadata(self, video_id: str) -> dict:
        """
        Fetch video metadata from YouTube
        
        Args:
            video_id: YouTube video ID
            
        Returns:
            dict with keys: video_id, title, thumbnail_url, duration_seconds, channel_id
            
        Raises:
            YouTubeAPIError: On API errors (auth, quota, not found)
        """
        try:
            logger.info(f"Fetching video metadata for video_id: {video_id}")
            
            request = self.youtube.videos().list(
                part='snippet,contentDetails',
                id=video_id
            )
            response = request.execute()
            
            if not response.get('items'):
                raise YouTubeAPIError(
                    'VIDEO_NOT_FOUND',
                    f'Video not found or unavailable: {video_id}'
                )
            
            video = response['items'][0]
            snippet = video['snippet']
            content_details = video['contentDetails']
            
            # Get thumbnail URL (prefer medium size)
            thumbnail_url = snippet['thumbnails'].get('medium', {}).get('url', '')
            if not thumbnail_url:
                thumbnail_url = snippet['thumbnails'].get('default', {}).get('url', '')
            
            # Convert duration to seconds
            duration_seconds = self._convert_duration_to_seconds(
                content_details['duration']
            )
            
            metadata = {
                'video_id': video_id,
                'title': snippet['title'],
                'thumbnail_url': thumbnail_url,
                'duration_seconds': duration_seconds,
                'channel_id': snippet['channelId']
            }
            
            logger.info(f"Successfully fetched video metadata: {metadata['title']}")
            return metadata
            
        except HttpError as e:
            error_content = e.content.decode('utf-8') if e.content else str(e)
            
            if e.resp.status == 403:
                if 'quotaExceeded' in error_content:
                    raise YouTubeAPIError(
                        'YOUTUBE_QUOTA_EXCEEDED',
                        'YouTube API quota exceeded'
                    )
                else:
                    raise YouTubeAPIError(
                        'YOUTUBE_AUTH_ERROR',
                        'YouTube API authentication failed'
                    )
            elif e.resp.status == 404:
                raise YouTubeAPIError(
                    'VIDEO_NOT_FOUND',
                    f'Video not found: {video_id}'
                )
            else:
                raise YouTubeAPIError(
                    'VIDEO_UNAVAILABLE',
                    f'Video unavailable: {error_content}'
                )
        except Exception as e:
            raise YouTubeAPIError(
                'VIDEO_UNAVAILABLE',
                f'Failed to fetch video metadata: {str(e)}'
            )
    
    def fetch_channel_metadata(self, channel_id: str) -> dict:
        """
        Fetch channel metadata from YouTube
        
        Args:
            channel_id: YouTube channel ID
            
        Returns:
            dict with keys: channel_id, name, thumbnail_url, description, subscriber_count
            
        Raises:
            YouTubeAPIError: On API errors
        """
        try:
            logger.info(f"Fetching channel metadata for channel_id: {channel_id}")
            
            request = self.youtube.channels().list(
                part='snippet,statistics',
                id=channel_id
            )
            response = request.execute()
            
            if not response.get('items'):
                raise YouTubeAPIError(
                    'CHANNEL_NOT_FOUND',
                    f'Channel not found: {channel_id}'
                )
            
            channel = response['items'][0]
            snippet = channel['snippet']
            statistics = channel.get('statistics', {})
            
            # Get thumbnail URL (prefer medium size)
            thumbnail_url = snippet['thumbnails'].get('medium', {}).get('url', '')
            if not thumbnail_url:
                thumbnail_url = snippet['thumbnails'].get('default', {}).get('url', '')
            
            # Get subscriber count (may be hidden)
            subscriber_count = int(statistics.get('subscriberCount', 0))
            
            metadata = {
                'channel_id': channel_id,
                'name': snippet['title'],
                'thumbnail_url': thumbnail_url,
                'description': snippet.get('description', ''),
                'subscriber_count': subscriber_count
            }
            
            logger.info(f"Successfully fetched channel metadata: {metadata['name']}")
            return metadata
            
        except HttpError as e:
            error_content = e.content.decode('utf-8') if e.content else str(e)
            
            if e.resp.status == 403:
                if 'quotaExceeded' in error_content:
                    raise YouTubeAPIError(
                        'YOUTUBE_QUOTA_EXCEEDED',
                        'YouTube API quota exceeded'
                    )
                else:
                    raise YouTubeAPIError(
                        'YOUTUBE_AUTH_ERROR',
                        'YouTube API authentication failed'
                    )
            elif e.resp.status == 404:
                raise YouTubeAPIError(
                    'CHANNEL_NOT_FOUND',
                    f'Channel not found: {channel_id}'
                )
            else:
                raise YouTubeAPIError(
                    'CHANNEL_NOT_FOUND',
                    f'Failed to fetch channel: {error_content}'
                )
        except Exception as e:
            raise YouTubeAPIError(
                'CHANNEL_NOT_FOUND',
                f'Failed to fetch channel metadata: {str(e)}'
            )
