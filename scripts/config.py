"""
Configuration management for transcript API integration
"""
import os
from dataclasses import dataclass
from exceptions import YouTubeAPIError


@dataclass
class Config:
    """Configuration loaded from environment variables"""
    backend_api_url: str
    youtube_api_key: str
    max_retries: int = 3
    retry_base_delay: float = 1.0
    
    @classmethod
    def from_env(cls) -> 'Config':
        """
        Load configuration from environment variables
        
        Returns:
            Config instance with values from environment
            
        Raises:
            YouTubeAPIError: If YOUTUBE_API_KEY is missing
        """
        # Get backend API URL with default
        backend_api_url = os.getenv('BACKEND_API_URL', 'http://localhost:8080')
        
        # Get YouTube API key (with fallback to default)
        youtube_api_key = os.getenv('YOUTUBE_API_KEY', 'AIzaSyDPmfxH6x_OXSWRNcTlMwEqES4lgI7VTdk')
        if not youtube_api_key or youtube_api_key.strip() == '':
            raise YouTubeAPIError(
                'YOUTUBE_API_KEY_MISSING',
                'YouTube API key is required but not provided in environment variables'
            )
        
        # Get optional retry configuration
        max_retries = int(os.getenv('MAX_RETRIES', '3'))
        retry_base_delay = float(os.getenv('RETRY_BASE_DELAY', '1.0'))
        
        return cls(
            backend_api_url=backend_api_url,
            youtube_api_key=youtube_api_key,
            max_retries=max_retries,
            retry_base_delay=retry_base_delay
        )
