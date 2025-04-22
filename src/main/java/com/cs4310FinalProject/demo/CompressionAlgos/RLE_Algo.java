import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

public class RLE_Algo {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
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
}