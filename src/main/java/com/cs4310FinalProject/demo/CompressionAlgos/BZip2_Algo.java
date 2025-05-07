package com.cs4310FinalProject.demo.CompressionAlgos;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;

public class BZip2_Algo {

    // Write the compressed data to a file
    public static void compressFile(String inputPath, String outputPath) throws IOException {
        byte[] inputData = Files.readAllBytes(new File(inputPath).toPath());
        byte[] compressedData = compress(inputData);

        try (FileOutputStream fos = new FileOutputStream(outputPath)) {
            fos.write(compressedData);
        }
    }

    // Write the decompressed data to a file
    public static void decompressFile(String inputPath, String outputPath) throws IOException {
        byte[] compressedData = Files.readAllBytes(new File(inputPath).toPath());
        byte[] decompressedData = decompress(compressedData);
        Files.write(new File(outputPath).toPath(), decompressedData);
    }

    // Compress the input data using BZip2
    private static byte[] compress(byte[] input) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        try (BZip2CompressorOutputStream bzip2Stream = new BZip2CompressorOutputStream(outputStream)) {
            bzip2Stream.write(input);
        }
        
        return outputStream.toByteArray();
    }

    // Decompress the BZip2 data
    private static byte[] decompress(byte[] compressed) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(compressed);
        
        try (BZip2CompressorInputStream bzip2Stream = new BZip2CompressorInputStream(inputStream)) {
            byte[] buffer = new byte[4096];
            int n;
            while ((n = bzip2Stream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, n);
            }
        }
        
        return outputStream.toByteArray();
    }
} 