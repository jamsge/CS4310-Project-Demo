package com.cs4310FinalProject.demo.CompressionAlgos;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

public class RLE_Algo {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean continueRunning = true;

        while (continueRunning) {
            System.out.print("Enter 'c' to compress, 'd' to decompress, or 'q' to quit: ");
            String option = scanner.nextLine();

            switch (option.toLowerCase()) {
                case "c":
                    compressFile(scanner);
                    break;
                case "d":
                    decompressFile(scanner);
                    break;
                case "q":
                    continueRunning = false; // Exit the loop and quit the program
                    System.out.println("Exiting the program.");
                    break;
                default:
                    System.out.println("Invalid option. Please enter 'c' for compress, 'd' for decompress, or 'q' to quit.");
            }
        }
        scanner.close();
    }

    private static void compressFile(Scanner scanner) {
        System.out.print("Enter the filepath of the file to compress, and hit Enter: ");
        String inputFilePath = scanner.nextLine();
        String outputFilePath = inputFilePath + ".rle"; // Output file with .rle extension

        try {
            String inputData = new String(Files.readAllBytes(Paths.get(inputFilePath)));
            String compressedData = compress(inputData);
            Files.write(Paths.get(outputFilePath), compressedData.getBytes());
            System.out.println("File compressed successfully. Output saved to: " + outputFilePath);
        } catch (IOException e) {
            System.err.println("Error reading or writing files: " + e.getMessage());
        }
    }

    private static void decompressFile(Scanner scanner) {
        System.out.print("Enter the filepath of the file to decompress, and hit Enter: ");
        String inputFilePath = scanner.nextLine();
        String outputFilePath = inputFilePath.replace(".rle", ""); // Remove .rle extension for output

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
                compressed.append(input.charAt(i));
                compressed.append(count);
                count = 1; // Reset count for the next character
            }
        }

        return compressed.toString();
    }

    public static String decompress(String input) {
        StringBuilder decompressed = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char currentChar = input.charAt(i);
            i++; // Move to the next character which should be the count
            StringBuilder countBuilder = new StringBuilder();

            // Collect all digits for the count
            while (i < input.length() && Character.isDigit(input.charAt(i))) {
                countBuilder.append(input.charAt(i));
                i++;
            }
            i--; // Adjust index since the for loop will increment it

            int count = Integer.parseInt(countBuilder.toString()); // Convert count to integer

            // Append the current character 'count' times to the decompressed string
            for (int j = 0; j < count; j++) {
                decompressed.append(currentChar);
            }
        }
        return decompressed.toString();
    }
}
