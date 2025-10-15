package com.example.lzh.exception;

import com.example.lzh.util.ErrorContext;

/**
 * Exception thrown when character encoding issues occur during file name processing.
 * This exception is thrown when file names cannot be properly decoded from the
 * archive due to encoding problems or unsupported character sets.
 * Enhanced with detailed error context information.
 * 要件 7.1, 7.2, 7.3, 7.4 に対応
 */
public class EncodingException extends LzhException {
    
    private String problematicEncoding;
    private byte[] originalBytes;
    
    /**
     * Constructs a new EncodingException with no detail message.
     */
    public EncodingException() {
        super("Character encoding error", "ENCODING_ERROR");
    }
    
    /**
     * Constructs a new EncodingException with the specified detail message.
     * 
     * @param message the detail message
     */
    public EncodingException(String message) {
        super(message, "ENCODING_ERROR");
    }
    
    /**
     * Constructs a new EncodingException with the specified encoding name.
     * 
     * @param encoding the problematic encoding name
     */
    public EncodingException(String encoding) {
        super("Character encoding error with encoding: " + encoding, "ENCODING_ERROR");
        this.problematicEncoding = encoding;
    }
    
    /**
     * Constructs a new EncodingException with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public EncodingException(String message, Throwable cause) {
        super(message, "ENCODING_ERROR", cause);
    }
    
    /**
     * Constructs a new EncodingException with the specified cause.
     * 
     * @param cause the cause of this exception
     */
    public EncodingException(Throwable cause) {
        super("Character encoding error", "ENCODING_ERROR", cause);
    }
    
    /**
     * Constructs a new EncodingException with error context.
     * 
     * @param message the detail message
     * @param errorContext the error context
     */
    public EncodingException(String message, ErrorContext errorContext) {
        super(message, errorContext);
        setErrorCode("ENCODING_ERROR");
    }
    
    /**
     * Constructs a new EncodingException with error context and cause.
     * 
     * @param message the detail message
     * @param errorContext the error context
     * @param cause the cause of this exception
     */
    public EncodingException(String message, ErrorContext errorContext, Throwable cause) {
        super(message, errorContext, cause);
        setErrorCode("ENCODING_ERROR");
    }
    
    /**
     * Creates an EncodingException for unsupported encoding.
     * 
     * @param encoding the unsupported encoding
     * @param errorContext error context
     * @return configured exception
     */
    public static EncodingException forUnsupportedEncoding(String encoding, ErrorContext errorContext) {
        EncodingException ex = new EncodingException("Unsupported character encoding: " + encoding, errorContext);
        ex.problematicEncoding = encoding;
        return ex;
    }
    
    /**
     * Creates an EncodingException for decoding failure.
     * 
     * @param encoding the encoding that failed
     * @param originalBytes the original bytes that couldn't be decoded
     * @param errorContext error context
     * @return configured exception
     */
    public static EncodingException forDecodingFailure(String encoding, byte[] originalBytes, ErrorContext errorContext) {
        String message = String.format("Failed to decode bytes using encoding %s (length: %d bytes)", 
                                     encoding, originalBytes != null ? originalBytes.length : 0);
        EncodingException ex = new EncodingException(message, errorContext);
        ex.problematicEncoding = encoding;
        ex.originalBytes = originalBytes != null ? originalBytes.clone() : null;
        return ex;
    }
    
    /**
     * Creates an EncodingException for encoding detection failure.
     * 
     * @param originalBytes the bytes for which encoding detection failed
     * @param errorContext error context
     * @return configured exception
     */
    public static EncodingException forDetectionFailure(byte[] originalBytes, ErrorContext errorContext) {
        String message = String.format("Failed to detect character encoding for %d bytes", 
                                     originalBytes != null ? originalBytes.length : 0);
        EncodingException ex = new EncodingException(message, errorContext);
        ex.originalBytes = originalBytes != null ? originalBytes.clone() : null;
        return ex;
    }
    
    /**
     * Gets the problematic encoding name.
     * 
     * @return the encoding name, or null if not set
     */
    public String getProblematicEncoding() {
        return problematicEncoding;
    }
    
    /**
     * Gets the original bytes that caused the encoding issue.
     * 
     * @return copy of the original bytes, or null if not set
     */
    public byte[] getOriginalBytes() {
        return originalBytes != null ? originalBytes.clone() : null;
    }
    
    /**
     * Gets a hex representation of the original bytes for debugging.
     * 
     * @return hex string representation, or null if no original bytes
     */
    public String getOriginalBytesHex() {
        if (originalBytes == null) {
            return null;
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < originalBytes.length; i++) {
            if (i > 0) {
                sb.append(" ");
            }
            sb.append(String.format("%02X", originalBytes[i] & 0xFF));
        }
        return sb.toString();
    }
}