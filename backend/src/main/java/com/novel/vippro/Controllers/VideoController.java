package com.novel.vippro.Controllers;

import com.novel.vippro.DTO.Video.CreateVideoDTO;
import com.novel.vippro.DTO.Video.VideoDTO;
import com.novel.vippro.Payload.Response.ControllerResponse;
import com.novel.vippro.Payload.Response.PageResponse;
import com.novel.vippro.Services.VideoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/videos")
@Tag(name = "Videos", description = "APIs for managing videos")
@SecurityRequirement(name = "bearerAuth")
public class VideoController {

    @Autowired
    private VideoService videoService;

    @Operation(summary = "Create video", description = "Create a new video entry (admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Video created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid video data"),
            @ApiResponse(responseCode = "403", description = "Not authorized")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ControllerResponse<VideoDTO> createVideo(
            @Parameter(description = "Video details", required = true) @Valid @RequestBody CreateVideoDTO request) {
        VideoDTO created = videoService.createVideo(request);
        return ControllerResponse.success("Video created successfully", created);
    }

    @Operation(summary = "Get videos", description = "Retrieve a paginated list of videos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Videos retrieved successfully")
    })
    @GetMapping
    public ControllerResponse<PageResponse<VideoDTO>> getVideos(
            @Parameter(description = "Search query to filter by title or description")
            @RequestParam(required = false) String search,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page", example = "12")
            @RequestParam(defaultValue = "12") int size,
            @Parameter(description = "Sort field", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction", example = "desc")
            @RequestParam(defaultValue = "desc") String sortDir) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));
        PageResponse<VideoDTO> videos = videoService.getVideos(search, pageable);
        return ControllerResponse.success("Videos retrieved successfully", videos);
    }

    @Operation(summary = "Get video by id", description = "Retrieve a single video by its identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Video retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Video not found")
    })
    @GetMapping("/{id}")
    public ControllerResponse<VideoDTO> getVideo(
            @Parameter(description = "Video identifier", required = true) @PathVariable UUID id) {
        VideoDTO video = videoService.getVideo(id);
        return ControllerResponse.success("Video retrieved successfully", video);
    }
}
