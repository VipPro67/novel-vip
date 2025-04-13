package com.novel.vippro.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;

    public String uploadJson(String content, String publicId) throws IOException {
        Map<String, Object> options = ObjectUtils.asMap(
                "resource_type", "raw",
                "format", "json",
                "public_id", publicId);

        Map result = cloudinary.uploader().upload(content.getBytes(), options);
        return (String) result.get("url");
    }

    public String getJsonContent(String publicId) throws IOException {
        String url = cloudinary.url()
                .resourceType("raw")
                .format("json")
                .publicId(publicId)
                .generate();
        return url;
    }

    public String uploadAudio(byte[] audioData, String publicId) throws IOException {
        Map<String, Object> options = ObjectUtils.asMap(
                "resource_type", "video",
                "format", "mp3",
                "public_id", publicId);

        Map result = cloudinary.uploader().upload(audioData, options);
        return (String) result.get("url");
    }
}