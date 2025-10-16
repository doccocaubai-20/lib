package com.example.lib.storage;

import org.apache.pdfbox.pdmodel.PDDocument; // Thêm import này
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class StorageService {

    private final Path rootLocation = Paths.get("uploads");

    public StorageService() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Không thể khởi tạo thư mục lưu trữ", e);
        }
    }

    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String generatedFilename = UUID.randomUUID().toString() + fileExtension;

            Path destinationFile = this.rootLocation.resolve(Paths.get(generatedFilename))
                    .normalize().toAbsolutePath();

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }
            
            return generatedFilename;
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi lưu file.", e);
        }
    }

    // ================== PHƯƠNG THỨC BẠN BỊ THIẾU ==================
    /**
     * Đọc file MultipartFile và trả về số trang của file PDF.
     * @param pdfFile File PDF được tải lên
     * @return Số trang, hoặc 0 nếu có lỗi hoặc file không hợp lệ.
     */
    public int getPdfPageCount(MultipartFile pdfFile) {
        if (pdfFile == null || pdfFile.isEmpty()) {
            return 0;
        }

        try (InputStream inputStream = pdfFile.getInputStream()) {
            // Dùng PDDocument của thư viện PDFBox để tải file
            PDDocument document = PDDocument.load(inputStream);
            int pageCount = document.getNumberOfPages();
            document.close();
            return pageCount;
        } catch (IOException e) {
            // In ra lỗi để dễ debug, nhưng không làm crash ứng dụng
            System.err.println("Không thể đọc file PDF để đếm trang: " + e.getMessage());
            return 0; // Trả về 0 nếu không đọc được
        }
    }
}