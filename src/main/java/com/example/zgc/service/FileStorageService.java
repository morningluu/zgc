package com.example.zgc.service;

import com.example.zgc.config.AppConfig;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path uploadPath;

    public FileStorageService(AppConfig appConfig) {
        this.uploadPath = Paths.get(appConfig.getDir());
        try {
            Files.createDirectories(uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("无法创建上传目录", e);
        }
    }

    public String saveFile(MultipartFile file) {
        if (file.isEmpty()) return null;
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = UUID.randomUUID().toString() + extension;
            Path targetPath = uploadPath.resolve(newFilename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            return "/photos/" + newFilename;
        } catch (IOException e) {
            throw new RuntimeException("文件保存失败", e);
        }
    }
}