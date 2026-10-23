package com.socommerce.app.service;

import com.socommerce.app.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Value("${app.upload.public-base-path:/uploads}")
    private String publicBasePath;

    private static final Set<String> ALLOWED_IMAGE_EXT = Set.of("jpg", "jpeg", "png", "webp", "gif");
    private static final Set<String> ALLOWED_VIDEO_EXT = Set.of("mp4", "mov", "webm");
    private static final long MAX_IMAGE_BYTES = 10L * 1024 * 1024;      // 10 MB
    private static final long MAX_VIDEO_BYTES = 200L * 1024 * 1024;     // 200 MB

    public String storeImage(MultipartFile file) {
        return store(file, ALLOWED_IMAGE_EXT, MAX_IMAGE_BYTES, "images");
    }

    public String storeVideo(MultipartFile file) {
        return store(file, ALLOWED_VIDEO_EXT, MAX_VIDEO_BYTES, "videos");
    }

    private String store(MultipartFile file, Set<String> allowedExt, long maxBytes, String subDir) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("No file was provided");
        }
        if (file.getSize() > maxBytes) {
            throw new BadRequestException("File exceeds the maximum allowed size");
        }
        String originalName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "" : file.getOriginalFilename());
        String ext = getExtension(originalName).toLowerCase();
        if (!allowedExt.contains(ext)) {
            throw new BadRequestException("Unsupported file type: " + ext);
        }
        try {
            // Normalize BOTH sides before comparing. Without this, a relative uploadDir like
            // "./uploads" keeps its literal "./" prefix while the resolved target path has it
            // stripped by normalize(), so startsWith() would always return false and reject
            // every upload -- even completely legitimate ones.
            Path targetDir = Path.of(uploadDir, subDir).normalize();
            Files.createDirectories(targetDir);
            String storedName = UUID.randomUUID() + "." + ext;
            Path targetPath = targetDir.resolve(storedName).normalize();
            if (!targetPath.startsWith(targetDir)) {
                throw new BadRequestException("Invalid file path");
            }
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            return publicBasePath + "/" + subDir + "/" + storedName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }

    private String getExtension(String filename) {
        int idx = filename.lastIndexOf('.');
        if (idx < 0 || idx == filename.length() - 1) {
            throw new BadRequestException("File is missing a valid extension");
        }
        return filename.substring(idx + 1);
    }
}
