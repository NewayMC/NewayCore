package ru.newaymc.newaycore.files;

import lombok.Getter;
import com.github.luben.zstd.Zstd;
import com.github.luben.zstd.ZstdDictCompress;
import com.github.luben.zstd.ZstdDictDecompress;
import com.github.luben.zstd.ZstdDictTrainer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ru.newaymc.newaycore.NewaycoreMod;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Zstandard file compression/decompression utility for game world chunks.
 * Supports both standard compression and dictionary-based compression for better ratios.
 * Each chunk is stored as a separate compressed file with metadata.
 * <p>
 *  File format: [MAGIC_NUMBER(4 bytes)] [DECOMPRESSED_SIZE(4 bytes)] [COMPRESSED_DATA]
 */
public class ZstdFileCompressor {
    private static final Logger LOGGER = LogManager.getLogger(NewaycoreMod.MODID + "/ZstdFileCompressor");

    /** Magic number for file format verification: 0x5A535444 ("ZSTD" in ASCII) */
    private static final int MAGIC_NUMBER = 0x5A535444;

    /** Minimum file size to actually perform compression (files smaller than this are copied) */
    private static final int MIN_COMPRESS_SIZE = 1024; // 1 KB
    private final ZstdDictCompress compressDict;
    private final ZstdDictDecompress decompressDict;
    private final boolean useDictionary;

    @Getter
    private static File zstdCompressDir;
    @Getter
    private static File zstdDecompressDir;

    /**
     * Creates a compressor without dictionary (standard Zstandard compression).
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

        if (!zstdCompressDir.exists() && !zstdDecompressDir.exists()) {
            LOGGER.info("Preparing ZSTD directories");
            zstdCompressDir.mkdirs();
            zstdDecompressDir.mkdirs();
        }
    }

    // ======================== COMPRESSION METHODS ========================

    /**
     * Compresses byte array data and saves it to a file with metadata.
     * Skips compression for files smaller than MIN_COMPRESS_SIZE.
     *
     * @param data       Raw data to compress
     * @param outputFile Output file to save compressed data
     * @throws IOException If an I/O error occurs
     */
    public void compressToFile(byte[] data, File outputFile) throws IOException {
        if (data == null || data.length == 0) {
            LOGGER.warn("Attempted to compress empty data. File will not be created.");
            return;
        }

        // Skip compression for tiny files - just save them directly
        if (data.length < MIN_COMPRESS_SIZE) {
            LOGGER.info("File too small (" + data.length + " bytes), saving uncompressed: " + outputFile.getName());
            Files.write(outputFile.toPath(), data);
            return;
        }

        LOGGER.info("Compressing " + data.length + " bytes to: " + outputFile.getAbsolutePath());

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

        LOGGER.info("Saved: " + outputFile.getName() + " (" + compressedData.length + " bytes)");
    }

    /**
     * Compresses a file and deletes the original if compression was successful.
     *
     * @param inputFile  Source file to compress
     * @param outputFile Destination file for compressed data (if null, adds .zst suffix)
     * @param deleteOriginal If true, deletes the original file after successful compression
     * @throws IOException If an I/O error occurs
     */
    public void compressFile(File inputFile, File outputFile, boolean deleteOriginal) throws IOException {
        if (!inputFile.exists()) {
            throw new FileNotFoundException("File not found: " + inputFile.getAbsolutePath());
        }
        if (inputFile.isDirectory()) {
            throw new IllegalArgumentException("Input is a directory, not a file: " + inputFile.getAbsolutePath());
        }

        // If output file not specified, create one with .zst extension
        if (outputFile == null) {
            outputFile = new File(inputFile.getAbsolutePath() + ".zst");
        }

        // If file is too small, just copy it
        if (inputFile.length() < MIN_COMPRESS_SIZE) {
            LOGGER.info("File too small (" + inputFile.length() + " bytes), copying uncompressed: " + inputFile.getName());
            Files.copy(inputFile.toPath(), outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            if (deleteOriginal) {
                if (inputFile.delete()) {
                    LOGGER.info("Deleted original file: " + inputFile.getName());
                } else {
                    LOGGER.warn("Failed to delete original file: " + inputFile.getName());
                }
            }
            return;
        }

        LOGGER.info("Compressing: " + inputFile.getName() + " -> " + outputFile.getName());

        byte[] data = Files.readAllBytes(inputFile.toPath());
        compressToFile(data, outputFile);

        // Delete original after successful compression
        if (deleteOriginal) {
            if (inputFile.delete()) {
                LOGGER.info("Deleted original file: " + inputFile.getName());
            } else {
                LOGGER.warn("Failed to delete original file: " + inputFile.getName());
            }
        }
    }

    /**
     * Streaming compression with automatic deletion of original file.
     *
     * @param inputFile  Source file to compress
     * @param outputFile Destination file for compressed data (if null, adds .zst suffix)
     * @param bufferSize Buffer size for reading (recommended: 64KB - 4MB)
     * @param deleteOriginal If true, deletes the original file after successful compression
     * @throws IOException If an I/O error occurs
     */
    public void compressFileStreaming(File inputFile, File outputFile, int bufferSize, boolean deleteOriginal) throws IOException {
        if (!inputFile.exists()) {
            throw new FileNotFoundException("File not found: " + inputFile.getAbsolutePath());
        }
        if (inputFile.isDirectory()) {
            throw new IllegalArgumentException("Input is a directory, not a file: " + inputFile.getAbsolutePath());
        }

        // If output file not specified, create one with .zst extension
        if (outputFile == null) {
            outputFile = new File(inputFile.getAbsolutePath() + ".zst");
        }

        // If file is too small, just copy it
        if (inputFile.length() < MIN_COMPRESS_SIZE) {
            LOGGER.info("File too small (" + inputFile.length() + " bytes), copying uncompressed: " + inputFile.getName());
            Files.copy(inputFile.toPath(), outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            if (deleteOriginal) {
                if (inputFile.delete()) {
                    LOGGER.info("Deleted original file: " + inputFile.getName());
                } else {
                    LOGGER.warn("Failed to delete original file: " + inputFile.getName());
                }
            }
            return;
        }

        LOGGER.info("Streaming compression: " + inputFile.getName() + " -> " + outputFile.getName());
        long startTime = System.nanoTime();

        try (FileInputStream fis = new FileInputStream(inputFile);
             DataOutputStream dos = new DataOutputStream(new FileOutputStream(outputFile))) {

            // Write header (size will be overwritten later)
            dos.writeInt(MAGIC_NUMBER);
            dos.writeInt(0); // Temporary, will be updated at the end

            byte[] buffer = new byte[bufferSize];
            int bytesRead;
            long totalBytes = 0;

            while ((bytesRead = fis.read(buffer)) != -1) {
                byte[] chunk = Arrays.copyOf(buffer, bytesRead);
                byte[] compressedChunk;

                if (useDictionary) {
                    compressedChunk = Zstd.compress(chunk, compressDict);
                } else {
                    compressedChunk = Zstd.compress(chunk);
                }

                dos.write(compressedChunk);
                totalBytes += bytesRead;
            }

            // Update the header with actual decompressed size
            dos.flush();
            try (RandomAccessFile raf = new RandomAccessFile(outputFile, "rw")) {
                raf.seek(4); // Skip magic number
                raf.writeInt((int) totalBytes);
            }

            long duration = (System.nanoTime() - startTime) / 1_000_000;
            double ratio = (double) outputFile.length() / totalBytes;
            LOGGER.info("Compression done in " + duration + " ms. Ratio: " + String.format("%.2f", ratio));
            LOGGER.info("Original: " + totalBytes + " bytes, Compressed: " + outputFile.length() + " bytes");
        }

        // Delete original after successful compression
        if (deleteOriginal) {
            if (inputFile.delete()) {
                LOGGER.info("Deleted original file: " + inputFile.getName());
            } else {
                LOGGER.warn("Failed to delete original file: " + inputFile.getName());
            }
        }
    }

    /**
     * Compresses a serializable object to a file.
     *
     * @param object     Serializable object to compress
     * @param outputFile Destination file
     * @throws IOException If an I/O error occurs
     */
    public void compressObject(Serializable object, File outputFile) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(object);
        }
        compressToFile(baos.toByteArray(), outputFile);
    }

    // ======================== DECOMPRESSION METHODS ========================

    /**
     * Decompresses a file and returns the raw data as a byte array.
     *
     * @param inputFile Compressed file to read
     * @return Decompressed data as byte array
     * @throws IOException If an I/O error occurs
     */
    public byte[] decompressFromFile(File inputFile) throws IOException {
        if (!inputFile.exists()) {
            throw new FileNotFoundException("File not found: " + inputFile.getAbsolutePath());
        }

        // Check if the file is actually compressed or just copied
        try (DataInputStream dis = new DataInputStream(new FileInputStream(inputFile))) {
            int magic = dis.readInt();

            // If magic number doesn't match, it's an uncompressed file
            if (magic != MAGIC_NUMBER) {
                LOGGER.info("File is uncompressed, reading directly: " + inputFile.getName());

                try (FileInputStream fis = new FileInputStream(inputFile)) {
                    return fis.readAllBytes();
                }
            }

            // Compressed file - read metadata
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
                throw new IOException("Decompressed size mismatch: " + decompressedData.length +
                        " != " + decompressedSize);
            }

            long duration = (System.nanoTime() - startTime) / 1_000_000;
            LOGGER.info("Decompressed " + decompressedData.length + " bytes in " + duration + " ms");

            return decompressedData;
        }
    }

    /**
     * Decompresses a file and saves it as a regular file.
     *
     * @param inputFile  Compressed file to decompress
     * @param outputFile Output file for decompressed data (if null, removes .zst suffix)
     * @throws IOException If an I/O error occurs
     */
    public void decompressToFile(File inputFile, File outputFile) throws IOException {
        // If output file not specified, remove .zst suffix or add .decompressed
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
        LOGGER.info("Decompressed file saved: " + outputFile.getAbsolutePath());
    }

    /**
     * Decompresses a file and returns the object.
     *
     * @param inputFile Compressed file containing a serialized object
     * @param <T>       Type of the object
     * @return Decompressed object
     * @throws IOException If an I/O error occurs
     */
    @SuppressWarnings("unchecked")
    public <T extends Serializable> T decompressObject(File inputFile) throws IOException {
        byte[] data = decompressFromFile(inputFile);
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data))) {
            return (T) ois.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException("Deserialization error: class not found", e);
        }
    }

    // ======================== BATCH OPERATIONS ========================

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

        LOGGER.info("Folder compression complete. Compressed: " + compressed +
                ", Deleted: " + deleted + ", Failed: " + failed);
    }

    /**
     * Streaming compression for entire folders with automatic deletion.
     *
     * @param folder     Folder containing files to compress
     * @param recursive  Process subdirectories recursively
     * @param bufferSize Buffer size for streaming
     * @param deleteOriginal Delete original files after successful compression
     * @throws IOException If an I/O error occurs
     */
    public void compressFolderStreaming(File folder, boolean recursive, int bufferSize, boolean deleteOriginal) throws IOException {
        if (!folder.isDirectory()) {
            throw new IllegalArgumentException("Not a folder: " + folder.getAbsolutePath());
        }

        LOGGER.info("Streaming compression of folder: " + folder.getAbsolutePath());

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
                        LOGGER.info("Copied small file: " + file.getName() + " (" + file.length() + " bytes)");
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

    /**
     * Decompresses all .zst files in a folder.
     *
     * @param folder                Folder containing compressed files
     * @param deleteAfterDecompress Delete .zst files after successful decompression
     * @throws IOException If an I/O error occurs
     */
    public void decompressFolder(File folder, boolean deleteAfterDecompress) throws IOException {
        if (!folder.isDirectory()) {
            throw new IllegalArgumentException("Not a folder: " + folder.getAbsolutePath());
        }

        File[] files = folder.listFiles((dir, name) -> name.endsWith(".zst"));
        if (files == null) return;

        for (File file : files) {
            String outputPath = file.getAbsolutePath();
            outputPath = outputPath.substring(0, outputPath.length() - 4);
            decompressToFile(file, new File(outputPath));

            if (deleteAfterDecompress) {
                if (file.delete()) {
                    LOGGER.info("Deleted: " + file.getName());
                }
            }
        }
    }

    // ======================== DICTIONARY TRAINING ========================

    /**
     * Trains a dictionary on sample data for better compression of similar files.
     *
     * @param sampleData     List of byte arrays (typical data samples)
     * @param dictionarySize Desired dictionary size (e.g., 16 * 1024 for 16 KB)
     * @return Trained dictionary as byte array
     */
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

    /**
     * Trains a dictionary from .zst files in a folder.
     *
     * @param folder         Folder containing .zst files to use as samples
     * @param dictionarySize Desired dictionary size
     * @return Trained dictionary as byte array
     * @throws IOException If an I/O error occurs
     */
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
                    dis.read(compressedData);
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
}
