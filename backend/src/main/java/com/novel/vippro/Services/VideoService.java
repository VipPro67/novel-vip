package com.novel.vippro.Services;

import com.novel.vippro.DTO.Video.CreateVideoDTO;
import com.novel.vippro.DTO.Video.VideoDTO;
import com.novel.vippro.Mapper.Mapper;
import com.novel.vippro.Models.Video;
import com.novel.vippro.Models.Video.VideoPlatform;
import com.novel.vippro.Payload.Response.PageResponse;
import com.novel.vippro.Repository.VideoRepository;
import jakarta.persistence.EntityNotFoundException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class VideoService {

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private Mapper mapper;

    @Transactional
    public VideoDTO createVideo(CreateVideoDTO request) {
        String rawUrl = Optional.ofNullable(request.videoUrl())
                .map(String::trim)
                .filter(StringUtils::hasText)
                .orElseThrow(() -> new IllegalArgumentException("Video URL must not be empty"));

        videoRepository.findByVideoUrl(rawUrl)
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Video with this URL already exists");
                });

        Video video = mapper.CreateVideoDTOtoVideo(request);
        video.setVideoUrl(rawUrl);

        URI uri = toUri(rawUrl);
        VideoPlatform platform = detectPlatform(uri);
        String externalId = platform == VideoPlatform.YOUTUBE ? extractYouTubeId(uri) : null;
        String embedUrl = buildEmbedUrl(platform, rawUrl, externalId);

        if (platform == VideoPlatform.YOUTUBE && !StringUtils.hasText(externalId)) {
            throw new IllegalArgumentException("Unable to extract YouTube video id from the provided URL");
        }

        video.setPlatform(platform);
        video.setExternalId(externalId);
        video.setEmbedUrl(embedUrl);

        Video saved = videoRepository.save(video);
        return mapper.VideoToDTO(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<VideoDTO> getVideos(String search, Pageable pageable) {
        Page<Video> page;
        if (StringUtils.hasText(search)) {
            page = videoRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                    search.trim(), search.trim(), pageable);
        } else {
            page = videoRepository.findAll(pageable);
        }

        return new PageResponse<>(page.map(mapper::VideoToDTO));
    }

    @Transactional(readOnly = true)
    public VideoDTO getVideo(UUID id) {
        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Video not found"));
        return mapper.VideoToDTO(video);
    }

    private URI toUri(String url) {
        String trimmed = url.trim();
        try {
            URI uri = URI.create(trimmed);
            if (uri.getScheme() == null) {
                uri = URI.create("https://" + trimmed);
            }
            return uri;
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid video URL", ex);
        }
    }

    private VideoPlatform detectPlatform(URI uri) {
        String host = Optional.ofNullable(uri.getHost())
                .map(h -> h.toLowerCase(Locale.ROOT))
                .orElse("");

        if (host.contains("youtube.com") || host.contains("youtu.be")) {
            return VideoPlatform.YOUTUBE;
        }
        if (host.contains("facebook.com") || host.contains("fb.watch")) {
            return VideoPlatform.FACEBOOK;
        }

        throw new IllegalArgumentException("Only YouTube and Facebook video URLs are supported");
    }

    private String extractYouTubeId(URI uri) {
        String host = Optional.ofNullable(uri.getHost())
                .map(h -> h.toLowerCase(Locale.ROOT))
                .orElse("");
        String path = Optional.ofNullable(uri.getPath()).orElse("");

        if (host.contains("youtu.be")) {
            return path.startsWith("/") ? path.substring(1) : path;
        }

        if (host.contains("youtube.com")) {
            String query = uri.getQuery();
            if (StringUtils.hasText(query)) {
                for (String param : query.split("&")) {
                    String[] pair = param.split("=", 2);
                    if (pair.length == 2 && "v".equals(pair[0])) {
                        return pair[1];
                    }
                }
            }

            String[] segments = path.split("/");
            for (int i = 0; i < segments.length; i++) {
                String segment = segments[i];
                if ("embed".equals(segment) || "shorts".equals(segment) || "watch".equals(segment)) {
                    if (i + 1 < segments.length) {
                        return segments[i + 1];
                    }
                }
            }
        }

        return null;
    }

    private String buildEmbedUrl(VideoPlatform platform, String originalUrl, String externalId) {
        return switch (platform) {
            case YOUTUBE -> "https://www.youtube.com/embed/" + externalId;
            case FACEBOOK -> "https://www.facebook.com/plugins/video.php?href="
                    + URLEncoder.encode(originalUrl, StandardCharsets.UTF_8)
                    + "&show_text=false&width=500";
        };
    }
}
