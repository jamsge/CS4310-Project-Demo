package com.cs4310FinalProject.demo.CompressionAlgos;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class RLE_Algo {

    // public static void main(String[] args) {
    //     Scanner scanner = new Scanner(System.in);
    //     boolean continueRunning = true;

    //     while (continueRunning) {
    //         System.out.print("Enter 'c' to compress, 'd' to decompress, or 'q' to quit: ");
    //         String option = scanner.nextLine();

    //         switch (option.toLowerCase()) {
    //             case "c":
    //                 compressFile(scanner);
    //                 break;
    //             case "d":
    //                 decompressFile(scanner);
    //                 break;
    //             case "q":
    //                 continueRunning = false; // Exit the loop and quit the program
    //                 System.out.println("Exiting the program.");
    //                 break;
    //             default:
    //                 System.out.println("Invalid option. Please enter 'c' for compress, 'd' for decompress, or 'q' to quit.");
    //         }
    //     }
    //     scanner.close();
    // }

    public static void compressFile(String inputFilePath, String outputFilePath) {
        try {
            String inputData = new String(Files.readAllBytes(Paths.get(inputFilePath)));
            String compressedData = compress(inputData);
            Files.write(Paths.get(outputFilePath), compressedData.getBytes());
            System.out.println("File compressed successfully. Output saved to: " + outputFilePath);
        } catch (IOException e) {
            System.err.println("Error reading or writing files: " + e.getMessage());
        }
    }

    public static void decompressFile(String inputFilePath, String outputFilePath) {
        try {
            String compressedData = new String(Files.readAllBytes(Paths.get(inputFilePath)));
            String decompressedData = decompress(compressedData);
            Files.write(Paths.get(outputFilePath), decompressedData.getBytes());
            System.out.println("File decompressed successfully. Output saved to: " + outputFilePath);
        } catch (IOException e) {
            System.err.println("Error reading or writing files: " + e.getMessage());
        }
    }

    public static String compress(String input) {
        StringBuilder compressed = new StringBuilder();
        int count = 1;

        for (int i = 0; i < input.length(); i++) {
            // Check if the next character is the same as the current one
            if (i + 1 < input.length() && input.charAt(i) == input.charAt(i + 1)) {
                count++;
            } else {
                // Append the current character and its count to the compressed string
                compressed.append(compressSpecialCase(input.charAt(i))).append(":").append(count).append(";");
                count = 1; // Reset count for the next character
            }
        }

        return compressed.toString();
    }

    public static String decompress(String input) {
        StringBuilder decompressed = new StringBuilder();

        int i = 0;
        while (i < input.length()) {
            StringBuilder token = new StringBuilder();
            while (i < input.length() && input.charAt(i) != ':') {
                char ch = input.charAt(i);
                if (ch == '\\' && i + 1 < input.length()) {
                    token.append(input.charAt(i + 1)); 
                    i += 2;
                } else {
                    token.append(ch);
                    i++;
                }
            }
            i++; // skip ':'
    
            StringBuilder countStr = new StringBuilder();
            while (i < input.length() && input.charAt(i) != ';') {
                countStr.append(input.charAt(i));
                i++;
            }
            i++; // skip ';'
    
            int count = Integer.parseInt(countStr.toString());
            String tokenStr = token.toString();
    
            for (int j = 0; j < count; j++) {
                decompressed.append(tokenStr);
            }
        }
        return decompressed.toString();
    }

    private static String compressSpecialCase(char ch) {
        if (ch == ':' || ch == ';' || ch == '\\') {
            return "\\" + ch;
        }
        return String.valueOf(ch);
    }
}

