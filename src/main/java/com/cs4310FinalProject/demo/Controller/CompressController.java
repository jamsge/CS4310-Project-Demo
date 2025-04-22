package com.cs4310FinalProject.demo.Controller;

import com.cs4310FinalProject.demo.CompressionAlgos.LZW_Algo;

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

        Path original = Files.createTempFile("original", null);
        file.transferTo(original.toFile());

        String currFile = Paths.get(file.getOriginalFilename()).getFileName().toString();
        String baseName = currFile.substring(0, currFile.lastIndexOf('.'));
        String extension = currFile.substring(currFile.lastIndexOf('.'));

        List<String> selectedAlgos = Arrays.asList(algorithmList.split(","));
        List<Map<String, Object>> results = new ArrayList<>();

        // Loop through each algorithm and perform compression and decompression
        for (String algo : selectedAlgos) {
            String ext = switch (algo) {
                case "LZW" -> ".lzw";
                case "RLE" -> ".rle";
                case "BZIP2" -> ".bz2";
                case "LZ77" -> ".lz77";
                default -> throw new IllegalArgumentException("Unknown algorithm: " + algo);
            };

            Path compressed = Path.of(System.getProperty("java.io.tmpdir"), 
                baseName + "_" + algo + "_compressed" + ext);
            Path decompressed = Path.of(System.getProperty("java.io.tmpdir"), 
                baseName + "_" + algo + "_decompressed" + extension); 

            Files.deleteIfExists(compressed);
            Files.deleteIfExists(decompressed);
            Files.createFile(compressed);
            Files.createFile(decompressed);

            long startComp = System.currentTimeMillis();
            switch (algo) {
                case "LZW" -> LZW_Algo.compressFile(original.toString(), compressed.toString());

                // Add ur algos then remove the comment here

                // case "RLE" -> RLE_Algo.compress();
                // case "BZIP2" -> Bzip2_Algo.compress();
                // case "LZ77" -> LZ77_Algo.compress();
            }
            long endComp = System.currentTimeMillis();

            long startDecomp = System.currentTimeMillis();
            switch (algo) {
                case "LZW" -> LZW_Algo.decompressFile(compressed.toString(), decompressed.toString());
                // case "RLE" -> RLE_Algo.decompressFile();
                // case "BZIP2" -> Bzip2_Algo.decompressFile();
                // case "LZ77" -> LZ77_Algo.decompressFile();
            }
            long endDecomp = System.currentTimeMillis();

            // Calculate the results
            long originalSize = Files.size(original);
            long compressedSize = Files.size(compressed);
            long compressionTime = endComp - startComp;
            double compressionRatio = (double) originalSize / compressedSize;
            double speedRatio = compressionRatio / compressionTime;
            
            Map<String, Object> result = new HashMap<>();
            result.put("algorithm", algo);
            result.put("originalSize", originalSize);
            result.put("compressedSize", compressedSize);
            result.put("compressionTime", compressionTime);
            result.put("decompressionTime", endDecomp - startDecomp);
            result.put("compressionRatio", String.format("%.2f", compressionRatio));
            result.put("compressionSpeedRatio", String.format("%.4f", speedRatio));
            result.put("compressedUrl", "/compress/files/" + compressed.getFileName());
            result.put("decompressedUrl", "/compress/files/" + decompressed.getFileName());

            results.add(result);
        }

        return ResponseEntity.ok(Map.of("results", results));
    }

    
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
}
