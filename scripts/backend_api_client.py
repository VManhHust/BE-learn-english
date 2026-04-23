"""
Backend API client for saving data to BE-learn-english service
"""
import json
import logging
import requests
from typing import List, Dict
from retry_handler import RetryHandler
from exceptions import APIClientError, APIRetryExhaustedError

logger = logging.getLogger(__name__)


class BackendAPIClient:
    """HTTP client for Backend REST API"""
    
    def __init__(self, base_url: str, retry_handler: RetryHandler):
        """
        Initialize API client
        
        Args:
            base_url: Backend API base URL (e.g., http://localhost:8080)
            retry_handler: RetryHandler instance for retry logic
        """
        self.base_url = base_url.rstrip('/')
        self.retry_handler = retry_handler
        self.timeout = 30  # 30 seconds timeout
    
    def _make_request(self, method: str, endpoint: str, data: dict = None) -> requests.Response:
        """
        Make HTTP request with error handling
        
        Args:
            method: HTTP method (GET, POST, etc.)
            endpoint: API endpoint path
            data: Request body data
            
        Returns:
            Response object
            
        Raises:
            APIClientError: On 4xx errors
            Exception: On 5xx errors (will be retried by retry_handler)
        """
        url = f"{self.base_url}{endpoint}"
        headers = {'Content-Type': 'application/json'}
        
        logger.debug(f"Making {method} request to {url}")
        
        try:
            if method == 'POST':
                response = requests.post(
                    url,
                    json=data,
                    headers=headers,
                    timeout=self.timeout
                )
            else:
                response = requests.get(
                    url,
                    headers=headers,
                    timeout=self.timeout
                )
            
            # Handle 4xx client errors (non-retryable)
            if 400 <= response.status_code < 500:
                error_message = response.text
                try:
                    error_json = response.json()
                    error_message = error_json.get('message', error_message)
                except:
                    pass
                
                raise APIClientError(
                    'API_CLIENT_ERROR',
                    error_message,
                    response.status_code
                )
            
            # Handle 5xx server errors (retryable)
            if response.status_code >= 500:
                raise Exception(f"HTTP {response.status_code}: {response.text}")
            
            # Success
            response.raise_for_status()
            return response
            
        except requests.exceptions.Timeout:
            raise TimeoutError(f"Request timeout after {self.timeout} seconds")
        except requests.exceptions.ConnectionError as e:
            raise ConnectionError(f"Connection error: {str(e)}")
        except APIClientError:
            raise
        except Exception as e:
            if 'HTTP 5' in str(e):
                raise
            raise Exception(f"Request failed: {str(e)}")
    
    def save_channel(self, channel_data: dict) -> str:
        """
        Save channel to backend
        
        Args:
            channel_data: YoutubeChannelDto dict from DataTransformer
            
        Returns:
            channel_youtube_id from response
            
        Raises:
            APIClientError: On 4xx errors
            APIRetryExhaustedError: After all retries fail
        """
        logger.info(f"Saving channel: {channel_data.get('channelName')}")
        
        def _save():
            response = self._make_request(
                'POST',
                '/api/v1/topic/youtube/channel/save',
                channel_data
            )
            return response
        
        try:
            response = self.retry_handler.execute_with_retry(_save)
            result = response.json()
            channel_youtube_id = result.get('channelYoutubeId')
            
            logger.info(f"Successfully saved channel: {channel_youtube_id}")
            return channel_youtube_id
            
        except APIClientError as e:
            logger.error(f"Failed to save channel: {e.message}")
            raise
        except APIRetryExhaustedError as e:
            logger.error(f"Failed to save channel after retries: {e.message}")
            raise
    
    def save_exercises(self, channel_youtube_id: str, exercises: List[dict]) -> None:
        """
        Save exercises to backend
        
        Args:
            channel_youtube_id: YouTube channel ID
            exercises: List of SaveExerciseRequest dicts
            
        Raises:
            APIClientError: On 4xx errors (e.g., channel not found)
            APIRetryExhaustedError: After all retries fail
        """
        logger.info(f"Saving {len(exercises)} exercise(s) for channel: {channel_youtube_id}")
        
        def _save():
            response = self._make_request(
                'POST',
                f'/api/v1/topic/youtube/exercises/save/by-channel-youtube-id/{channel_youtube_id}',
                exercises
            )
            return response
        
        try:
            self.retry_handler.execute_with_retry(_save)
            logger.info(f"Successfully saved {len(exercises)} exercise(s)")
            
        except APIClientError as e:
            if e.status_code == 404:
                raise APIClientError(
                    'CHANNEL_NOT_FOUND_IN_DB',
                    f'Channel not found in database: {channel_youtube_id}',
                    404
                )
            logger.error(f"Failed to save exercises: {e.message}")
            raise
        except APIRetryExhaustedError as e:
            logger.error(f"Failed to save exercises after retries: {e.message}")
            raise
    
    def save_modules(self, video_id: str, modules: List[dict]) -> None:
        """
        Save modules to backend
        
        Args:
            video_id: YouTube video ID
            modules: List of SaveModuleRequest dicts
            
        Raises:
            APIClientError: On 4xx errors (e.g., video not found)
            APIRetryExhaustedError: After all retries fail
        """
        logger.info(f"Saving {len(modules)} module(s) for video: {video_id}")
        
        def _save():
            response = self._make_request(
                'POST',
                f'/api/v1/topic/youtube/exercises/content/save/by-video-id/{video_id}',
                modules
            )
            return response
        
        try:
            self.retry_handler.execute_with_retry(_save)
            logger.info(f"Successfully saved {len(modules)} module(s)")
            
        except APIClientError as e:
            if e.status_code == 404:
                raise APIClientError(
                    'VIDEO_NOT_FOUND_IN_DB',
                    f'Video not found in database: {video_id}',
                    404
                )
            logger.error(f"Failed to save modules: {e.message}")
            raise
        except APIRetryExhaustedError as e:
            logger.error(f"Failed to save modules after retries: {e.message}")
            raise
