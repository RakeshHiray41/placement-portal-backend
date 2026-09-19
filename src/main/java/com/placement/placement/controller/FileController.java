package com.placement.placement.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final String UPLOAD_FOLDER = "uploads/";

    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            File folder = new File(UPLOAD_FOLDER);
            if (!folder.exists()) {
                folder.mkdirs();
            }

            String fileName = StringUtils.cleanPath(file.getOriginalFilename());
            Path path = Paths.get(UPLOAD_FOLDER + fileName);

            Files.write(path, file.getBytes());

            return ResponseEntity.ok("Uploaded URL: http://localhost:8081/uploads/" + fileName);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    // GET FILE
    @GetMapping("/get/{filename}")
    public ResponseEntity<?> getFile(@PathVariable String filename) {
        Path uploadPath = Paths.get(UPLOAD_FOLDER).toAbsolutePath().normalize();
        Path path = uploadPath.resolve(filename).normalize();

        // FIX: block path traversal, e.g. GET /api/files/get/../../application.properties
        if (!path.startsWith(uploadPath) || !Files.exists(path)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File Not Found");
        }

        try {
            byte[] bytes = Files.readAllBytes(path);
            // FIX: previously no Content-Type was set, so browsers often failed to
            // render images/PDFs correctly (showed broken image icons instead).
            String contentType = Files.probeContentType(path);
            return ResponseEntity.ok()
                    .header("Content-Type", contentType != null ? contentType : "application/octet-stream")
                    .body(bytes);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }
}