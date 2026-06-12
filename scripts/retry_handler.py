"""
Retry handler with exponential backoff for API calls
"""
import time
import logging
from typing import Callable, Any
from exceptions import APIRetryExhaustedError, APIClientError

logger = logging.getLogger(__name__)


class RetryHandler:
    """Handles retry logic with exponential backoff"""
    
    def __init__(self, max_retries: int = 3, base_delay: float = 1.0):
        """
        Initialize retry handler
        
        Args:
            max_retries: Maximum number of retry attempts
            base_delay: Base delay in seconds (doubles each retry)
        """
        self.max_retries = max_retries
        self.base_delay = base_delay
    
    def should_retry(self, error: Exception) -> bool:
        """
        Determine if error is retryable (5xx, timeout)
        
        Args:
            error: Exception to check
            
        Returns:
            True if error is retryable, False otherwise
        """
        # Don't retry 4xx client errors
        if isinstance(error, APIClientError):
            return False
        
        # Retry on network/connection errors
        if isinstance(error, (ConnectionError, TimeoutError)):
            return True
        
        # Check for HTTP 5xx errors in exception message
        error_str = str(error).lower()
        if '5' in error_str and ('http' in error_str or 'status' in error_str):
            return True
        
        # Check for timeout in error message
        if 'timeout' in error_str:
            return True
        
        return False
    
    def execute_with_retry(self, func: Callable, *args, **kwargs) -> Any:
        """
        Execute function with retry logic
        
        Args:
            func: Function to execute
            *args, **kwargs: Arguments to pass to function
            
        Returns:
            Function return value
            
        Raises:
            APIRetryExhaustedError: After all retries exhausted
            APIClientError: On non-retryable errors (4xx)
        """
        last_error = None
        
        for attempt in range(self.max_retries + 1):
            try:
                return func(*args, **kwargs)
            except Exception as e:
                last_error = e
                
                # Don't retry if error is not retryable
                if not self.should_retry(e):
                    raise
                
                # If this was the last attempt, raise retry exhausted error
                if attempt >= self.max_retries:
                    raise APIRetryExhaustedError(
                        f"Failed after {self.max_retries} retry attempts: {str(e)}",
                        self.max_retries
                    )
                
                # Calculate delay with exponential backoff
                delay = self.base_delay * (2 ** attempt)
                logger.warning(
                    f"Attempt {attempt + 1} failed: {str(e)}. "
                    f"Retrying in {delay} seconds..."
                )
                time.sleep(delay)
        
        # Should never reach here, but just in case
        raise APIRetryExhaustedError(
            f"Failed after {self.max_retries} retry attempts: {str(last_error)}",
            self.max_retries
        )
