package com.cs4310FinalProject.demo.FileDeduplicator;

import java.io.*;
import java.security.*;
import java.util.*;

public class FileDeduplicator {
    public static String getFileHash(File file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream is = new FileInputStream(file)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
        }
        byte[] hashBytes = digest.digest();
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }
    
    public static void findDuplicates(String dirPath) throws Exception {
        File dir = new File(dirPath);
        Map<String, List<File>> hashToFiles = new HashMap<>();

        // Recursively scan files
        for (File file : getAllFiles(dir)) {
            String hash = getFileHash(file);
            hashToFiles.computeIfAbsent(hash, k -> new ArrayList<>()).add(file);
        }

        // Print duplicates
        for (List<File> duplicates : hashToFiles.values()) {
            if (duplicates.size() > 1) {
                System.out.println("Duplicates (" + duplicates.size() + " files):");
                for (File file : duplicates) {
                    System.out.println("  " + file.getAbsolutePath());
                }
            }
        }
    }

    private static List<File> getAllFiles(File dir) {
        List<File> fileList = new ArrayList<>();
        if (!dir.isDirectory()) return fileList;

        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    fileList.addAll(getAllFiles(file));
                } else {
                    fileList.add(file);
                }
            }
        }
        return fileList;
    }
}