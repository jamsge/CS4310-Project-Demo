package com.cs4310FinalProject.demo.Controller;

import com.cs4310FinalProject.demo.FileDeduplicator.FileDeduplicator;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
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
        
        // Determine which files to process based on inputs
        List<File> filesToProcess = new ArrayList<>();
        List<Path> tempFiles = new ArrayList<>();  // Track temp files for cleanup
        
        // Check if directory path exists and is a directory
        File directory = new File(directoryPath);
        boolean useDirectoryPath = directory.exists() && directory.isDirectory();
        
        // Process uploaded directory files if available
        if (directoryFiles != null && directoryFiles.length > 0) {
            for (MultipartFile uploadedFile : directoryFiles) {
                if (!uploadedFile.isEmpty()) {
                    // Create a temporary file for each uploaded file
                    Path tempFile = Files.createTempFile("uploaded_dir_", null);
                    uploadedFile.transferTo(tempFile.toFile());
                    filesToProcess.add(tempFile.toFile());
                    tempFiles.add(tempFile);
                }
            }
        } 
        // If no files uploaded but directory path is valid, use it
        else if (useDirectoryPath) {
            filesToProcess = FileDeduplicator.getAllFiles(directory);
        } 
        // Neither directory path nor uploads are valid
        else {
            response.put("error", "No valid files or directory provided");
            return ResponseEntity.badRequest().body(response);
        }
        
        // If specific file is provided to check against all others
        if (file != null && !file.isEmpty()) {
            // Create a temporary file from uploaded file
            Path tempFile = Files.createTempFile("uploaded_file_", null);
            file.transferTo(tempFile.toFile());
            tempFiles.add(tempFile);
            
            // Get the hash of the uploaded file
            String fileHash = FileDeduplicator.getFileHash(tempFile.toFile());
            
            // Find duplicates with the same hash
            List<String> duplicatePaths = new ArrayList<>();
            for (File f : filesToProcess) {
                String currentHash = FileDeduplicator.getFileHash(f);
                if (currentHash.equals(fileHash)) {
                    duplicatePaths.add(f.getAbsolutePath());
                }
            }
            
            response.put("fileHash", fileHash);
            response.put("originalFileName", file.getOriginalFilename());
            response.put("duplicates", duplicatePaths);
            response.put("duplicateCount", duplicatePaths.size());
        } 
        // Find all duplicates among the files
        else {
            Map<String, List<String>> hashToFilePaths = new HashMap<>();
            
            // Collect all files and their hashes
            for (File f : filesToProcess) {
                try {
                    String hash = FileDeduplicator.getFileHash(f);
                    hashToFilePaths.computeIfAbsent(hash, k -> new ArrayList<>()).add(f.getAbsolutePath());
                } catch (Exception e) {
                    // Log error but continue with other files
                    System.err.println("Error processing file: " + f.getAbsolutePath() + " - " + e.getMessage());
                }
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
        
        // Clean up temp files
        for (Path tempFile : tempFiles) {
            try {
                Files.deleteIfExists(tempFile);
            } catch (Exception e) {
                System.err.println("Failed to delete temp file: " + tempFile);
            }
        }
        
        return ResponseEntity.ok(response);
    }
}