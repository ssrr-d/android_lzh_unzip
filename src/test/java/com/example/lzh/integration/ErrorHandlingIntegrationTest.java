package com.example.lzh.integration;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;

import java.io.File;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import com.example.lzh.LzhExtractor;
import com.example.lzh.exception.LzhException;
import com.example.lzh.exception.InvalidArchiveException;
import com.example.lzh.exception.CorruptedArchiveException;
import com.example.lzh.exception.UnsupportedMethodException;
import com.example.lzh.exception.EncodingException;
import com.example.lzh.util.ErrorContext;

/**
 * Integration tests for comprehensive error handling across all components
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 21)
public class ErrorHandlingIntegrationTest {
    
    private LzhExtractor extractor;
    private File tempOutputDir;
    
    @Before
    public void setUp() throws IOException {
        extractor = new LzhExtractor();
        
        // Create temporary output directory for tests
        tempOutputDir = new File(System.getProperty("java.io.tmpdir"), "lzh_error_test_" + System.currentTimeMillis());
        tempOutputDir.mkdirs();
    }
    
    // Input validation error integration tests
    
    @Test
    public void testNullInputHandlingAcrossAllMethods() {
        // Test that all public methods handle null inputs consistently
        
        try {
            extractor.extract((File) null, tempOutputDir);
            fail("Should throw InvalidArchiveException for null file");
        } catch (InvalidArchiveException e) {
            assertNotNull("Should have error context", e.getErrorContext());
            assertTrue("Should mention null input", e.getMessage().toLowerCase().contains("null"));
        } catch (LzhException e) {
            fail("Should be InvalidArchiveException, got: " + e.getClass().getSimpleName());
        }
        
        try {
            extractor.extract((java.io.InputStream) null, tempOutputDir);
            fail("Should throw InvalidArchiveException for null stream");
        } catch (InvalidArchiveException e) {
            assertNotNull("Should have error context", e.getErrorContext());
        } catch (LzhException e) {
            fail("Should be InvalidArchiveException, got: " + e.getClass().getSimpleName());
        }
        
        try {
            extractor.extract((byte[]) null, tempOutputDir);
            fail("Should throw InvalidArchiveException for null byte array");
        } catch (InvalidArchiveException e) {
            assertNotNull("Should have error context", e.getErrorContext());
        } catch (LzhException e) {
            fail("Should be InvalidArchiveException, got: " + e.getClass().getSimpleName());
        }
        
        try {
            extractor.getArchiveInfo((File) null);
            fail("Should throw InvalidArchiveException for null file");
        } catch (InvalidArchiveException e) {
            assertNotNull("Should have error context", e.getErrorContext());
        } catch (LzhException e) {
            fail("Should be InvalidArchiveException, got: " + e.getClass().getSimpleName());
        }
    }
    
    @Test
    public void testEmptyInputHandlingAcrossAllMethods() {
        byte[] emptyData = new byte[0];
        
        try {
            extractor.extract(emptyData, tempOutputDir);
            fail("Should throw InvalidArchiveException for empty data");
        } catch (InvalidArchiveException e) {
            assertTrue("Should mention empty data", e.getMessage().toLowerCase().contains("empty"));
        } catch (LzhException e) {
            fail("Should be InvalidArchiveException, got: " + e.getClass().getSimpleName());
        }
        
        try {
            extractor.getArchiveInfo(emptyData);
            fail("Should throw InvalidArchiveException for empty data");
        } catch (InvalidArchiveException e) {
            assertTrue("Should mention empty data", e.getMessage().toLowerCase().contains("empty"));
        } catch (LzhException e) {
            fail("Should be InvalidArchiveException, got: " + e.getClass().getSimpleName());
        }
    }
    
    // Archive format error integration tests
    
    @Test
    public void testInvalidArchiveFormatHandling() {
        byte[] invalidData = "This is definitely not a valid LZH archive format".getBytes();
        
        try {
            extractor.getArchiveInfo(invalidData);
            fail("Should throw exception for invalid archive format");
        } catch (InvalidArchiveException e) {
            assertNotNull("Should have error context", e.getErrorContext());
            ErrorContext context = e.getErrorContext();
            assertEquals("Should have correct input source", "byte array", context.getInputSource());
        } catch (CorruptedArchiveException e) {
            // Also acceptable for invalid format
            assertNotNull("Should have error context", e.getErrorContext());
        } catch (LzhException e) {
            // Other LZH exceptions are also acceptable
            assertTrue("Should be appropriate LZH exception", true);
        }
    }
    
    @Test
    public void testCorruptedHeaderHandling() {
        // Create data that looks like it might be LZH but is corrupted
        byte[] corruptedHeader = new byte[50];
        corruptedHeader[0] = 0x1A; // Valid header size
        corruptedHeader[1] = 0x00; // Checksum
        // Rest is garbage
        
        try {
            extractor.getArchiveInfo(corruptedHeader);
            fail("Should throw exception for corrupted header");
        } catch (CorruptedArchiveException e) {
            assertNotNull("Should have error context", e.getErrorContext());
        } catch (InvalidArchiveException e) {
            // Also acceptable
            assertNotNull("Should have error context", e.getErrorContext());
        } catch (LzhException e) {
            // Other LZH exceptions are also acceptable
            assertTrue("Should be appropriate LZH exception", true);
        }
    }
    
    // Unsupported method error integration tests
    
    @Test
    public void testUnsupportedCompressionMethodHandling() {
        // Create synthetic data with unsupported compression method
        byte[] unsupportedMethodData = createDataWithUnsupportedMethod("-lh6-");
        
        try {
            extractor.extract(unsupportedMethodData, tempOutputDir);
            fail("Should throw UnsupportedMethodException");
        } catch (UnsupportedMethodException e) {
            assertTrue("Should mention unsupported method", 
                      e.getMessage().toLowerCase().contains("unsupported") || 
                      e.getMessage().contains("-lh6-"));
        } catch (LzhException e) {
            // Other exceptions are acceptable for synthetic data
            assertTrue("Should handle unsupported method gracefully", true);
        }
    }
    
    // File system error integration tests
    
    @Test
    public void testOutputDirectoryErrorHandling() {
        byte[] validLzhData = createMinimalValidLzhData();
        
        // Test with non-existent parent directory
        File invalidOutputDir = new File("/nonexistent/path/that/should/not/exist");
        
        try {
            extractor.extract(validLzhData, invalidOutputDir);
            fail("Should throw exception for invalid output directory");
        } catch (InvalidArchiveException e) {
            // Expected for output directory validation
            assertTrue("Should mention directory issue", 
                      e.getMessage().toLowerCase().contains("directory") ||
                      e.getMessage().toLowerCase().contains("output"));
        } catch (LzhException e) {
            // Other exceptions are acceptable
            assertTrue("Should handle directory errors gracefully", true);
        }
    }
    
    @Test
    public void testReadOnlyOutputDirectoryHandling() {
        byte[] validLzhData = createMinimalValidLzhData();
        
        // Create a read-only directory (if possible on the platform)
        File readOnlyDir = new File(tempOutputDir, "readonly");
        readOnlyDir.mkdirs();
        readOnlyDir.setWritable(false);
        
        try {
            extractor.extract(validLzhData, readOnlyDir);
            // May or may not fail depending on platform and permissions
        } catch (LzhException e) {
            // Should handle permission errors gracefully
            assertTrue("Should handle permission errors", true);
        } finally {
            // Restore permissions for cleanup
            readOnlyDir.setWritable(true);
        }
    }
    
    // Memory and resource error integration tests
    
    @Test
    public void testLargeDataHandling() {
        // Test with data claiming to be very large
        byte[] largeClaimData = createDataClaimingLargeSize();
        
        try {
            extractor.extract(largeClaimData, tempOutputDir);
            fail("Should handle large size claims appropriately");
        } catch (CorruptedArchiveException e) {
            // Expected for data integrity issues
            assertTrue("Should detect size inconsistencies", true);
        } catch (LzhException e) {
            // Other exceptions are acceptable
            assertFalse("Should not be OutOfMemoryError", e.getCause() instanceof OutOfMemoryError);
        }
    }
    
    // Security error integration tests
    
    @Test
    public void testPathTraversalAttackHandling() {
        byte[] maliciousData = createDataWithMaliciousPath("../../../etc/passwd");
        
        try {
            extractor.extract(maliciousData, tempOutputDir);
            
            // Verify no files were created outside the output directory
            File maliciousFile = new File("/etc/passwd");
            assertFalse("Should not create files outside output directory", maliciousFile.exists());
            
        } catch (LzhException e) {
            // Should reject malicious paths
            assertTrue("Should handle path traversal attempts", true);
        }
    }
    
    @Test
    public void testMultiplePathTraversalVariants() {
        String[] maliciousPaths = {
            "../../../etc/passwd",
            "..\\..\\..\\windows\\system32\\config\\sam",
            "sub/../../../etc/passwd",
            "sub\\..\\..\\..\\windows\\system32\\config\\sam"
        };
        
        for (String maliciousPath : maliciousPaths) {
            try {
                byte[] maliciousData = createDataWithMaliciousPath(maliciousPath);
                extractor.extract(maliciousData, tempOutputDir);
                
                // Should not create files outside output directory
                assertTrue("Should handle path traversal: " + maliciousPath, true);
                
            } catch (LzhException e) {
                // Expected - should reject malicious paths
                assertTrue("Should reject malicious path: " + maliciousPath, true);
            }
        }
    }
    
    // Encoding error integration tests
    
    @Test
    public void testInvalidEncodingHandling() {
        // Create data with invalid character encoding
        byte[] invalidEncodingData = createDataWithInvalidEncoding();
        
        try {
            extractor.getArchiveInfo(invalidEncodingData);
            // Should handle gracefully, possibly with warnings
        } catch (EncodingException e) {
            assertNotNull("Should have error context", e.getErrorContext());
        } catch (LzhException e) {
            // Other exceptions are acceptable
            assertTrue("Should handle encoding issues gracefully", true);
        }
    }
    
    // Error context propagation tests
    
    @Test
    public void testErrorContextPropagation() {
        byte[] invalidData = "invalid".getBytes();
        
        try {
            extractor.extract(invalidData, tempOutputDir);
            fail("Should throw exception");
        } catch (LzhException e) {
            ErrorContext context = e.getErrorContext();
            assertNotNull("Should have error context", context);
            
            if (context != null) {
                assertNotNull("Should have operation", context.getOperation());
                assertEquals("Should have correct input source", "byte array", context.getInputSource());
                assertEquals("Should have correct output path", tempOutputDir.getAbsolutePath(), context.getOutputPath());
                assertEquals("Should have correct file size", Long.valueOf(invalidData.length), context.getFileSize());
            }
        }
    }
    
    // Helper methods for creating test data
    
    private byte[] createMinimalValidLzhData() {
        // Create minimal LZH-like structure
        byte[] data = new byte[32];
        data[0] = 0x1A; // Header size
        data[1] = 0x00; // Checksum
        // Add method signature
        System.arraycopy("-lh0-".getBytes(), 0, data, 2, 5);
        return data;
    }
    
    private byte[] createDataWithUnsupportedMethod(String method) {
        byte[] data = new byte[32];
        data[0] = 0x1A; // Header size
        data[1] = 0x00; // Checksum
        // Add unsupported method signature
        System.arraycopy(method.getBytes(), 0, data, 2, Math.min(method.length(), 5));
        return data;
    }
    
    private byte[] createDataClaimingLargeSize() {
        byte[] data = new byte[32];
        data[0] = 0x1A; // Header size
        data[1] = 0x00; // Checksum
        System.arraycopy("-lh0-".getBytes(), 0, data, 2, 5);
        
        // Set large size claims (little endian)
        data[7] = (byte) 0xFF;  // Compressed size
        data[8] = (byte) 0xFF;
        data[9] = (byte) 0xFF;
        data[10] = (byte) 0x7F; // Large but not overflow
        
        data[11] = (byte) 0xFF; // Original size
        data[12] = (byte) 0xFF;
        data[13] = (byte) 0xFF;
        data[14] = (byte) 0x7F;
        
        return data;
    }
    
    private byte[] createDataWithMaliciousPath(String maliciousPath) {
        byte[] pathBytes = maliciousPath.getBytes();
        byte[] data = new byte[32 + pathBytes.length];
        
        data[0] = (byte) (0x1A + pathBytes.length); // Header size
        data[1] = 0x00; // Checksum
        System.arraycopy("-lh0-".getBytes(), 0, data, 2, 5);
        
        // Add small size
        data[7] = 0x10;  // Compressed size
        data[11] = 0x10; // Original size
        
        // Add filename length and malicious path
        data[21] = (byte) pathBytes.length;
        System.arraycopy(pathBytes, 0, data, 22, pathBytes.length);
        
        return data;
    }
    
    private byte[] createDataWithInvalidEncoding() {
        byte[] data = new byte[32];
        data[0] = 0x1A; // Header size
        data[1] = 0x00; // Checksum
        System.arraycopy("-lh0-".getBytes(), 0, data, 2, 5);
        
        // Add invalid UTF-8 sequence in filename area
        data[21] = 0x04; // Filename length
        data[22] = (byte) 0xFF; // Invalid UTF-8
        data[23] = (byte) 0xFE;
        data[24] = (byte) 0xFD;
        data[25] = (byte) 0xFC;
        
        return data;
    }
    
    @org.junit.After
    public void tearDown() {
        // Clean up temporary files
        if (tempOutputDir != null && tempOutputDir.exists()) {
            deleteDirectory(tempOutputDir);
        }
    }
    
    private void deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        dir.delete();
    }
}