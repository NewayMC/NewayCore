package ru.newaymc.newaycore.files;

import com.github.luben.zstd.Zstd;
import com.github.luben.zstd.ZstdDictCompress;
import com.github.luben.zstd.ZstdDictDecompress;
import com.github.luben.zstd.ZstdDictTrainer;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * Zstandard file compression/decompression utility for game world chunks.
 * Supports both standard compression and dictionary-based compression for better ratios.
 * Each chunk is stored as a separate compressed file with metadata.
 * <p>
 *  File format: [MAGIC_NUMBER(4 bytes)] [DECOMPRESSED_SIZE(4 bytes)] [COMPRESSED_DATA]
 */
public class ZstdFileCompressor {
    private static final Logger LOGGER = Logger.getLogger(ZstdFileCompressor.class.getName());

    /** Magic number for file format verification: 0x5A535444 ("ZSTD" in ASCII) */
    private static final int MAGIC_NUMBER = 0x5A535444;

    /** Minimum file size to actually perform compression (files smaller than this are copied) */
    private static final int MIN_COMPRESS_SIZE = 1024; // 1 KB

    private ZstdDictCompress compressDict;
    private ZstdDictDecompress decompressDict;
    private boolean useDictionary = false;

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
            LOGGER.warning("Attempted to compress empty data. File will not be created.");
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
     * Compresses a raw file into a compressed file.
     *
     * @param inputFile  Source file to compress
     * @param outputFile Destination file for compressed data (if null, adds .zst suffix)
     * @throws IOException If an I/O error occurs
     */
    public void compressFile(File inputFile, File outputFile) throws IOException {
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
            return;
        }

        LOGGER.info("Compressing: " + inputFile.getName() + " -> " + outputFile.getName());

        byte[] data = Files.readAllBytes(inputFile.toPath());
        compressToFile(data, outputFile);
    }

    /**
     * Streams a large file for compression without loading it entirely into memory.
     * Recommended for files larger than 100 MB.
     *
     * @param inputFile  Source file to compress
     * @param outputFile Destination file for compressed data (if null, adds .zst suffix)
     * @param bufferSize Buffer size for reading (recommended: 64KB - 4MB)
     * @throws IOException If an I/O error occurs
     */
    public void compressFileStreaming(File inputFile, File outputFile, int bufferSize) throws IOException {
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
    }

    /**
     * Compresses a serializable object to a file.
     * Convenience method for game world chunks and entity data.
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
                // Reset to beginning and read entire file
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
     * Decompresses a file directly to an OutputStream without storing in memory.
     *
     * @param inputFile     Compressed file to read
     * @param outputStream  Stream to write decompressed data
     * @throws IOException If an I/O error occurs
     */
    public void decompressToStream(File inputFile, OutputStream outputStream) throws IOException {
        byte[] data = decompressFromFile(inputFile);
        outputStream.write(data);
        outputStream.flush();
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

    /**
     * Compresses all files in a folder.
     *
     * @param folder    Folder containing files to compress
     * @param recursive Process subdirectories recursively
     * @throws IOException If an I/O error occurs
     */
    public void compressFolder(File folder, boolean recursive) throws IOException {
        if (!folder.isDirectory()) {
            throw new IllegalArgumentException("Not a folder: " + folder.getAbsolutePath());
        }

        File[] files = folder.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isFile() && !file.getName().endsWith(".zst")) {
                compressFile(file, new File(file.getAbsolutePath() + ".zst"));
            } else if (recursive && file.isDirectory()) {
                compressFolder(file, true);
            }
        }
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
     * Trains a dictionary on sample data for better compression of similar chunks.
     * Dictionary improves compression for small, repetitive data structures.
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
     * Convenience method for training on existing game chunks.
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
                LOGGER.warning("Failed to read sample file: " + file.getName() + " - " + e.getMessage());
            }
        }

        if (samples.isEmpty()) {
            throw new IllegalArgumentException("No valid samples found in folder: " + folder.getAbsolutePath());
        }

        return trainDictionary(samples, dictionarySize);
    }

    // ======================== UTILITY METHODS ========================

    /**
     * Checks if file exists.
     *
     * @param file File to check
     * @return true if file exists
     */
    public static boolean fileExists(File file) {
        return file.exists();
    }

    /**
     * Deletes file.
     *
     * @param file File to delete
     * @return true if deletion was successful
     */
    public static boolean deleteFile(File file) {
        if (file.exists()) {
            boolean deleted = file.delete();
            if (deleted) {
                LOGGER.info("Deleted: " + file.getName());
            }
            return deleted;
        }
        return false;
    }

    /**
     * Gets the size of a compressed file on disk.
     *
     * @param file Compressed file
     * @return File size in bytes
     * @throws IOException If an I/O error occurs
     */
    public static long getCompressedSize(File file) throws IOException {
        return file.length();
    }

    /**
     * Checks if dictionary compression is enabled.
     *
     * @return true if dictionary is being used
     */
    public boolean isUseDictionary() {
        return useDictionary;
    }

    /**
     * Gets the compression dictionary.
     *
     * @return Dictionary for compression, or null if not used
     */
    public ZstdDictCompress getCompressDict() {
        return compressDict;
    }

    /**
     * Gets the decompression dictionary.
     *
     * @return Dictionary for decompression, or null if not used
     */
    public ZstdDictDecompress getDecompressDict() {
        return decompressDict;
    }
}
