package ru.newaymc.newaycore.files;

import lombok.Getter;
import com.github.luben.zstd.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ru.newaymc.newaycore.NewaycoreMod;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Zstandard file compression/decompression utility.
 */
public class ZstdFileCompressor {
    private static final Logger LOGGER = LogManager.getLogger(NewaycoreMod.MODID + "/ZstdFileCompressor");

    private static final int MAGIC_NUMBER = 0x5A535444;

    private static final int MIN_COMPRESS_SIZE = 1024; // 1 KB
    private final ZstdDictCompress compressDict;
    private final ZstdDictDecompress decompressDict;
    private final boolean useDictionary;

    @Getter
    private static File zstdCompressDir;
    @Getter
    private static File zstdDecompressDir;

    /**
     * Creates a compressor without dictionary.
     */
    public ZstdFileCompressor() {
        this.compressDict = null;
        this.decompressDict = null;
        this.useDictionary = false;
    }

    /**
     * Creates a compressor with a pre-trained dictionary for better compression.
     *
     * @param dictionaryData The pre-trained dictionary data
     */
    public ZstdFileCompressor(byte[] dictionaryData) {
        if (dictionaryData != null && dictionaryData.length > 0) {
            this.compressDict = new ZstdDictCompress(dictionaryData, Zstd.defaultCompressionLevel());
            this.decompressDict = new ZstdDictDecompress(dictionaryData);
            this.useDictionary = true;
        } else {
            this.compressDict = null;
            this.decompressDict = null;
            this.useDictionary = false;
        }
    }

    public static void prepareDirectory(String baseDir) {
        zstdCompressDir = new File(baseDir + "/zstd/compress/");
        zstdDecompressDir = new File(baseDir + "/zstd/decompress/");


        LOGGER.info("Preparing ZSTD directories");
        zstdCompressDir.mkdirs();
        zstdDecompressDir.mkdirs();
    }

    // ======================== FILES ========================

    public void compressFile(File inputFile, File outputFile, boolean deleteOriginal) throws IOException {
        if (!inputFile.exists()) {
            throw new FileNotFoundException("File not found: " + inputFile.getAbsolutePath());
        }
        if (inputFile.isDirectory()) {
            throw new IllegalArgumentException("Input is a directory, not a file: " + inputFile.getAbsolutePath());
        }

        if (outputFile == null) {
            outputFile = new File(inputFile.getAbsolutePath() + ".zst");
        }

        if (inputFile.length() < MIN_COMPRESS_SIZE) {
            LOGGER.debug("File too small (" + inputFile.length() + " bytes), copying uncompressed: " + inputFile.getName());
            Files.copy(inputFile.toPath(), outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            if (deleteOriginal) {
                if (inputFile.delete()) {
                    LOGGER.debug("Deleted original file: " + inputFile.getName());
                } else {
                    LOGGER.warn("Failed to delete original file: " + inputFile.getName());
                }
            }
            return;
        }

        LOGGER.debug("Compressing: " + inputFile.getName() + " -> " + outputFile.getName());

        byte[] data = Files.readAllBytes(inputFile.toPath());
        compressToFile(data, outputFile);

        if (deleteOriginal) {
            if (inputFile.delete()) {
                LOGGER.info("Deleted original file: " + inputFile.getName());
            } else {
                LOGGER.warn("Failed to delete original file: " + inputFile.getName());
            }
        }
    }

    public void decompressToFile(File inputFile, File outputFile) throws IOException {
        if (outputFile == null) {
            String path = inputFile.getAbsolutePath();
            if (path.endsWith(".zst")) {
                path = path.substring(0, path.length() - 4);
            } else {
                path = path + ".decompressed";
            }
            outputFile = new File(path);
        }

        byte[] data = decompressFromFile(inputFile);
        Files.write(outputFile.toPath(), data);
        LOGGER.debug("Decompressed file saved: " + outputFile.getAbsolutePath());
    }

    // ======================== BATCH OPERATIONS ========================

    @Deprecated(forRemoval = true)
    public void compressFolder(File folder, boolean recursive, boolean deleteOriginal) throws IOException {
        if (!folder.isDirectory()) {
            throw new IllegalArgumentException("Not a folder: " + folder.getAbsolutePath());
        }

        File[] files = folder.listFiles();
        if (files == null) return;

        int compressed = 0;
        int deleted = 0;
        int failed = 0;

        for (File file : files) {
            if (file.isFile() && !file.getName().endsWith(".zst")) {
                try {
                    File outputFile = new File(file.getAbsolutePath() + ".zst");
                    compressFile(file, outputFile, deleteOriginal);
                    compressed++;
                    if (deleteOriginal && !file.exists()) {
                        deleted++;
                    }
                } catch (Exception e) {
                    LOGGER.warn("Failed to compress: " + file.getName() + " - " + e.getMessage());
                    failed++;
                }
            } else if (recursive && file.isDirectory()) {
                compressFolder(file, true, deleteOriginal);
            }
        }

        LOGGER.info("Folder compression complete. Compressed: " + compressed + ", Deleted: " + deleted + ", Failed: " + failed);
    }

    public void compressFolderStreaming(File folder, boolean recursive, int bufferSize, boolean deleteOriginal) throws IOException {
        if (!folder.isDirectory()) {
            throw new IllegalArgumentException("Not a folder: " + folder.getAbsolutePath());
        }

        LOGGER.debug("Streaming compression of folder: " + folder.getAbsolutePath());

        File[] files = folder.listFiles();
        if (files == null) return;

        int compressed = 0;
        int deleted = 0;
        int skipped = 0;
        int failed = 0;

        for (File file : files) {
            if (file.isFile() && !file.getName().endsWith(".zst")) {
                try {
                    if (file.length() < MIN_COMPRESS_SIZE) {
                        File outputFile = new File(file.getAbsolutePath() + ".zst");
                        Files.copy(file.toPath(), outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        LOGGER.debug("Copied small file: " + file.getName() + " (" + file.length() + " bytes)");
                        skipped++;

                        if (deleteOriginal) {
                            if (file.delete()) {
                                deleted++;
                            }
                        }
                    } else {
                        File outputFile = new File(file.getAbsolutePath() + ".zst");
                        compressFileStreaming(file, outputFile, bufferSize, deleteOriginal);
                        compressed++;
                        if (deleteOriginal && !file.exists()) {
                            deleted++;
                        }
                    }
                } catch (Exception e) {
                    LOGGER.warn("Failed to compress: " + file.getName() + " - " + e.getMessage());
                    failed++;
                }
            } else if (recursive && file.isDirectory()) {
                compressFolderStreaming(file, true, bufferSize, deleteOriginal);
            }
        }

        LOGGER.info("Folder compression complete. Compressed: " + compressed +
                ", Skipped (small): " + skipped +
                ", Deleted: " + deleted +
                ", Failed: " + failed);
    }

    public void decompressFolder(File folder, boolean deleteAfterDecompress) throws IOException {
        File[] files = folder.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {

                decompressFolder(file, deleteAfterDecompress);
            } else if (file.getName().endsWith(".zst")) {
                try {
                    String outputPath = file.getAbsolutePath();
                    outputPath = outputPath.substring(0, outputPath.length() - 4);
                    decompressToFile(file, new File(outputPath));

                    if (deleteAfterDecompress) {
                        if (file.delete()) {
                            LOGGER.debug("Deleted: " + file.getAbsolutePath());
                        } else {
                            LOGGER.warn("Failed to delete: " + file.getAbsolutePath());
                        }
                    }
                } catch (IOException e) {
                    LOGGER.error("Error decompressing: " + file.getAbsolutePath(), e);
                    throw e;
                }
            }
        }
    }

    // ======================== DICTIONARY TRAINING ========================

    public static byte[] trainDictionary(List<byte[]> sampleData, int dictionarySize) {
        if (sampleData == null || sampleData.isEmpty()) {
            throw new IllegalArgumentException("At least one sample is required for training");
        }

        LOGGER.info("Training dictionary of " + dictionarySize + " bytes on " + sampleData.size() + " samples");

        ZstdDictTrainer trainer = new ZstdDictTrainer(dictionarySize, 0);
        for (byte[] sample : sampleData) {
            if (sample != null && sample.length > 0) {
                trainer.addSample(sample);
            }
        }

        byte[] dictionary = trainer.trainSamples();
        LOGGER.info("Dictionary trained successfully. Size: " + dictionary.length + " bytes");
        return dictionary;
    }

    public static byte[] trainDictionaryFromFolder(File folder, int dictionarySize) throws IOException {
        if (!folder.isDirectory()) {
            throw new IllegalArgumentException("Not a folder: " + folder.getAbsolutePath());
        }

        List<byte[]> samples = new ArrayList<>();
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".zst"));
        if (files == null) {
            throw new IllegalArgumentException("No .zst files found in folder: " + folder.getAbsolutePath());
        }

        for (File file : files) {
            try (DataInputStream dis = new DataInputStream(new FileInputStream(file))) {
                int magic = dis.readInt();
                if (magic == MAGIC_NUMBER) {
                    int decompressedSize = dis.readInt();
                    byte[] compressedData = new byte[(int) (file.length() - 8)];
                    dis.readFully(compressedData);
                    byte[] sample = Zstd.decompress(compressedData, decompressedSize);
                    samples.add(sample);
                }
            } catch (Exception e) {
                LOGGER.warn("Failed to read sample file: " + file.getName() + " - " + e.getMessage());
            }
        }

        if (samples.isEmpty()) {
            throw new IllegalArgumentException("No valid samples found in folder: " + folder.getAbsolutePath());
        }

        return trainDictionary(samples, dictionarySize);
    }

    // ======================== UTILS ========================

    private void compressToFile(byte[] data, File outputFile) throws IOException {
        if (data == null || data.length == 0) {
            LOGGER.warn("Attempted to compress empty data. File will not be created.");
            return;
        }

        if (data.length < MIN_COMPRESS_SIZE) {
            LOGGER.debug("File too small (" + data.length + " bytes), saving uncompressed: " + outputFile.getName());
            Files.write(outputFile.toPath(), data);
            return;
        }

        LOGGER.debug("Compressing " + data.length + " bytes to: " + outputFile.getAbsolutePath());

        byte[] compressedData;
        long startTime = System.nanoTime();

        if (useDictionary) {
            compressedData = Zstd.compress(data, compressDict);
        } else {
            compressedData = Zstd.compress(data);
        }

        long duration = (System.nanoTime() - startTime) / 1_000_000;
        double ratio = (double) compressedData.length / data.length;
        LOGGER.info("Compression done in " + duration + " ms. Ratio: " + String.format("%.2f", ratio));

        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(outputFile))) {
            dos.writeInt(MAGIC_NUMBER);
            dos.writeInt(data.length);
            dos.write(compressedData);
        }

        LOGGER.debug("Saved: " + outputFile.getName() + " (" + compressedData.length + " bytes)");
    }

    private void compressFileStreaming(File inputFile, File outputFile, int bufferSize, boolean deleteOriginal) throws IOException {
        if (!inputFile.exists()) {
            throw new FileNotFoundException("File not found: " + inputFile.getAbsolutePath());
        }
        if (inputFile.isDirectory()) {
            throw new IllegalArgumentException("Input is a directory, not a file: " + inputFile.getAbsolutePath());
        }

        if (outputFile == null) {
            outputFile = new File(inputFile.getAbsolutePath() + ".zst");
        }

        if (inputFile.length() < MIN_COMPRESS_SIZE) {
            LOGGER.info("File too small ({} bytes), copying uncompressed: {}", inputFile.length(), inputFile.getName());
            Files.copy(inputFile.toPath(), outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            if (deleteOriginal && inputFile.delete()) {
                LOGGER.debug("Deleted original file: {}", inputFile.getName());
            }
            return;
        }

        LOGGER.debug("Streaming compression: {} -> {}", inputFile.getName(), outputFile.getName());
        long startTime = System.nanoTime();

        try (FileInputStream fis = new FileInputStream(inputFile);
             DataOutputStream dos = new DataOutputStream(new FileOutputStream(outputFile))) {

            dos.writeInt(MAGIC_NUMBER);
            dos.writeInt(0); // Temp.

            try (ZstdOutputStream zos = new ZstdOutputStream(dos)) {
                if (useDictionary) {
                    zos.setDict(compressDict);
                }
                byte[] buffer = new byte[bufferSize];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    zos.write(buffer, 0, bytesRead);
                }
            }

            long totalBytes = inputFile.length();
            try (RandomAccessFile raf = new RandomAccessFile(outputFile, "rw")) {
                raf.seek(4);
                raf.writeInt((int) totalBytes);
            }

            long duration = (System.nanoTime() - startTime) / 1_000_000;
            LOGGER.info("Compression done in {} ms. Original: {} bytes, Compressed: {} bytes", duration, totalBytes, outputFile.length());
        }

        if (deleteOriginal && inputFile.delete()) {
            LOGGER.debug("Deleted original file: {}", inputFile.getName());
        }
    }

    private byte[] decompressFromFile(File inputFile) throws IOException {
        if (!inputFile.exists()) {
            throw new FileNotFoundException("File not found: " + inputFile.getAbsolutePath());
        }

        try (DataInputStream dis = new DataInputStream(new FileInputStream(inputFile))) {
            int magic = dis.readInt();

            if (magic != MAGIC_NUMBER) {
                LOGGER.info("File is uncompressed, reading directly: " + inputFile.getName());

                try (FileInputStream fis = new FileInputStream(inputFile)) {
                    return fis.readAllBytes();
                }
            }

            int decompressedSize = dis.readInt();
            if (decompressedSize <= 0) {
                throw new IllegalArgumentException("Invalid decompressed size: " + decompressedSize);
            }

            LOGGER.info("Decompressing: " + inputFile.getName());
            long startTime = System.nanoTime();

            byte[] compressedData = new byte[(int) (inputFile.length() - 8)];
            int readBytes = dis.read(compressedData);
            if (readBytes != compressedData.length) {
                throw new IOException("Failed to read entire compressed file");
            }

            byte[] decompressedData;
            if (useDictionary) {
                decompressedData = Zstd.decompress(compressedData, decompressDict, decompressedSize);
            } else {
                decompressedData = Zstd.decompress(compressedData, decompressedSize);
            }

            if (decompressedData.length != decompressedSize) {
                throw new IOException("Decompressed size mismatch: " + decompressedData.length + " != " + decompressedSize);
            }

            long duration = (System.nanoTime() - startTime) / 1_000_000;
            LOGGER.debug("Decompressed " + decompressedData.length + " bytes in " + duration + " ms");

            return decompressedData;
        }
    }
}
