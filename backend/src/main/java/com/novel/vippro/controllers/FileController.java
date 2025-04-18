package com.novel.vippro.controllers;

import com.novel.vippro.dto.*;
import com.novel.vippro.payload.response.ControllerResponse;
import com.novel.vippro.services.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/files")
@Tag(name = "File Management", description = "APIs for file upload and management")
@SecurityRequirement(name = "bearerAuth")
public class FileController {

        @Autowired
        private FileService fileService;

        @Operation(summary = "Upload file", description = "Upload a file to the system (images, documents, etc.)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "File uploaded successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid file or file too large"),
                        @ApiResponse(responseCode = "401", description = "Not authenticated"),
                        @ApiResponse(responseCode = "415", description = "Unsupported file type")
        })
        @PostMapping("/upload")
        public ControllerResponse<FileUploadDTO> uploadFile(
                        @Parameter(description = "File to upload", required = true) @RequestParam("file") MultipartFile file,
                        @Parameter(description = "File type (NOVEL_COVER, CHAPTER_IMAGE, etc.)") @RequestParam(required = false) String type) {
                FileUploadDTO uploadResult = fileService.uploadFile(file, type);
                return ControllerResponse.success("File uploaded successfully", uploadResult);
        }

        @Operation(summary = "Upload multiple files", description = "Upload multiple files in a single request")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Files uploaded successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid files or files too large"),
                        @ApiResponse(responseCode = "401", description = "Not authenticated"),
                        @ApiResponse(responseCode = "415", description = "Unsupported file type")
        })
        @PostMapping("/upload/multiple")
        public ControllerResponse<List<FileUploadDTO>> uploadMultipleFiles(
                        @Parameter(description = "Files to upload", required = true) @RequestParam("files") MultipartFile[] files,
                        @Parameter(description = "File type (NOVEL_COVER, CHAPTER_IMAGE, etc.)") @RequestParam(required = false) String type) {
                List<FileUploadDTO> uploadResults = fileService.uploadMultipleFiles(files, type);
                return ControllerResponse.success("Files uploaded successfully", uploadResults);
        }

        @Operation(summary = "Download file", description = "Download a file by its ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "File downloaded successfully"),
                        @ApiResponse(responseCode = "404", description = "File not found")
        })
        @GetMapping("/{id}")
        public Resource downloadFile(
                        @Parameter(description = "File ID", required = true) @PathVariable UUID id) {
                FileDownloadDTO fileDownload = fileService.downloadFile(id);
                return fileDownload.getResource();
        }

        @Operation(summary = "Delete file", description = "Delete a file from the system")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "File deleted successfully"),
                        @ApiResponse(responseCode = "401", description = "Not authenticated"),
                        @ApiResponse(responseCode = "403", description = "Not authorized"),
                        @ApiResponse(responseCode = "404", description = "File not found")
        })
        @DeleteMapping("/{id}")
        public ControllerResponse<Void> deleteFile(
                        @Parameter(description = "File ID", required = true) @PathVariable UUID id) {
                fileService.deleteFile(id);
                return ControllerResponse.success("File deleted successfully", null);
        }

        @Operation(summary = "Get file metadata", description = "Get metadata about a file without downloading it")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "File metadata retrieved successfully"),
                        @ApiResponse(responseCode = "404", description = "File not found")
        })
        @GetMapping("/{id}/metadata")
        public ControllerResponse<FileMetadataDTO> getFileMetadata(
                        @Parameter(description = "File ID", required = true) @PathVariable UUID id) {
                FileMetadataDTO metadata = fileService.getFileMetadata(id);
                return ControllerResponse.success("File metadata retrieved successfully", metadata);
        }

        @Operation(summary = "Update file metadata", description = "Update metadata of an existing file")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "File metadata updated successfully"),
                        @ApiResponse(responseCode = "401", description = "Not authenticated"),
                        @ApiResponse(responseCode = "403", description = "Not authorized"),
                        @ApiResponse(responseCode = "404", description = "File not found")
        })
        @PutMapping("/{id}/metadata")
        public ControllerResponse<FileMetadataDTO> updateFileMetadata(
                        @Parameter(description = "File ID", required = true) @PathVariable UUID id,
                        @Parameter(description = "Updated metadata", required = true) @Valid @RequestBody FileMetadataUpdateDTO metadata) {
                FileMetadataDTO updatedMetadata = fileService.updateFileMetadata(id, metadata);
                return ControllerResponse.success("File metadata updated successfully", updatedMetadata);
        }
}