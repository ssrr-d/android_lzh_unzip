package com.example.lzh.exception;

import com.example.lzh.util.ErrorContext;

/**
 * Exception thrown when the input file is not a valid LZH archive format.
 * This exception is thrown when the file header or structure doesn't match
 * the expected LZH format specifications.
 * Enhanced with detailed error context information.
 * 要件 7.1, 7.2, 7.3, 7.4 に対応
 */
public class InvalidArchiveException extends LzhException {
    
    /**
     * Constructs a new InvalidArchiveException with no detail message.
     */
    public InvalidArchiveException() {
        super("Invalid LZH archive format", "INVALID_ARCHIVE");
    }
    
    /**
     * Constructs a new InvalidArchiveException with the specified detail message.
     * 
     * @param message the detail message
     */
    public InvalidArchiveException(String message) {
        super(message, "INVALID_ARCHIVE");
    }
    
    /**
     * Constructs a new InvalidArchiveException with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public InvalidArchiveException(String message, Throwable cause) {
        super(message, "INVALID_ARCHIVE", cause);
    }
    
    /**
     * Constructs a new InvalidArchiveException with the specified cause.
     * 
     * @param cause the cause of this exception
     */
    public InvalidArchiveException(Throwable cause) {
        super("Invalid LZH archive format", "INVALID_ARCHIVE", cause);
    }
    
    /**
     * Constructs a new InvalidArchiveException with error context.
     * 
     * @param message the detail message
     * @param errorContext the error context
     */
    public InvalidArchiveException(String message, ErrorContext errorContext) {
        super(message, errorContext);
        setErrorCode("INVALID_ARCHIVE");
    }
    
    /**
     * Constructs a new InvalidArchiveException with error context and cause.
     * 
     * @param message the detail message
     * @param errorContext the error context
     * @param cause the cause of this exception
     */
    public InvalidArchiveException(String message, ErrorContext errorContext, Throwable cause) {
        super(message, errorContext, cause);
        setErrorCode("INVALID_ARCHIVE");
    }
    
    /**
     * Creates an InvalidArchiveException for header validation errors.
     * 
     * @param headerIssue description of the header issue
     * @param errorContext error context
     * @return configured exception
     */
    public static InvalidArchiveException forHeaderError(String headerIssue, ErrorContext errorContext) {
        return new InvalidArchiveException("Invalid LZH header: " + headerIssue, errorContext);
    }
    
    /**
     * Creates an InvalidArchiveException for file format errors.
     * 
     * @param formatIssue description of the format issue
     * @param errorContext error context
     * @return configured exception
     */
    public static InvalidArchiveException forFormatError(String formatIssue, ErrorContext errorContext) {
        return new InvalidArchiveException("Invalid file format: " + formatIssue, errorContext);
    }
    
    /**
     * Creates an InvalidArchiveException for empty archive errors.
     * 
     * @param errorContext error context
     * @return configured exception
     */
    public static InvalidArchiveException forEmptyArchive(ErrorContext errorContext) {
        return new InvalidArchiveException("Archive is empty or contains no valid entries", errorContext);
    }
    
    /**
     * Creates an InvalidArchiveException for input validation errors.
     * 
     * @param validationIssue description of the validation issue
     * @param errorContext error context
     * @return configured exception
     */
    public static InvalidArchiveException forInputValidation(String validationIssue, ErrorContext errorContext) {
        return new InvalidArchiveException("Input validation failed: " + validationIssue, errorContext);
    }
}