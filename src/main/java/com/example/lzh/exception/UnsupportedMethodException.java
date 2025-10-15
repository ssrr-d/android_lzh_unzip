package com.example.lzh.exception;

import com.example.lzh.util.ErrorContext;

/**
 * Exception thrown when an unsupported compression method is encountered.
 * This exception is thrown when the LZH archive uses a compression method
 * that is not supported by this library (e.g., methods other than LH0, LH1, LH5).
 * Enhanced with detailed error context information.
 * 要件 7.1, 7.2, 7.3, 7.4 に対応
 */
public class UnsupportedMethodException extends LzhException {
    
    private static final String[] SUPPORTED_METHODS = {"-lh0-", "-lh1-", "-lh5-"};
    
    /**
     * Constructs a new UnsupportedMethodException with no detail message.
     */
    public UnsupportedMethodException() {
        super("Unsupported compression method", "UNSUPPORTED_METHOD");
    }
    
    /**
     * Constructs a new UnsupportedMethodException with the specified detail message.
     * 
     * @param message the detail message
     */
    public UnsupportedMethodException(String message) {
        super(message, "UNSUPPORTED_METHOD");
    }
    
    /**
     * Constructs a new UnsupportedMethodException with the specified compression method.
     * 
     * @param method the unsupported compression method
     */
    public UnsupportedMethodException(String method) {
        super(createMethodMessage(method), "UNSUPPORTED_METHOD");
    }
    
    /**
     * Constructs a new UnsupportedMethodException with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public UnsupportedMethodException(String message, Throwable cause) {
        super(message, "UNSUPPORTED_METHOD", cause);
    }
    
    /**
     * Constructs a new UnsupportedMethodException with the specified cause.
     * 
     * @param cause the cause of this exception
     */
    public UnsupportedMethodException(Throwable cause) {
        super("Unsupported compression method", "UNSUPPORTED_METHOD", cause);
    }
    
    /**
     * Constructs a new UnsupportedMethodException with error context.
     * 
     * @param message the detail message
     * @param errorContext the error context
     */
    public UnsupportedMethodException(String message, ErrorContext errorContext) {
        super(message, errorContext);
        setErrorCode("UNSUPPORTED_METHOD");
    }
    
    /**
     * Constructs a new UnsupportedMethodException with error context and cause.
     * 
     * @param message the detail message
     * @param errorContext the error context
     * @param cause the cause of this exception
     */
    public UnsupportedMethodException(String message, ErrorContext errorContext, Throwable cause) {
        super(message, errorContext, cause);
        setErrorCode("UNSUPPORTED_METHOD");
    }
    
    /**
     * Creates an UnsupportedMethodException for a specific compression method.
     * 
     * @param method the unsupported compression method
     * @param errorContext error context
     * @return configured exception
     */
    public static UnsupportedMethodException forMethod(String method, ErrorContext errorContext) {
        String message = createMethodMessage(method);
        return new UnsupportedMethodException(message, errorContext);
    }
    
    /**
     * Creates a detailed message for unsupported compression method.
     * 
     * @param method the unsupported method
     * @return detailed message
     */
    private static String createMethodMessage(String method) {
        StringBuilder sb = new StringBuilder();
        sb.append("Unsupported compression method: ").append(method != null ? method : "null");
        sb.append(". Supported methods are: ");
        for (int i = 0; i < SUPPORTED_METHODS.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(SUPPORTED_METHODS[i]);
        }
        return sb.toString();
    }
    
    /**
     * Gets the list of supported compression methods.
     * 
     * @return array of supported method strings
     */
    public static String[] getSupportedMethods() {
        return SUPPORTED_METHODS.clone();
    }
    
    /**
     * Checks if a compression method is supported.
     * 
     * @param method the compression method to check
     * @return true if supported, false otherwise
     */
    public static boolean isMethodSupported(String method) {
        if (method == null) {
            return false;
        }
        for (String supportedMethod : SUPPORTED_METHODS) {
            if (supportedMethod.equalsIgnoreCase(method)) {
                return true;
            }
        }
        return false;
    }
}