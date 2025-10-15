package com.example.lzh.exception;

import com.example.lzh.util.ErrorContext;

/**
 * Exception thrown when the LZH archive is corrupted or damaged.
 * This exception is thrown when the archive structure is valid but the data
 * is corrupted, checksums don't match, or decompression fails due to data corruption.
 * Enhanced with detailed error context information.
 * 要件 7.1, 7.2, 7.3, 7.4 に対応
 */
public class CorruptedArchiveException extends LzhException {
    
    /**
     * Constructs a new CorruptedArchiveException with no detail message.
     */
    public CorruptedArchiveException() {
        super("LZH archive is corrupted or damaged", "CORRUPTED_ARCHIVE");
    }
    
    /**
     * Constructs a new CorruptedArchiveException with the specified detail message.
     * 
     * @param message the detail message
     */
    public CorruptedArchiveException(String message) {
        super(message, "CORRUPTED_ARCHIVE");
    }
    
    /**
     * Constructs a new CorruptedArchiveException with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public CorruptedArchiveException(String message, Throwable cause) {
        super(message, "CORRUPTED_ARCHIVE", cause);
    }
    
    /**
     * Constructs a new CorruptedArchiveException with the specified cause.
     * 
     * @param cause the cause of this exception
     */
    public CorruptedArchiveException(Throwable cause) {
        super("LZH archive is corrupted or damaged", "CORRUPTED_ARCHIVE", cause);
    }
    
    /**
     * Constructs a new CorruptedArchiveException with error context.
     * 
     * @param message the detail message
     * @param errorContext the error context
     */
    public CorruptedArchiveException(String message, ErrorContext errorContext) {
        super(message, errorContext);
        setErrorCode("CORRUPTED_ARCHIVE");
    }
    
    /**
     * Constructs a new CorruptedArchiveException with error context and cause.
     * 
     * @param message the detail message
     * @param errorContext the error context
     * @param cause the cause of this exception
     */
    public CorruptedArchiveException(String message, ErrorContext errorContext, Throwable cause) {
        super(message, errorContext, cause);
        setErrorCode("CORRUPTED_ARCHIVE");
    }
    
    /**
     * Creates a CorruptedArchiveException for checksum mismatch errors.
     * 
     * @param expectedChecksum expected checksum value
     * @param actualChecksum actual checksum value
     * @param errorContext error context
     * @return configured exception
     */
    public static CorruptedArchiveException forChecksumMismatch(int expectedChecksum, int actualChecksum, ErrorContext errorContext) {
        String message = String.format("Checksum mismatch: expected 0x%04X, got 0x%04X", expectedChecksum, actualChecksum);
        return new CorruptedArchiveException(message, errorContext);
    }
    
    /**
     * Creates a CorruptedArchiveException for incomplete data errors.
     * 
     * @param expectedBytes expected number of bytes
     * @param actualBytes actual number of bytes read
     * @param errorContext error context
     * @return configured exception
     */
    public static CorruptedArchiveException forIncompleteData(long expectedBytes, long actualBytes, ErrorContext errorContext) {
        String message = String.format("Incomplete data: expected %d bytes, got %d bytes", expectedBytes, actualBytes);
        return new CorruptedArchiveException(message, errorContext);
    }
    
    /**
     * Creates a CorruptedArchiveException for decompression errors.
     * 
     * @param decompressionError description of the decompression error
     * @param errorContext error context
     * @return configured exception
     */
    public static CorruptedArchiveException forDecompressionError(String decompressionError, ErrorContext errorContext) {
        return new CorruptedArchiveException("Decompression failed: " + decompressionError, errorContext);
    }
    
    /**
     * Creates a CorruptedArchiveException for data integrity errors.
     * 
     * @param integrityIssue description of the integrity issue
     * @param errorContext error context
     * @return configured exception
     */
    public static CorruptedArchiveException forDataIntegrityError(String integrityIssue, ErrorContext errorContext) {
        return new CorruptedArchiveException("Data integrity error: " + integrityIssue, errorContext);
    }
}