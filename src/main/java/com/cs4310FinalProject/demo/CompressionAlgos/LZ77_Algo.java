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

public class LZ77_Algo {

    // make sure offset/length values in LZ77 triplets are never larger than a
    // single byte
    private static final int SEARCH_BUFFER_SIZE = 255;
    private static final int LOOKAHEAD_BUFFER_SIZE = 255;

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

    // Compress the input data using LZ77
    private static List<Byte> compress(byte[] input) {
        List<Byte> result = new ArrayList<>();
        int pos = 0;

        while (pos < input.length) {
            short maxMatchLength = 0;
            short matchOffset = 0;

            int searchStart = Math.max(0, pos - SEARCH_BUFFER_SIZE);

            // for each byte in the search buffer
            for (int i = searchStart; i < pos; i++) {
                short length = 0;

                // if search buffer byte matches current byte
                // increment length by 1 for every following byte that matches
                // the corresponding following bytes in the lookahead buffer
                while (length < LOOKAHEAD_BUFFER_SIZE && pos + length < input.length
                        && input[i + length] == input[pos + length]) {
                    length++;
                    if (i + length >= pos)
                        break;
                }

                // if this set of bytes is a longer match than previous ones
                // overwrite maxMatchLength and offset with new values
                if (length > maxMatchLength) {
                    maxMatchLength = length;
                    matchOffset = (short) (pos - i);
                }
            }

            // index of next byte
            int nextByte = (pos + maxMatchLength);

            // add first two values in triplet to byte list
            result.add((byte) matchOffset);
            result.add((byte) maxMatchLength);

            // write next character byte unless at end of array
            if (nextByte < input.length) {
                result.add(input[nextByte]);
            } else {
                result.add((byte) 0);
            }

            // increment position to byte following "nextByte"
            pos += maxMatchLength + 1;
        }

        return result;
    }

    // Decompress the LZ77 data
    private static byte[] decompress(List<Byte> compressed) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        // for each triplet in the compressed byte array...
        for (int i = 0; i < compressed.size(); i += 3) {

            // seperate each triplet value into individual variables
            int offset = Byte.toUnsignedInt(compressed.get(i));
            int length = Byte.toUnsignedInt(compressed.get(i + 1));
            byte nextChar = compressed.get(i + 2);

            // if there is a pattern match, (offset != 0) read that match
            // from the current output array and write
            // the pattern to the end of the output array
            int start = output.size() - offset;

            for (int j = 0; j < length; j++) {
                byte b = output.toByteArray()[start + j];
                output.write(b);
            }

            // write next character to end of the output array
            if (nextChar != (byte)0) {
                output.write(nextChar);
            }
        }

        return output.toByteArray();
    }
}
