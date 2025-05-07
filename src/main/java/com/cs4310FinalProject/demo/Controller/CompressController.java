package com.cs4310FinalProject.demo.Controller;

import com.cs4310FinalProject.demo.CompressionAlgos.LZW_Algo;
import com.cs4310FinalProject.demo.CompressionAlgos.RLE_Algo;
import com.cs4310FinalProject.demo.CompressionAlgos.LZ77_Algo;
import com.cs4310FinalProject.demo.CompressionAlgos.BZip2_Algo;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@RestController
@RequestMapping("/compress")
public class CompressController {

    @PostMapping("/compare")
    public ResponseEntity<Map<String, Object>> compare(
            @RequestParam("file") MultipartFile file,
            @RequestParam("algorithms") String algorithmList,
            HttpServletRequest request) throws IOException {

        // create a temporary file that copies the original data
        Path original = Files.createTempFile("original", null);
        file.transferTo(original.toFile());

        // get the base name and extension of the original file
        String currFile = Paths.get(file.getOriginalFilename()).getFileName().toString();
        String baseName = currFile.substring(0, currFile.lastIndexOf('.'));
        String extension = currFile.substring(currFile.lastIndexOf('.'));

        // handle which algorithms was selected from the frontend
        List<String> selectedAlgos = Arrays.asList(algorithmList.split(","));
        List<Map<String, Object>> results = new ArrayList<>();

        // array for csv file
        List<String> csvLines = new ArrayList<>();
        csvLines.add(
                "Algorithm,Run,OriginalSize,CompressedSize,CompressionTime(ms),DecompressionTime(ms),CompressionRatio(%),CompressionSavings(%),CompressionSpeedRatio(Perc/ms),Correctness");

        for (String algo : selectedAlgos) {
            String ext = switch (algo) {
                case "LZW" -> ".lzw";
                case "RLE" -> ".rle";
                case "BZIP2" -> ".bz2";
                case "LZ77" -> ".lz77";
                default -> throw new IllegalArgumentException("Unknown algorithm: " + algo);
            };

            long totalCompressionTime = 0;
            long totalDecompressionTime = 0;
            double totalCompressionRatio = 0;
            double totalCompressionSavings = 0;
            double totalCompressionSpeedRatio = 0;
            int totalErrors = 0;
            long originalSize = 0;
            long compressedSize = 0;

            // loop 50 times
            for (int run = 1; run <= 50; run++) {
                // create temporary files for compressed and decompressed data (as of rn, these
                // 2 files has nothing written in them)
                Path compressed = Path.of(System.getProperty("java.io.tmpdir"),
                        baseName + "_" + algo + "_compressed_" + run + ext);
                Path decompressed = Path.of(System.getProperty("java.io.tmpdir"),
                        baseName + "_" + algo + "_decompressed_" + run + extension);

                // remove any existing files with the same name before creating new ones
                Files.deleteIfExists(compressed);
                Files.deleteIfExists(decompressed);
                Files.createFile(compressed);
                Files.createFile(decompressed);

                long startComp = System.currentTimeMillis();
                switch (algo) {

                    // Calls the algorithm and pass the original and compressed file paths

                    // case "LZW" -> LZW_Algo.compress();
                    case "LZW" -> LZW_Algo.compressFile(original.toString(), compressed.toString());

                    // case "RLE" -> RLE_Algo.compress();
                    case "RLE" -> RLE_Algo.compressFile(original.toString(), compressed.toString());

                    // case "BZIP2" -> Bzip2_Algo.compress();
                    case "BZIP2" -> BZip2_Algo.compressFile(original.toString(), compressed.toString());

                    // case "LZ77" -> LZ77_Algo.compress();
                    case "LZ77" -> LZ77_Algo.compressFile(original.toString(), compressed.toString());
                }
                long endComp = System.currentTimeMillis();

                long startDecomp = System.currentTimeMillis();
                switch (algo) {

                    // Calls the algorithm and pass the compressed and decompressed file paths

                    // case "LZW" -> LZW_Algo.decompress();
                    case "LZW" -> LZW_Algo.decompressFile(compressed.toString(), decompressed.toString());

                    // case "RLE" -> RLE_Algo.decompressFile();
                    case "RLE" -> RLE_Algo.decompressFile(compressed.toString(), decompressed.toString());

                    // case "BZIP2" -> Bzip2_Algo.decompressFile();
                    case "BZIP2" -> BZip2_Algo.decompressFile(compressed.toString(), decompressed.toString());

                    // case "LZ77" -> LZ77_Algo.decompressFile();
                    case "LZ77" -> LZ77_Algo.decompressFile(compressed.toString(), decompressed.toString());
                }
                long endDecomp = System.currentTimeMillis();

                // calculate the results

                originalSize = Files.size(original);
                compressedSize = Files.size(compressed);

                long compressionTime = endComp - startComp;
                long decompressionTime = endDecomp - startDecomp;
                double compressionRatio = ((double) compressedSize / originalSize) * 100;
                double compressionSavings = (1.0 - ((double) compressedSize / originalSize)) * 100;
                double compressionSpeedRatio = compressionRatio / compressionTime;

                boolean identical = filesAreIdentical(original, decompressed);
                String correctness = identical ? "Pass" : "Fail";

                if (!identical)
                    totalErrors++;

                totalCompressionTime += compressionTime;
                totalDecompressionTime += decompressionTime;
                totalCompressionRatio += compressionRatio;
                totalCompressionSavings += compressionSavings;
                totalCompressionSpeedRatio += compressionSpeedRatio;

                // write results to csv
                csvLines.add(String.format(Locale.US,
                        "%s,%d,%d,%d,%d,%d,%.2f,%.2f,%.4f,%s",
                        algo, run, originalSize, compressedSize,
                        compressionTime, decompressionTime,
                        compressionRatio, compressionSavings,
                        compressionSpeedRatio, correctness));
            }

            // put results into a map
            Map<String, Object> avgResult = new HashMap<>();
            avgResult.put("algorithm", algo);
            avgResult.put("originalSize", originalSize);
            avgResult.put("compressedSize", compressedSize);
            avgResult.put("avgCompressionTime", totalCompressionTime / 100);
            avgResult.put("avgDecompressionTime", totalDecompressionTime / 100);
            avgResult.put("avgCompressionRatio", String.format("%.2f", totalCompressionRatio / 100));
            avgResult.put("avgCompressionSavings", String.format("%.2f", totalCompressionSavings / 100));
            avgResult.put("avgCompressionSpeedRatio", String.format("%.4f", totalCompressionSpeedRatio / 100));
            avgResult.put("errorRate", String.format("%.2f%%", (totalErrors / 100.0) * 100));
            avgResult.put("compressedUrl", "/compress/files/" + baseName + "_" + algo + "_compressed_1" + ext);
            avgResult.put("decompressedUrl",
                    "/compress/files/" + baseName + "_" + algo + "_decompressed_1" + extension);

            results.add(avgResult);
        }

        Path csvFile = Path.of(System.getProperty("java.io.tmpdir"), baseName + "_compression_results.csv");
        Files.deleteIfExists(csvFile);
        Files.write(csvFile, csvLines);

        Map<String, Object> response = new HashMap<>();
        response.put("results", results);
        response.put("csvUrl", "/compress/files/" + csvFile.getFileName());

        return ResponseEntity.ok(response);
    }

    // Handles download requests
    @GetMapping("/files/{filename}")
    public ResponseEntity<Resource> download(@PathVariable String filename) throws IOException {
        Path path = Files.list(Path.of(System.getProperty("java.io.tmpdir")))
                .filter(p -> p.getFileName().toString().equals(filename))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("File not found: " + filename));

        Resource res = new InputStreamResource(new FileInputStream(path.toFile()));

        MediaType type;
        try {
            String mime = Files.probeContentType(path);
            type = mime != null ? MediaType.parseMediaType(mime) : MediaType.APPLICATION_OCTET_STREAM;
        } catch (Exception e) {
            type = MediaType.APPLICATION_OCTET_STREAM;
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(type)
                .body(res);
    }

    private static boolean filesAreIdentical(Path file1, Path file2) throws IOException {
        return Arrays.equals(Files.readAllBytes(file1), Files.readAllBytes(file2));
    }
}
