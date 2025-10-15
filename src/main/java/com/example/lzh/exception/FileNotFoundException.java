package com.example.lzh.exception;

import com.example.lzh.util.ErrorContext;
import java.util.List;

/**
 * Exception thrown when a requested file is not found in the LZH archive.
 * This exception is thrown when attempting to extract a specific file
 * that does not exist in the archive.
 * Enhanced with detailed error context information.
 * 要件 7.1, 7.2, 7.3, 7.4 に対応
 */
public class FileNotFoundException extends LzhException {
    
    private String requestedFileName;
    private List<String> availableFiles;
    
    /**
     * Constructs a new FileNotFoundException with no detail message.
     */
    public FileNotFoundException() {
        super("File not found in archive", "FILE_NOT_FOUND");
    }
    
    /**
     * Constructs a new FileNotFoundException with the specified detail message.
     * 
     * @param message the detail message
     */
    public FileNotFoundException(String message) {
        super(message, "FILE_NOT_FOUND");
    }
    
    /**
     * Constructs a new FileNotFoundException with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public FileNotFoundException(String message, Throwable cause) {
        super(message, "FILE_NOT_FOUND", cause);
    }
    
    /**
     * Constructs a new FileNotFoundException with the specified cause.
     * 
     * @param cause the cause of this exception
     */
    public FileNotFoundException(Throwable cause) {
        super("File not found in archive", "FILE_NOT_FOUND", cause);
    }
    
    /**
     * Constructs a new FileNotFoundException with error context.
     * 
     * @param message the detail message
     * @param errorContext the error context
     */
    public FileNotFoundException(String message, ErrorContext errorContext) {
        super(message, errorContext);
        setErrorCode("FILE_NOT_FOUND");
    }
    
    /**
     * Constructs a new FileNotFoundException with error context and cause.
     * 
     * @param message the detail message
     * @param errorContext the error context
     * @param cause the cause of this exception
     */
    public FileNotFoundException(String message, ErrorContext errorContext, Throwable cause) {
        super(message, errorContext, cause);
        setErrorCode("FILE_NOT_FOUND");
    }
    
    /**
     * Creates a FileNotFoundException for a specific file.
     * 
     * @param fileName the name of the file that was not found
     * @param errorContext error context
     * @return configured exception
     */
    public static FileNotFoundException forFile(String fileName, ErrorContext errorContext) {
        String message = "File not found in archive: " + fileName;
        FileNotFoundException ex = new FileNotFoundException(message, errorContext);
        ex.requestedFileName = fileName;
        return ex;
    }
    
    /**
     * Creates a FileNotFoundException with suggestions.
     * 
     * @param fileName the name of the file that was not found
     * @param availableFiles list of available files in the archive
     * @param errorContext error context
     * @return configured exception
     */
    public static FileNotFoundException forFileWithSuggestions(String fileName, List<String> availableFiles, ErrorContext errorContext) {
        StringBuilder message = new StringBuilder();
        message.append("File not found in archive: ").append(fileName);
        
        if (availableFiles != null && !availableFiles.isEmpty()) {
            message.append(". Available files: ");
            for (int i = 0; i < Math.min(availableFiles.size(), 10); i++) {
                if (i > 0) {
                    message.append(", ");
                }
                message.append(availableFiles.get(i));
            }
            if (availableFiles.size() > 10) {
                message.append(" (and ").append(availableFiles.size() - 10).append(" more)");
            }
        }
        
        FileNotFoundException ex = new FileNotFoundException(message.toString(), errorContext);
        ex.requestedFileName = fileName;
        ex.availableFiles = availableFiles;
        return ex;
    }
    
    /**
     * Creates a FileNotFoundException for empty archive.
     * 
     * @param errorContext error context
     * @return configured exception
     */
    public static FileNotFoundException forEmptyArchive(ErrorContext errorContext) {
        return new FileNotFoundException("Archive is empty - no files available for extraction", errorContext);
    }
    
    /**
     * Gets the name of the file that was requested but not found.
     * 
     * @return the requested file name, or null if not set
     */
    public String getRequestedFileName() {
        return requestedFileName;
    }
    
    /**
     * Gets the list of available files in the archive.
     * 
     * @return list of available files, or null if not set
     */
    public List<String> getAvailableFiles() {
        return availableFiles;
    }
    
    /**
     * Finds similar file names in the available files list.
     * 
     * @return list of similar file names, or empty list if none found
     */
    public List<String> getSimilarFileNames() {
        if (requestedFileName == null || availableFiles == null) {
            return java.util.Collections.emptyList();
        }
        
        java.util.List<String> similar = new java.util.ArrayList<>();
        String lowerRequested = requestedFileName.toLowerCase();
        
        for (String available : availableFiles) {
            String lowerAvailable = available.toLowerCase();
            
            // Check for partial matches
            if (lowerAvailable.contains(lowerRequested) || lowerRequested.contains(lowerAvailable)) {
                similar.add(available);
            }
            // Check for similar extensions
            else if (getFileExtension(lowerRequested).equals(getFileExtension(lowerAvailable))) {
                similar.add(available);
            }
        }
        
        return similar;
    }
    
    /**
     * Gets the file extension from a file name.
     * 
     * @param fileName the file name
     * @return the extension (without dot), or empty string if no extension
     */
    private String getFileExtension(String fileName) {
        if (fileName == null) {
            return "";
        }
        int lastDot = fileName.lastIndexOf('.');
        return lastDot >= 0 ? fileName.substring(lastDot + 1) : "";
    }
}