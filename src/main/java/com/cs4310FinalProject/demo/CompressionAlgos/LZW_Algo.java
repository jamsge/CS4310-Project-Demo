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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LZW_Algo {

    // Method to handle compression and decompression method calls

    public static void compressFile(String inputPath, String outputPath) throws IOException {
        // Read the input file into a byte array
        byte[] inputData = Files.readAllBytes(new File(inputPath).toPath());

        // Call the actual compression algorithm
        List<Integer> compressedData = compress(inputData);

        // Write the compressed data to the output file
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(outputPath))) {
            for (int data : compressedData) {
                out.writeShort(data);
            }
        }
    }

    public static void decompressFile(String inputPath, String outputPath) throws IOException {
        // Read the compressed data from the input file
        List<Integer> compressedData = new ArrayList<>();
        try (DataInputStream in = new DataInputStream(new FileInputStream(inputPath))) {
            while (in.available() > 0) {
                compressedData.add(in.readUnsignedShort());
            }
        }

        // Call the actual decompression algorithm
        byte[] decompressedData = decompress(compressedData);

        // Write the decompressed data to the output file
        Files.write(new File(outputPath).toPath(), decompressedData);
    }


    // Acutal LZW compression and decompression algorithm
    // Algo Following: https://www.cs.columbia.edu/~allen/S14/NOTES/lzw.pdf
    
    private static List<Integer> compress(byte[] input) {
        Map<String, Integer> dictionary = new HashMap<>();
        int dictSize = 256;
    
        // Enter all characters into the dictionary
        for (int i = 0; i < 256; i++) {
            dictionary.put("" + (char) i, i);
        }
    
        // Start with the first character
        String s = "" + (char) (input[0] & 0xFF);
        List<Integer> result = new ArrayList<>();
    
        //  Loop over input from second character onward
        for (int i = 1; i < input.length; i++) {
            char c = (char) (input[i] & 0xFF);
            String sc = s + c;
    
            if (dictionary.containsKey(sc)) {
                s = sc;
            } else {
                result.add(dictionary.get(s));
                dictionary.put(sc, dictSize++);
                s = "" + c;
            }
        }
    
        result.add(dictionary.get(s));
        return result;
    }

    private static byte[] decompress(List<Integer> compressed) {
        Map<Integer, String> dictionary = new HashMap<>();
        int dictSize = 256;
    
        // Enter all characters into the dictionary
        for (int i = 0; i < 256; i++) {
            dictionary.put(i, "" + (char) i);
        }
    
        // Start with the first character
        int priorCode = compressed.remove(0);
        String entry = dictionary.get(priorCode);
        StringBuilder priorString = new StringBuilder(entry);
    
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        output.write(priorString.toString().getBytes(), 0, priorString.length());
    
        // Process remaining codes
        for (int code : compressed) {
            String currentString;
            if (!dictionary.containsKey(code)) {
                currentString = priorString.append(priorString.charAt(0)).toString();
            } else {
                currentString = dictionary.get(code);
            }
            output.write(currentString.getBytes(), 0, currentString.length());
            dictionary.put(dictSize++, priorString.toString() + currentString.charAt(0));
            priorString = new StringBuilder(currentString);
        }
    
        return output.toByteArray();
    }
}
