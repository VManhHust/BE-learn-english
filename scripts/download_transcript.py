#!/usr/bin/env python3
"""
YouTube Transcript Downloader Script
Called from Java to download video transcripts using youtube-transcript-api
Extended to save data to backend database via REST API
"""

import sys
import json
import time
import logging
import traceback
from datetime import datetime
from youtube_transcript_api import YouTubeTranscriptApi

# Import new modules for API integration
from config import Config
from youtube_metadata_fetcher import YouTubeMetadataFetcher
from data_transformer import DataTransformer
from backend_api_client import BackendAPIClient
from retry_handler import RetryHandler
from exceptions import YouTubeAPIError, APIClientError, APIRetryExhaustedError

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler('transcript_api_integration.log'),
        logging.StreamHandler()
    ]
)
logger = logging.getLogger(__name__)


def download_transcript(video_id, languages=['en', 'en-GB', 'en-US']):
    """
    Download transcript for a YouTube video
    
    Args:
        video_id: YouTube video ID
        languages: List of language codes to try
        
    Returns:
        dict: Success response with transcript segments or error response
    """
    try:
        logger.info(f"Starting transcript download for video_id: {video_id}")
        
        # Add small delay to avoid rate limiting
        time.sleep(0.5)
        
        # Create API instance and fetch transcript
        api = YouTubeTranscriptApi()
        transcript_data = api.fetch(video_id, languages=languages)
        
        # Convert to our format
        segments = []
        for idx, entry in enumerate(transcript_data):
            # Version 1.2.4 returns FetchedTranscriptSnippet objects with attributes
            segments.append({
                'id': idx + 1,
                't_start_ms': int(entry.start * 1000),
                't_end_ms': int((entry.start + entry.duration) * 1000),
                'caption': entry.text.strip()
            })
        
        logger.info(f"Successfully downloaded {len(segments)} transcript segments")
        
        return {
            'success': True,
            'video_id': video_id,
            'language': 'en',
            'segments': segments
        }
        
    except Exception as e:
        error_msg = str(e)
        error_msg_lower = error_msg.lower()
        
        logger.error(f"Failed to download transcript: {error_msg}")
        
        # Check for NoTranscriptFound error (no requested language available)
        if 'notranscriptfound' in error_msg_lower or 'no transcripts were found' in error_msg_lower:
            return {
                'success': False,
                'error': 'NO_SUBTITLES_AVAILABLE',
                'message': f'No English subtitles available for video: {video_id}'
            }
        # Check for TranscriptsDisabled error
        elif 'transcriptsdisabled' in error_msg_lower or 'disabled' in error_msg_lower:
            return {
                'success': False,
                'error': 'TRANSCRIPTS_DISABLED',
                'message': f'Transcripts are disabled for video: {video_id}'
            }
        # Check for VideoUnavailable error
        elif 'videounavailable' in error_msg_lower or 'video' in error_msg_lower and 'unavailable' in error_msg_lower:
            return {
                'success': False,
                'error': 'VIDEO_UNAVAILABLE',
                'message': f'Video unavailable: {video_id}'
            }
        else:
            return {
                'success': False,
                'error': 'UNKNOWN_ERROR',
                'message': f'Unexpected error: {str(e)}',
                'traceback': traceback.format_exc()
            }


def save_to_backend(video_id: str, segments: list, config: Config) -> dict:
    """
    Save transcript data to backend database via REST API
    
    Args:
        video_id: YouTube video ID
        segments: Transcript segments
        config: Configuration object
        
    Returns:
        dict with api_save_status containing success/failure information
    """
    start_time = time.time()
    
    try:
        # Initialize components
        logger.info("Initializing API integration components")
        metadata_fetcher = YouTubeMetadataFetcher(config.youtube_api_key)
        retry_handler = RetryHandler(config.max_retries, config.retry_base_delay)
        api_client = BackendAPIClient(config.backend_api_url, retry_handler)
        
        # Step 1: Fetch video metadata (includes channel_id)
        logger.info("Step 1: Fetching video metadata from YouTube")
        video_metadata = metadata_fetcher.fetch_video_metadata(video_id)
        channel_id = video_metadata['channel_id']
        
        # Step 2: Fetch channel metadata
        logger.info("Step 2: Fetching channel metadata from YouTube")
        channel_metadata = metadata_fetcher.fetch_channel_metadata(channel_id)
        
        # Step 3: Transform and save channel data
        logger.info("Step 3: Saving channel data to backend")
        channel_dto = DataTransformer.transform_channel_data(channel_metadata)
        channel_youtube_id = api_client.save_channel(channel_dto)
        
        # Step 4: Transform and save exercise data
        logger.info("Step 4: Saving exercise data to backend")
        exercise_dtos = DataTransformer.transform_exercise_data(video_metadata)
        api_client.save_exercises(channel_youtube_id, exercise_dtos)
        
        # Step 5: Transform and save module data
        logger.info("Step 5: Saving module data to backend")
        module_dtos = DataTransformer.transform_module_data(segments)
        api_client.save_modules(video_id, module_dtos)
        
        elapsed_time = time.time() - start_time
        logger.info(f"Successfully saved all data to backend in {elapsed_time:.2f} seconds")
        
        return {
            'success': True,
            'channel_youtube_id': channel_youtube_id,
            'video_id': video_id,
            'segments_saved': len(segments),
            'elapsed_time_seconds': round(elapsed_time, 2)
        }
        
    except YouTubeAPIError as e:
        elapsed_time = time.time() - start_time
        logger.error(f"YouTube API error: {e.error_code} - {e.message}")
        return {
            'success': False,
            'error_code': e.error_code,
            'message': e.message,
            'step': 'fetch_metadata',
            'elapsed_time_seconds': round(elapsed_time, 2)
        }
        
    except APIClientError as e:
        elapsed_time = time.time() - start_time
        logger.error(f"Backend API error: {e.error_code} - {e.message}")
        return {
            'success': False,
            'error_code': e.error_code,
            'message': e.message,
            'status_code': e.status_code,
            'step': 'save_to_backend',
            'elapsed_time_seconds': round(elapsed_time, 2)
        }
        
    except APIRetryExhaustedError as e:
        elapsed_time = time.time() - start_time
        logger.error(f"Retry exhausted: {e.message}")
        return {
            'success': False,
            'error_code': 'API_RETRY_EXHAUSTED',
            'message': e.message,
            'attempts': e.attempts,
            'step': 'save_to_backend',
            'elapsed_time_seconds': round(elapsed_time, 2)
        }
        
    except Exception as e:
        elapsed_time = time.time() - start_time
        logger.error(f"Unexpected error during API integration: {str(e)}")
        logger.error(traceback.format_exc())
        return {
            'success': False,
            'error_code': 'UNKNOWN_ERROR',
            'message': str(e),
            'traceback': traceback.format_exc(),
            'step': 'save_to_backend',
            'elapsed_time_seconds': round(elapsed_time, 2)
        }


def main():
    """Main entry point"""
    if len(sys.argv) < 2:
        print(json.dumps({
            'success': False,
            'error': 'INVALID_ARGUMENTS',
            'message': 'Usage: python download_transcript.py <video_id>'
        }))
        sys.exit(1)
    
    video_id = sys.argv[1]
    languages = sys.argv[2:] if len(sys.argv) > 2 else ['en', 'en-GB', 'en-US']
    
    logger.info(f"Processing video: {video_id}")
    start_time = time.time()
    
    # Step 1: Download transcript
    result = download_transcript(video_id, languages)
    
    # If transcript download failed, return immediately
    if not result['success']:
        logger.error("Transcript download failed, skipping API integration")
        print(json.dumps(result, indent=2))
        sys.exit(1)
    
    # Step 2: Try to save to backend (optional - don't fail if API integration fails)
    try:
        config = Config.from_env()
        api_save_status = save_to_backend(video_id, result['segments'], config)
        result['api_save_status'] = api_save_status
        
        if not api_save_status['success']:
            logger.warning("API integration failed, but transcript was downloaded successfully")
    except YouTubeAPIError as e:
        # If YouTube API key is missing, just skip API integration
        if e.error_code == 'YOUTUBE_API_KEY_MISSING':
            logger.info("YouTube API key not configured, skipping API integration")
            result['api_save_status'] = {
                'success': False,
                'error_code': 'YOUTUBE_API_KEY_MISSING',
                'message': 'API integration skipped - YouTube API key not configured'
            }
        else:
            raise
    except Exception as e:
        logger.error(f"Failed to initialize API integration: {str(e)}")
        result['api_save_status'] = {
            'success': False,
            'error_code': 'INITIALIZATION_ERROR',
            'message': str(e)
        }
    
    elapsed_time = time.time() - start_time
    logger.info(f"Total processing time: {elapsed_time:.2f} seconds")
    
    print(json.dumps(result, indent=2))
    sys.exit(0 if result['success'] else 1)


if __name__ == '__main__':
    main()
