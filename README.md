# BE-learn-english

Backend service for English learning application with YouTube transcript integration.

## Features

- User authentication and authorization
- YouTube video transcript download and processing
- Learning exercises and modules management
- Topic-based content organization

## Prerequisites

- Java 21
- Maven 3.6+
- PostgreSQL 12+
- YouTube Data API v3 key (for channel metadata extraction)

## Setup

### 1. Clone the repository

```bash
git clone <repository-url>
cd BE-learn-english
```

### 2. Configure environment variables

Create a `.env` file or set the following environment variables:

```bash
# Database Configuration
DATABASE_URL=jdbc:postgresql://localhost:5432/postgres
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=postgres

# JWT Configuration
JWT_SECRET=your-secret-key-must-be-at-least-32-bytes-long

# Google OAuth (optional)
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
GOOGLE_REDIRECT_URI=http://localhost:8080/api/auth/callback/google

# Frontend Configuration
FRONTEND_URL=http://localhost:3000
ALLOWED_ORIGIN=http://localhost:3000

# YouTube API Configuration (REQUIRED for transcript feature)
YOUTUBE_API_KEY=your-youtube-data-api-v3-key
YOUTUBE_API_TIMEOUT_SECONDS=15
YOUTUBE_CACHE_TTL_MINUTES=60
```

### 3. Obtain YouTube Data API v3 Key

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select an existing one
3. Enable the **YouTube Data API v3**
4. Create credentials (API Key)
5. Copy the API key and set it as `YOUTUBE_API_KEY` environment variable

### 4. Build the project

```bash
mvn clean install
```

### 5. Run the application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## YouTube Transcript API

### Endpoint

**POST** `/api/v1/youtube/transcript/download`

### Description

Downloads transcripts and metadata for one or more YouTube videos.

### Request Body

```json
{
  "urls": [
    "https://www.youtube.com/watch?v=VIDEO_ID_1",
    "https://www.youtube.com/watch?v=VIDEO_ID_2"
  ]
}
```

### Response

```json
{
  "results": [
    {
      "success": true,
      "videoId": "VIDEO_ID_1",
      "video": {
        "title": "Video Title",
        "videoID": "VIDEO_ID_1",
        "length": 514,
        "channelID": "CHANNEL_ID",
        "thumbnailUrl": "https://i.ytimg.com/vi/VIDEO_ID_1/hqdefault.jpg",
        "captions": [
          {
            "id": 0,
            "t_start_ms": 0,
            "t_end_ms": 3160,
            "caption": "Caption text here"
          }
        ]
      },
      "channel": {
        "channelID": "CHANNEL_ID",
        "channelHandle": "@channelhandle",
        "channelName": "Channel Name",
        "channelDescription": "Channel description",
        "channelThumbnail": {
          "default": {
            "url": "https://...",
            "width": 88,
            "height": 88
          },
          "medium": {
            "url": "https://...",
            "width": 240,
            "height": 240
          },
          "high": {
            "url": "https://...",
            "width": 800,
            "height": 800
          }
        },
        "channelStatistics": {
          "viewCount": 3127353823,
          "subscriberCount": 23500000,
          "hiddenSubscriberCount": false,
          "videoCount": 269
        }
      },
      "errorMessage": null
    }
  ],
  "successCount": 1,
  "failureCount": 0
}
```

### Supported URL Formats

- `https://www.youtube.com/watch?v=VIDEO_ID`
- `https://youtu.be/VIDEO_ID`
- `https://www.youtube.com/embed/VIDEO_ID`

### Features

- **Batch Processing**: Process multiple videos in a single request
- **Parallel Execution**: Videos are processed in parallel for better performance
- **Timeout Handling**: 15-second timeout per video (configurable)
- **Channel Metadata Caching**: Channel information is cached for 1 hour to reduce API calls
- **Language Priority**: Automatically selects English subtitles (en, en-GB, en-US)
- **Error Handling**: Continues processing remaining videos even if one fails

### Error Responses

- **400 Bad Request**: Invalid URL format
- **404 Not Found**: Video not found or no subtitles available
- **503 Service Unavailable**: YouTube service temporarily unavailable

## Configuration

All configuration options are available in `application.properties`:

```properties
# YouTube API Configuration
youtube.api.key=${YOUTUBE_API_KEY:}
youtube.api.timeout-seconds=${YOUTUBE_API_TIMEOUT_SECONDS:15}
youtube.api.max-retries=${YOUTUBE_API_MAX_RETRIES:2}
youtube.cache.channel-ttl-minutes=${YOUTUBE_CACHE_TTL_MINUTES:60}
youtube.cache.max-size=${YOUTUBE_CACHE_MAX_SIZE:1000}
youtube.downloader.max-retries=${YOUTUBE_DOWNLOADER_MAX_RETRIES:1}
```

## Development

### Running Tests

```bash
mvn test
```

### Building for Production

```bash
mvn clean package -DskipTests
java -jar target/BE-learn-english-0.0.1-SNAPSHOT.jar
```

## Architecture

The application follows a layered architecture:

- **Controller Layer**: REST API endpoints
- **Service Layer**: Business logic and orchestration
- **Repository Layer**: Data access
- **Configuration Layer**: Bean definitions and application setup

### YouTube Transcript Feature Components

- `YoutubeTranscriptController`: REST API endpoint
- `YoutubeTranscriptService`: Orchestrates the download process
- `VideoMetadataExtractor`: Extracts video metadata using java-youtube-downloader
- `ChannelMetadataExtractor`: Extracts channel metadata using YouTube Data API v3
- `TranscriptDownloader`: Downloads and parses video transcripts
- `TranscriptParser`: Parses SRT format subtitles
- `YoutubeUrlValidator`: Validates and extracts video IDs from URLs

## License

[Your License Here]
