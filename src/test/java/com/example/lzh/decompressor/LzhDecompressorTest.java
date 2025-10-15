package com.example.lzh.decompressor;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.example.lzh.exception.UnsupportedMethodException;

/**
 * Unit tests for LzhDecompressor factory and common functionality
 */
public class LzhDecompressorTest {
    
    // Factory method tests
    
    @Test
    public void testCreateDecompressorLh0() throws UnsupportedMethodException {
        LzhDecompressor decompressor = LzhDecompressor.createDecompressor("-lh0-");
        assertNotNull("Should create LH0 decompressor", decompressor);
        assertTrue("Should be Lh0Decompressor instance", decompressor instanceof Lh0Decompressor);
    }
    
    @Test
    public void testCreateDecompressorLh1() throws UnsupportedMethodException {
        LzhDecompressor decompressor = LzhDecompressor.createDecompressor("-lh1-");
        assertNotNull("Should create LH1 decompressor", decompressor);
        assertTrue("Should be Lh1Decompressor instance", decompressor instanceof Lh1Decompressor);
    }
    
    @Test
    public void testCreateDecompressorLh5() throws UnsupportedMethodException {
        LzhDecompressor decompressor = LzhDecompressor.createDecompressor("-lh5-");
        assertNotNull("Should create LH5 decompressor", decompressor);
        assertTrue("Should be Lh5Decompressor instance", decompressor instanceof Lh5Decompressor);
    }
    
    @Test(expected = UnsupportedMethodException.class)
    public void testCreateDecompressorUnsupportedMethod() throws UnsupportedMethodException {
        LzhDecompressor.createDecompressor("-lh6-");
    }
    
    @Test(expected = UnsupportedMethodException.class)
    public void testCreateDecompressorNullMethod() throws UnsupportedMethodException {
        LzhDecompressor.createDecompressor(null);
    }
    
    @Test(expected = UnsupportedMethodException.class)
    public void testCreateDecompressorEmptyMethod() throws UnsupportedMethodException {
        LzhDecompressor.createDecompressor("");
    }
    
    @Test(expected = UnsupportedMethodException.class)
    public void testCreateDecompressorInvalidMethod() throws UnsupportedMethodException {
        LzhDecompressor.createDecompressor("invalid");
    }
    
    // Case sensitivity tests
    
    @Test
    public void testCreateDecompressorCaseSensitive() throws UnsupportedMethodException {
        // Test that method names are case sensitive
        try {
            LzhDecompressor.createDecompressor("-LH0-");
            fail("Should throw exception for uppercase method");
        } catch (UnsupportedMethodException e) {
            // Expected
        }
        
        try {
            LzhDecompressor.createDecompressor("-Lh0-");
            fail("Should throw exception for mixed case method");
        } catch (UnsupportedMethodException e) {
            // Expected
        }
    }
    
    // Whitespace handling tests
    
    @Test(expected = UnsupportedMethodException.class)
    public void testCreateDecompressorWithWhitespace() throws UnsupportedMethodException {
        LzhDecompressor.createDecompressor(" -lh0- ");
    }
    
    @Test(expected = UnsupportedMethodException.class)
    public void testCreateDecompressorWithInternalWhitespace() throws UnsupportedMethodException {
        LzhDecompressor.createDecompressor("-lh 0-");
    }
    
    // Buffered decompression tests
    
    @Test
    public void testDecompressWithBufferingLh0() throws Exception {
        LzhDecompressor decompressor = LzhDecompressor.createDecompressor("-lh0-");
        
        byte[] testData = "Hello, World!".getBytes();
        ByteArrayInputStream input = new ByteArrayInputStream(testData);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        
        decompressor.decompressWithBuffering(input, output, testData.length, testData.length);
        
        assertArrayEquals("Buffered decompression should produce correct output", 
                         testData, output.toByteArray());
    }
    
    @Test
    public void testDecompressWithBufferingEmptyData() throws Exception {
        LzhDecompressor decompressor = LzhDecompressor.createDecompressor("-lh0-");
        
        byte[] testData = new byte[0];
        ByteArrayInputStream input = new ByteArrayInputStream(testData);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        
        decompressor.decompressWithBuffering(input, output, 0, 0);
        
        assertEquals("Empty data should produce empty output", 0, output.size());
    }
    
    @Test
    public void testDecompressWithBufferingLargeData() throws Exception {
        LzhDecompressor decompressor = LzhDecompressor.createDecompressor("-lh0-");
        
        // Create large test data (larger than default buffer size)
        byte[] testData = new byte[16384]; // 16KB
        for (int i = 0; i < testData.length; i++) {
            testData[i] = (byte) (i % 256);
        }
        
        ByteArrayInputStream input = new ByteArrayInputStream(testData);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        
        decompressor.decompressWithBuffering(input, output, testData.length, testData.length);
        
        assertArrayEquals("Large data decompression should produce correct output", 
                         testData, output.toByteArray());
    }
    
    // Error handling tests
    
    @Test
    public void testDecompressWithNullInput() throws Exception {
        LzhDecompressor decompressor = LzhDecompressor.createDecompressor("-lh0-");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        
        try {
            decompressor.decompressWithBuffering(null, output, 0, 0);
            fail("Should throw exception for null input");
        } catch (Exception e) {
            // Expected - could be IOException or other exception
            assertTrue("Should be an appropriate exception", 
                      e instanceof IOException || e instanceof NullPointerException);
        }
    }
    
    @Test
    public void testDecompressWithNullOutput() throws Exception {
        LzhDecompressor decompressor = LzhDecompressor.createDecompressor("-lh0-");
        ByteArrayInputStream input = new ByteArrayInputStream(new byte[0]);
        
        try {
            decompressor.decompressWithBuffering(input, null, 0, 0);
            fail("Should throw exception for null output");
        } catch (Exception e) {
            // Expected - could be IOException or other exception
            assertTrue("Should be an appropriate exception", 
                      e instanceof IOException || e instanceof NullPointerException);
        }
    }
    
    // Performance and resource tests
    
    @Test
    public void testMultipleDecompressions() throws Exception {
        LzhDecompressor decompressor = LzhDecompressor.createDecompressor("-lh0-");
        
        byte[] testData = "Test data for multiple decompressions".getBytes();
        
        // Perform multiple decompressions to test resource handling
        for (int i = 0; i < 10; i++) {
            ByteArrayInputStream input = new ByteArrayInputStream(testData);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            
            decompressor.decompressWithBuffering(input, output, testData.length, testData.length);
            
            assertArrayEquals("Decompression " + i + " should produce correct output", 
                             testData, output.toByteArray());
        }
    }
    
    @Test
    public void testDecompressorReuse() throws Exception {
        // Test that the same decompressor instance can be reused
        LzhDecompressor decompressor = LzhDecompressor.createDecompressor("-lh0-");
        
        byte[] testData1 = "First test data".getBytes();
        byte[] testData2 = "Second test data with different content".getBytes();
        
        // First decompression
        ByteArrayInputStream input1 = new ByteArrayInputStream(testData1);
        ByteArrayOutputStream output1 = new ByteArrayOutputStream();
        decompressor.decompressWithBuffering(input1, output1, testData1.length, testData1.length);
        
        // Second decompression with same instance
        ByteArrayInputStream input2 = new ByteArrayInputStream(testData2);
        ByteArrayOutputStream output2 = new ByteArrayOutputStream();
        decompressor.decompressWithBuffering(input2, output2, testData2.length, testData2.length);
        
        assertArrayEquals("First decompression should be correct", testData1, output1.toByteArray());
        assertArrayEquals("Second decompression should be correct", testData2, output2.toByteArray());
    }
    
    // Edge case tests
    
    @Test
    public void testAllSupportedMethods() throws Exception {
        String[] supportedMethods = {"-lh0-", "-lh1-", "-lh5-"};
        
        for (String method : supportedMethods) {
            LzhDecompressor decompressor = LzhDecompressor.createDecompressor(method);
            assertNotNull("Should create decompressor for method: " + method, decompressor);
            
            // Test basic functionality
            byte[] testData = ("Test data for " + method).getBytes();
            
            if ("-lh0-".equals(method)) {
                // Only test LH0 since others require specific compressed data format
                ByteArrayInputStream input = new ByteArrayInputStream(testData);
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                
                decompressor.decompressWithBuffering(input, output, testData.length, testData.length);
                assertArrayEquals("Method " + method + " should work correctly", 
                                 testData, output.toByteArray());
            }
        }
    }
    
    @Test
    public void testMethodNameVariations() {
        String[] invalidMethods = {
            "-lh0",     // Missing trailing dash
            "lh0-",     // Missing leading dash
            "-lh0--",   // Extra dash
            "--lh0-",   // Extra leading dash
            "-lh00-",   // Extra zero
            "-lh-0-",   // Extra dash in middle
            "-LH0-",    // Uppercase
            "-lH0-",    // Mixed case
            "-lh0-\n",  // With newline
            "-lh0-\t",  // With tab
        };
        
        for (String method : invalidMethods) {
            try {
                LzhDecompressor.createDecompressor(method);
                fail("Should throw exception for invalid method: '" + method + "'");
            } catch (UnsupportedMethodException e) {
                // Expected
            }
        }
    }
}