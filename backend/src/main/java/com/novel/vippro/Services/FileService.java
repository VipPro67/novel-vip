package com.novel.vippro.Services;

import com.novel.vippro.DTO.File.FileDownloadDTO;
import com.novel.vippro.DTO.File.FileMetadataDTO;
import com.novel.vippro.DTO.File.FileMetadataUpdateDTO;
import com.novel.vippro.Exception.ResourceNotFoundException;
import com.novel.vippro.Mapper.Mapper;
import com.novel.vippro.Models.FileMetadata;
import com.novel.vippro.Repository.FileMetadataRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FileService {

    @Autowired
    @Qualifier("s3FileStorageService")
    private FileStorageService fileStorageService;

    @Autowired
    private FileMetadataRepository fileMetadataRepository;

    @Autowired
    private Mapper mapper;

    @Transactional
    public FileMetadata uploadFile(MultipartFile file, String type) {
        try {
            String publicId = UUID.randomUUID().toString();
            String fileUrl = fileStorageService.uploadFile(file.getBytes(), publicId, file.getContentType());

            FileMetadata metadata = new FileMetadata();
            metadata.setFileName(file.getOriginalFilename());
            metadata.setContentType(file.getContentType());
            metadata.setSize(file.getSize());
            metadata.setType(type);
            metadata.setPublicId(publicId);
            metadata.setFileUrl(fileUrl);
            metadata = fileMetadataRepository.save(metadata);
            return metadata;
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    @Transactional
    public List<FileMetadata> uploadMultipleFiles(MultipartFile[] files, String type) {
        return Arrays.stream(files)
                .map(file -> uploadFile(file, type))
                .collect(Collectors.toList());
    }

    public FileDownloadDTO downloadFile(UUID id) {
        FileMetadata metadata = fileMetadataRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("File", "id", id));

        try {
            byte[] fileContent = fileStorageService.downloadFile(metadata.getPublicId());

            FileDownloadDTO downloadDTO = new FileDownloadDTO();
            downloadDTO.setFileName(metadata.getFileName());
            downloadDTO.setContentType(metadata.getContentType());
            downloadDTO.setResource(new ByteArrayResource(fileContent));

            return downloadDTO;
        } catch (IOException e) {
            throw new RuntimeException("Failed to download file", e);
        }
    }

    @Transactional
    public void deleteFile(UUID id) {
        FileMetadata metadata = fileMetadataRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("File", "id", id));

        try {
            fileStorageService.deleteFile(metadata.getPublicId());
            fileMetadataRepository.delete(metadata);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file", e);
        }
    }

    public FileMetadataDTO getFileMetadata(UUID id) {
        FileMetadata metadata = fileMetadataRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("File", "id", id));

        return mapper.FileMetadataToDTO(metadata);
    }

    @Transactional
    public FileMetadataDTO updateFileMetadata(UUID id, FileMetadataUpdateDTO updateDTO) {
        FileMetadata metadata = fileMetadataRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("File", "id", id));

        metadata.setFileName(updateDTO.getFileName());
        metadata.setType(updateDTO.getType());
        metadata.setUpdatedAt(Instant.now());
        metadata = fileMetadataRepository.save(metadata);
        return mapper.FileMetadataToDTO(metadata);
    }

}
