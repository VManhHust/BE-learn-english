"""
Custom exceptions for transcript API integration
"""


class YouTubeAPIError(Exception):
    """Base exception for YouTube API errors"""
    
    def __init__(self, error_code: str, message: str):
        self.error_code = error_code
        self.message = message
        super().__init__(f"[{error_code}] {message}")


class APIClientError(Exception):
    """Exception for 4xx client errors (non-retryable)"""
    
    def __init__(self, error_code: str, message: str, status_code: int):
        self.error_code = error_code
        self.message = message
        self.status_code = status_code
        super().__init__(f"[{error_code}] HTTP {status_code}: {message}")


class APIRetryExhaustedError(Exception):
    """Exception when all retry attempts fail"""
    
    def __init__(self, message: str, attempts: int):
        self.message = message
        self.attempts = attempts
        super().__init__(f"Retry exhausted after {attempts} attempts: {message}")
