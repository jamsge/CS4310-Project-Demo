package com.cs4310FinalProject.demo.Controller;

import com.cs4310FinalProject.demo.FileDeduplicator.FileDeduplicator;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/deduplicate")
public class FileDeduplicatorController {

    @PostMapping("/find")
    public ResponseEntity<Map<String, Object>> findDuplicates(
            @RequestParam("directoryPath") String directoryPath,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "files", required = false) MultipartFile[] directoryFiles,
            HttpServletRequest request) throws Exception {

        Map<String, Object> response = new HashMap<>();
        
        // First check if we have directory files from the directory picker
        List<File> allFiles = new ArrayList<>();
        File directory = new File(directoryPath);;
        if (directoryFiles != null && directoryFiles.length > 0) {
            // Process the files directly from the directory picker
            for (MultipartFile uploadedFile : directoryFiles) {
                // Create a temporary file for each uploaded file
                Path tempFile = Files.createTempFile("uploaded_dir_", null);
                uploadedFile.transferTo(tempFile.toFile());
                allFiles.add(tempFile.toFile());
            }
        } else {
            // Fallback to traditional path-based approach
            if (!directory.exists() || !directory.isDirectory()) {
                response.put("error", "Invalid directory path");
                return ResponseEntity.badRequest().body(response);
            }
            allFiles = FileDeduplicator.getAllFiles(directory);
        }

        // If specific file is provided, find duplicates of that file
        if (file != null && !file.isEmpty()) {
            // Create a temporary file from uploaded file
            Path tempFile = Files.createTempFile("uploaded", null);
            file.transferTo(tempFile.toFile());
            
            // Get the hash of the uploaded file
            String fileHash = FileDeduplicator.getFileHash(tempFile.toFile());
            
            // Find duplicates with the same hash
            List<String> duplicatePaths = new ArrayList<>();
            for (File f : FileDeduplicator.getAllFiles(directory)) {
                if (FileDeduplicator.getFileHash(f).equals(fileHash)) {
                    duplicatePaths.add(f.getAbsolutePath());
                }
            }
            
            response.put("fileHash", fileHash);
            response.put("originalFileName", file.getOriginalFilename());
            response.put("duplicates", duplicatePaths);
            response.put("duplicateCount", duplicatePaths.size());
            
            // Clean up temp file
            Files.deleteIfExists(tempFile);
        } 
        // Find all duplicates in directory
        else {
            Map<String, List<String>> hashToFilePaths = new HashMap<>();
            
            // Collect all files and their hashes
            for (File f : FileDeduplicator.getAllFiles(directory)) {
                String hash = FileDeduplicator.getFileHash(f);
                List<String> paths = hashToFilePaths.getOrDefault(hash, new ArrayList<>());
                paths.add(f.getAbsolutePath());
                hashToFilePaths.put(hash, paths);
            }
            
            // Filter only duplicates (files with same hash)
            List<Map<String, Object>> duplicateGroups = new ArrayList<>();
            for (Map.Entry<String, List<String>> entry : hashToFilePaths.entrySet()) {
                if (entry.getValue().size() > 1) {
                    Map<String, Object> group = new HashMap<>();
                    group.put("hash", entry.getKey());
                    group.put("files", entry.getValue());
                    group.put("count", entry.getValue().size());
                    duplicateGroups.add(group);
                }
            }
            
            response.put("duplicateGroups", duplicateGroups);
            response.put("totalGroups", duplicateGroups.size());
        }
        
        return ResponseEntity.ok(response);
    }
}