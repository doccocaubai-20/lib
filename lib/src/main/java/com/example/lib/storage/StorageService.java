package com.example.lib.storage;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import jakarta.annotation.PostConstruct;

@Service
public class StorageService {

    private final Path rootLocation;

    // Sử dụng StorageProperties để lấy đường dẫn từ application.properties
    public StorageService(StorageProperties properties) {
        this.rootLocation = Paths.get(properties.getLocation());
    }

    // @PostConstruct đảm bảo thư mục được tạo khi service khởi động
    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(rootLocation);
            System.out.println("Storage location created: " + rootLocation.toAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage location", e);
        }
    }

    // Phương thức Store được cải tiến
    public String store(MultipartFile file) {
        String filename = StringUtils.cleanPath(file.getOriginalFilename());
        try {
            if (file.isEmpty() || filename.contains("..")) {
                throw new RuntimeException("Failed to store file with invalid path: " + filename);
            }
            
            // Tạo tên file duy nhất để tránh trùng lặp
            String extension = StringUtils.getFilenameExtension(filename);
            String storedFilename = UUID.randomUUID().toString() + "." + extension;

            Path destinationFile = this.rootLocation.resolve(storedFilename).normalize().toAbsolutePath();
            
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }
            return storedFilename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file " + filename, e);
        }
    }

    // Phương thức getPdfPageCount giữ nguyên
    public int getPdfPageCount(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream();
             PDDocument document = PDDocument.load(inputStream)) {
            return document.getNumberOfPages();
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    // PHƯƠNG THỨC MỚI: Tải file dưới dạng Resource
    public Resource loadAsResource(String filename) {
        try {
            Path file = rootLocation.resolve(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read file: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Could not read file: " + filename, e);
        }
    }

    // PHƯƠNG THỨC MỚI: Xóa file
    public void delete(String filename) {
        // Kiểm tra xem filename có hợp lệ không
        if (filename == null || filename.isBlank()) {
            return;
        }
        try {
            Path file = rootLocation.resolve(filename);
            Files.deleteIfExists(file);
        } catch (IOException e) {
            System.err.println("Failed to delete file: " + filename + " " + e.getMessage());
        }
    }
}