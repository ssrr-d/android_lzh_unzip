package com.example.lzh.exception;

import com.example.lzh.util.ErrorContext;

/**
 * Base exception class for all LZH-related errors.
 * This is the root exception for all LZH library operations.
 * Enhanced with detailed error context information.
 * 要件 7.1, 7.2, 7.3, 7.4 に対応
 */
public class LzhException extends Exception {
    
    private ErrorContext errorContext;
    private String errorCode;
    private long timestamp;
    
    /**
     * Constructs a new LzhException with no detail message.
     */
    public LzhException() {
        super();
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Constructs a new LzhException with the specified detail message.
     * 
     * @param message the detail message
     */
    public LzhException(String message) {
        super(message);
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Constructs a new LzhException with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public LzhException(String message, Throwable cause) {
        super(message, cause);
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Constructs a new LzhException with the specified cause.
     * 
     * @param cause the cause of this exception
     */
    public LzhException(Throwable cause) {
        super(cause);
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Constructs a new LzhException with error context.
     * 
     * @param message the detail message
     * @param errorContext the error context
     */
    public LzhException(String message, ErrorContext errorContext) {
        super(errorContext != null ? errorContext.createDetailedMessage(message) : message);
        this.errorContext = errorContext;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Constructs a new LzhException with error context and cause.
     * 
     * @param message the detail message
     * @param errorContext the error context
     * @param cause the cause of this exception
     */
    public LzhException(String message, ErrorContext errorContext, Throwable cause) {
        super(errorContext != null ? errorContext.createDetailedMessage(message) : message, cause);
        this.errorContext = errorContext;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Constructs a new LzhException with error code.
     * 
     * @param message the detail message
     * @param errorCode the error code
     */
    public LzhException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Constructs a new LzhException with error code and cause.
     * 
     * @param message the detail message
     * @param errorCode the error code
     * @param cause the cause of this exception
     */
    public LzhException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Gets the error context associated with this exception.
     * 
     * @return the error context, or null if not set
     */
    public ErrorContext getErrorContext() {
        return errorContext;
    }
    
    /**
     * Sets the error context for this exception.
     * 
     * @param errorContext the error context
     */
    public void setErrorContext(ErrorContext errorContext) {
        this.errorContext = errorContext;
    }
    
    /**
     * Gets the error code associated with this exception.
     * 
     * @return the error code, or null if not set
     */
    public String getErrorCode() {
        return errorCode;
    }
    
    /**
     * Sets the error code for this exception.
     * 
     * @param errorCode the error code
     */
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    
    /**
     * Gets the timestamp when this exception was created.
     * 
     * @return the timestamp in milliseconds
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * Creates a detailed error message including context information.
     * 
     * @return detailed error message
     */
    public String getDetailedMessage() {
        StringBuilder sb = new StringBuilder();
        
        if (errorCode != null) {
            sb.append("[").append(errorCode).append("] ");
        }
        
        sb.append(getMessage());
        
        if (errorContext != null) {
            sb.append("\n").append(errorContext.createDebugInfo());
        }
        
        if (getCause() != null) {
            sb.append("\n原因: ").append(getCause().getClass().getSimpleName());
            if (getCause().getMessage() != null) {
                sb.append(" - ").append(getCause().getMessage());
            }
        }
        
        return sb.toString();
    }
    
    /**
     * Creates a user-friendly error message.
     * 
     * @return user-friendly error message
     */
    public String getUserFriendlyMessage() {
        String baseMessage = getMessage();
        
        if (errorCode != null) {
            switch (errorCode) {
                case "INVALID_ARCHIVE":
                    return "指定されたファイルは有効なLZHアーカイブではありません。";
                case "CORRUPTED_ARCHIVE":
                    return "LZHアーカイブが破損しているか、データが不完全です。";
                case "UNSUPPORTED_METHOD":
                    return "サポートされていない圧縮方式が使用されています。";
                case "FILE_NOT_FOUND":
                    return "指定されたファイルがアーカイブ内に見つかりません。";
                case "ENCODING_ERROR":
                    return "ファイル名の文字エンコーディングに問題があります。";
                case "IO_ERROR":
                    return "ファイルの読み書き中にエラーが発生しました。";
                case "PERMISSION_ERROR":
                    return "ファイルアクセスの権限がありません。";
                default:
                    return baseMessage;
            }
        }
        
        return baseMessage;
    }
}