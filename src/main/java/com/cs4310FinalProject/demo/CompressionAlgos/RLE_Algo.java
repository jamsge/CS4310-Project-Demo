package com.cs4310FinalProject.demo.CompressionAlgos;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class RLE_Algo {

    // Write the compressed data to a file
    public static void compressFile(String inputPath, String outputPath) throws IOException {
        byte[] inputData = Files.readAllBytes(new File(inputPath).toPath());
        List<Byte> compressedData = compress(inputData);

        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(outputPath))) {
            for (byte data : compressedData) {
                out.writeByte(data);
            }
        }
    }

    // Write the decompressed data to a file
    public static void decompressFile(String inputPath, String outputPath) throws IOException {
        List<Byte> compressedData = new ArrayList<>();

        try (DataInputStream in = new DataInputStream(new FileInputStream(inputPath))) {
            while (in.available() > 0) {
                compressedData.add(in.readByte());
            }
        }

        byte[] decompressedData = decompress(compressedData);
        Files.write(new File(outputPath).toPath(), decompressedData);
    }

    // Compress the input data using RLE
    private static List<Byte> compress(byte[] input) {
        List<Byte> result = new ArrayList<>();
        int count = 1;

        for (int i = 0; i < input.length; i++) {
            // Count occurrences of the same byte
            while (i + 1 < input.length && input[i] == input[i + 1]) {
                count++;
                i++;
            }
            // Store the byte and its count
            result.add(input[i]);
            result.add((byte) count);
            count = 1; // Reset count for the next character
        }

        return result;
    }

    // Decompress the RLE data
    private static byte[] decompress(List<Byte> compressed) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        for (int i = 0; i < compressed.size(); i += 2) {
            byte value = compressed.get(i);
            byte count = compressed.get(i + 1);

            // Write the value 'count' times to the output
            for (int j = 0; j < count; j++) {
                output.write(value);
            }
        }

        return output.toByteArray();
    }
}
