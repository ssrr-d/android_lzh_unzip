package com.example.lzh.exception;

import org.junit.Test;
import static org.junit.Assert.*;

import com.example.lzh.util.ErrorContext;

/**
 * Unit tests for LzhException and its subclasses
 */
public class LzhExceptionTest {
    
    // LzhException basic tests
    
    @Test
    public void testLzhExceptionWithMessage() {
        String message = "Test exception message";
        LzhException exception = new LzhException(message);
        
        assertEquals("Message should match", message, exception.getMessage());
        assertNull("Cause should be null", exception.getCause());
        assertNull("Error context should be null", exception.getErrorContext());
    }
    
    @Test
    public void testLzhExceptionWithMessageAndCause() {
        String message = "Test exception message";
        Throwable cause = new RuntimeException("Root cause");
        LzhException exception = new LzhException(message, cause);
        
        assertEquals("Message should match", message, exception.getMessage());
        assertEquals("Cause should match", cause, exception.getCause());
        assertNull("Error context should be null", exception.getErrorContext());
    }
    
    @Test
    public void testLzhExceptionWithMessageAndContext() {
        String message = "Test exception message";
        ErrorContext context = new ErrorContext("Test operation");
        LzhException exception = new LzhException(message, context);
        
        assertEquals("Message should match", message, exception.getMessage());
        assertNull("Cause should be null", exception.getCause());
        assertEquals("Error context should match", context, exception.getErrorContext());
    }
    
    @Test
    public void testLzhExceptionWithMessageContextAndCause() {
        String message = "Test exception message";
        ErrorContext context = new ErrorContext("Test operation");
        Throwable cause = new RuntimeException("Root cause");
        LzhException exception = new LzhException(message, context, cause);
        
        assertEquals("Message should match", message, exception.getMessage());
        assertEquals("Cause should match", cause, exception.getCause());
        assertEquals("Error context should match", context, exception.getErrorContext());
    }
    
    @Test
    public void testLzhExceptionSetErrorContext() {
        LzhException exception = new LzhException("Test message");
        ErrorContext context = new ErrorContext("Test operation");
        
        exception.setErrorContext(context);
        assertEquals("Error context should be set", context, exception.getErrorContext());
    }
    
    // InvalidArchiveException tests
    
    @Test
    public void testInvalidArchiveExceptionBasic() {
        String message = "Invalid archive format";
        InvalidArchiveException exception = new InvalidArchiveException(message);
        
        assertEquals("Message should match", message, exception.getMessage());
        assertTrue("Should be instance of LzhException", exception instanceof LzhException);
    }
    
    @Test
    public void testInvalidArchiveExceptionWithCause() {
        String message = "Invalid archive format";
        Throwable cause = new IOException("File read error");
        InvalidArchiveException exception = new InvalidArchiveException(message, cause);
        
        assertEquals("Message should match", message, exception.getMessage());
        assertEquals("Cause should match", cause, exception.getCause());
    }
    
    @Test
    public void testInvalidArchiveExceptionForInputValidation() {
        String message = "Input validation failed";
        ErrorContext context = new ErrorContext("Input validation");
        InvalidArchiveException exception = InvalidArchiveException.forInputValidation(message, context);
        
        assertEquals("Message should match", message, exception.getMessage());
        assertEquals("Error context should match", context, exception.getErrorContext());
    }
    
    @Test
    public void testInvalidArchiveExceptionForEmptyArchive() {
        ErrorContext context = new ErrorContext("Archive parsing");
        InvalidArchiveException exception = InvalidArchiveException.forEmptyArchive(context);
        
        assertTrue("Message should indicate empty archive", 
                  exception.getMessage().toLowerCase().contains("empty"));
        assertEquals("Error context should match", context, exception.getErrorContext());
    }
    
    // CorruptedArchiveException tests
    
    @Test
    public void testCorruptedArchiveExceptionBasic() {
        String message = "Archive is corrupted";
        CorruptedArchiveException exception = new CorruptedArchiveException(message);
        
        assertEquals("Message should match", message, exception.getMessage());
        assertTrue("Should be instance of LzhException", exception instanceof LzhException);
    }
    
    @Test
    public void testCorruptedArchiveExceptionWithCause() {
        String message = "Archive is corrupted";
        Throwable cause = new IOException("Checksum mismatch");
        CorruptedArchiveException exception = new CorruptedArchiveException(message, cause);
        
        assertEquals("Message should match", message, exception.getMessage());
        assertEquals("Cause should match", cause, exception.getCause());
    }
    
    @Test
    public void testCorruptedArchiveExceptionForDataIntegrityError() {
        String message = "Data integrity check failed";
        ErrorContext context = new ErrorContext("Data validation");
        CorruptedArchiveException exception = CorruptedArchiveException.forDataIntegrityError(message, context);
        
        assertEquals("Message should match", message, exception.getMessage());
        assertEquals("Error context should match", context, exception.getErrorContext());
    }
    
    @Test
    public void testCorruptedArchiveExceptionForIncompleteData() {
        long expected = 1000;
        long actual = 500;
        ErrorContext context = new ErrorContext("Data reading");
        CorruptedArchiveException exception = CorruptedArchiveException.forIncompleteData(expected, actual, context);
        
        String message = exception.getMessage();
        assertTrue("Message should contain expected size", message.contains(String.valueOf(expected)));
        assertTrue("Message should contain actual size", message.contains(String.valueOf(actual)));
        assertEquals("Error context should match", context, exception.getErrorContext());
    }
    
    // UnsupportedMethodException tests
    
    @Test
    public void testUnsupportedMethodExceptionBasic() {
        String message = "Unsupported compression method";
        UnsupportedMethodException exception = new UnsupportedMethodException(message);
        
        assertEquals("Message should match", message, exception.getMessage());
        assertTrue("Should be instance of LzhException", exception instanceof LzhException);
    }
    
    @Test
    public void testUnsupportedMethodExceptionWithMethod() {
        String method = "-lh6-";
        UnsupportedMethodException exception = new UnsupportedMethodException("Unsupported method: " + method);
        
        assertTrue("Message should contain method", exception.getMessage().contains(method));
    }
    
    // EncodingException tests
    
    @Test
    public void testEncodingExceptionBasic() {
        String message = "Character encoding error";
        EncodingException exception = new EncodingException(message);
        
        assertEquals("Message should match", message, exception.getMessage());
        assertTrue("Should be instance of LzhException", exception instanceof LzhException);
    }
    
    @Test
    public void testEncodingExceptionWithCause() {
        String message = "Character encoding error";
        Throwable cause = new java.nio.charset.CharacterCodingException();
        EncodingException exception = new EncodingException(message, cause);
        
        assertEquals("Message should match", message, exception.getMessage());
        assertEquals("Cause should match", cause, exception.getCause());
    }
    
    // FileNotFoundException tests
    
    @Test
    public void testFileNotFoundExceptionBasic() {
        String message = "File not found in archive";
        com.example.lzh.exception.FileNotFoundException exception = 
            new com.example.lzh.exception.FileNotFoundException(message);
        
        assertEquals("Message should match", message, exception.getMessage());
        assertTrue("Should be instance of LzhException", exception instanceof LzhException);
    }
    
    @Test
    public void testFileNotFoundExceptionWithFilename() {
        String filename = "missing.txt";
        com.example.lzh.exception.FileNotFoundException exception = 
            new com.example.lzh.exception.FileNotFoundException("File not found: " + filename);
        
        assertTrue("Message should contain filename", exception.getMessage().contains(filename));
    }
    
    // Exception hierarchy tests
    
    @Test
    public void testExceptionHierarchy() {
        // Test that all custom exceptions extend LzhException
        assertTrue("InvalidArchiveException should extend LzhException", 
                  new InvalidArchiveException("test") instanceof LzhException);
        assertTrue("CorruptedArchiveException should extend LzhException", 
                  new CorruptedArchiveException("test") instanceof LzhException);
        assertTrue("UnsupportedMethodException should extend LzhException", 
                  new UnsupportedMethodException("test") instanceof LzhException);
        assertTrue("EncodingException should extend LzhException", 
                  new EncodingException("test") instanceof LzhException);
        assertTrue("FileNotFoundException should extend LzhException", 
                  new com.example.lzh.exception.FileNotFoundException("test") instanceof LzhException);
        
        // Test that LzhException extends Exception
        assertTrue("LzhException should extend Exception", 
                  new LzhException("test") instanceof Exception);
    }
    
    // Error context tests
    
    @Test
    public void testErrorContextPropagation() {
        ErrorContext context = new ErrorContext("Test operation")
            .withFileName("test.txt")
            .withCompressionMethod("-lh5-")
            .withFileSize(1024L);
        
        LzhException exception = new LzhException("Test message", context);
        
        assertEquals("Error context should be preserved", context, exception.getErrorContext());
        assertEquals("Operation should match", "Test operation", context.getOperation());
        assertEquals("Filename should match", "test.txt", context.getFileName());
        assertEquals("Compression method should match", "-lh5-", context.getCompressionMethod());
        assertEquals("File size should match", Long.valueOf(1024L), context.getFileSize());
    }
    
    @Test
    public void testExceptionChaining() {
        Throwable rootCause = new RuntimeException("Root cause");
        IOException ioCause = new IOException("IO error", rootCause);
        LzhException lzhException = new LzhException("LZH error", ioCause);
        
        assertEquals("Direct cause should be IOException", ioCause, lzhException.getCause());
        assertEquals("Root cause should be RuntimeException", rootCause, lzhException.getCause().getCause());
    }
    
    // Null handling tests
    
    @Test
    public void testExceptionsWithNullMessage() {
        LzhException exception = new LzhException(null);
        assertNull("Null message should be preserved", exception.getMessage());
        
        InvalidArchiveException invalidException = new InvalidArchiveException(null);
        assertNull("Null message should be preserved", invalidException.getMessage());
    }
    
    @Test
    public void testExceptionsWithNullContext() {
        LzhException exception = new LzhException("Test message", (ErrorContext) null);
        assertNull("Null context should be preserved", exception.getErrorContext());
        
        exception.setErrorContext(null);
        assertNull("Setting null context should work", exception.getErrorContext());
    }
    
    @Test
    public void testExceptionsWithNullCause() {
        LzhException exception = new LzhException("Test message", (Throwable) null);
        assertNull("Null cause should be preserved", exception.getCause());
    }
    
    // toString and message formatting tests
    
    @Test
    public void testExceptionToString() {
        String message = "Test exception message";
        LzhException exception = new LzhException(message);
        
        String toString = exception.toString();
        assertTrue("toString should contain class name", 
                  toString.contains("LzhException"));
        assertTrue("toString should contain message", 
                  toString.contains(message));
    }
    
    @Test
    public void testExceptionWithComplexErrorContext() {
        ErrorContext context = new ErrorContext("Complex operation")
            .withInputSource("/path/to/file.lzh")
            .withOutputPath("/output/dir")
            .withFileName("complex.txt")
            .withCompressionMethod("-lh5-")
            .withFileSize(2048L)
            .withProcessedBytes(1024L)
            .withData("customKey", "customValue");
        
        LzhException exception = new LzhException("Complex error", context);
        
        assertEquals("Error context should be complete", context, exception.getErrorContext());
        assertNotNull("Exception should have meaningful string representation", exception.toString());
    }
}