package com.novel.vippro.Models;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.novel.vippro.Models.base.BaseEntity;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

@Entity
@Table(name = "file_metadata")
public class FileMetadata extends BaseEntity {
    private String contentType;
    private String publicId;
    private String fileUrl;
    private String fileName;
    private String type;
    private long size;
}