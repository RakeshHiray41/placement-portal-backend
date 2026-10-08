package com.placement.placement.controller;

import com.placement.placement.entity.Album;
import com.placement.placement.repository.AlbumRepository;
import com.placement.placement.service.StorageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/admin/albums")
public class AlbumController {

    private final AlbumRepository albumRepo;
    private final StorageService storageService;

    public AlbumController(AlbumRepository albumRepo, StorageService storageService) {
        this.albumRepo = albumRepo;
        this.storageService = storageService;
    }

    private static final List<String> VALID_CATEGORIES = List.of("PLACEMENT_EVENT", "COMPANY_VISIT");

    private void validate(String title, String category) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (category == null || !VALID_CATEGORIES.contains(category)) {
            throw new IllegalArgumentException("Category must be one of: " + VALID_CATEGORIES);
        }
    }

    // Create a new album with one or more photos
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Album createAlbum(
            @RequestParam String title,
            @RequestParam String category,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate eventDate,
            @RequestParam(required = false) MultipartFile[] photos
    ) throws IOException {

        validate(title, category);

        Album album = new Album();
        album.setTitle(title);
        album.setCategory(category);
        album.setDescription(description);
        album.setEventDate(eventDate);

        List<String> urls = new ArrayList<>();
        if (photos != null) {
            for (MultipartFile photo : photos) {
                if (photo != null && !photo.isEmpty()) {
                    urls.add(storageService.store(photo));
                }
            }
        }
        album.setPhotoUrls(urls);

        return albumRepo.save(album);
    }

    // Public: list all albums (newest first)
    @GetMapping
    public List<Album> getAllAlbums() {
        List<Album> albums = albumRepo.findAll();
        albums.sort((a, b) -> {
            if (a.getEventDate() == null) return 1;
            if (b.getEventDate() == null) return -1;
            return b.getEventDate().compareTo(a.getEventDate());
        });
        return albums;
    }

    // Public: single album with all its photos
    @GetMapping("/{id}")
    public Album getAlbum(@PathVariable Long id) {
        return albumRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Album not found"));
    }

    // Update album details and optionally add more photos
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Album updateAlbum(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam String category,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate eventDate,
            @RequestParam(required = false) MultipartFile[] photos
    ) throws IOException {

        validate(title, category);

        Album album = albumRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Album not found"));

        album.setTitle(title);
        album.setCategory(category);
        album.setDescription(description);
        album.setEventDate(eventDate);

        if (photos != null) {
            for (MultipartFile photo : photos) {
                if (photo != null && !photo.isEmpty()) {
                    album.getPhotoUrls().add(storageService.store(photo));
                }
            }
        }

        return albumRepo.save(album);
    }

    // Remove a single photo from an album (by its URL)
    @DeleteMapping("/{id}/photos")
    public Album deletePhoto(@PathVariable Long id, @RequestParam String photoUrl) {
        Album album = albumRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Album not found"));

        album.getPhotoUrls().remove(photoUrl);
        storageService.delete(photoUrl);

        return albumRepo.save(album);
    }

    // Delete an entire album and all its photo files
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAlbum(@PathVariable Long id) {
        Album album = albumRepo.findById(id).orElse(null);
        if (album == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Album not found with id: " + id);
        }

        for (String url : album.getPhotoUrls()) {
            storageService.delete(url);
        }
        albumRepo.deleteById(id);

        return ResponseEntity.ok("Album deleted with id: " + id);
    }
}