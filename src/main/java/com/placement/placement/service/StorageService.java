package com.placement.placement.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Set;
import java.util.UUID;

@Service
public class StorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    // FIX: added size limit and allowed file types (previously anything could be uploaded)
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "webp", "pdf");

    public String store(MultipartFile file) throws IOException {

        if (file == null || file.isEmpty()) {
            return null;
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File too large. Max 5MB allowed.");
        }

        // FIX: previously used file.getOriginalFilename() directly in the saved path,
        // which allowed path traversal (e.g. a file named "../../evil.png").
        // Now we only keep the extension and generate a fully random name.
        String original = StringUtils.cleanPath(
                file.getOriginalFilename() == null ? "" : file.getOriginalFilename()
        );
        String ext = "";
        int dot = original.lastIndexOf('.');
        if (dot != -1) {
            ext = original.substring(dot + 1).toLowerCase();
        }

        if (!ALLOWED_EXT.contains(ext)) {
            throw new IllegalArgumentException("File type not allowed: " + ext);
        }

        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String fileName = UUID.randomUUID() + "." + ext;
        Path filePath = uploadPath.resolve(fileName).normalize();

        // Safety net: never allow writing outside the uploads folder
        if (!filePath.startsWith(uploadPath)) {
            throw new IllegalArgumentException("Invalid file path.");
        }

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return "/uploads/" + fileName;
    }
}