package com.example.lzh.decompressor;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.example.lzh.exception.CorruptedArchiveException;

/**
 * Unit tests for Lh0Decompressor (uncompressed data)
 */
public class Lh0DecompressorTest {
    
    private Lh0Decompressor decompressor;
    
    @Before
    public void setUp() {
        decompressor = new Lh0Decompressor();
    }
    
    // Basic functionality tests
    
    @Test
    public void testDecompressSimpleData() throws IOException {
        byte[] testData = "Hello, World!".getBytes();
        ByteArrayInputStream input = new ByteArrayInputStream(testData);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        
        decompressor.decompress(input, output, testData.length, testData.length);
        
        assertArrayEquals("Should copy data exactly", testData, output.toByteArray());
    }
    
    @Test
    public void testDecompressEmptyData() throws IOException {
        byte[] testData = new byte[0];
        ByteArrayInputStream input = new ByteArrayInputStream(testData);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        
        decompressor.decompress(input, output, 0, 0);
        
        assertEquals("Empty data should produce empty output", 0, output.size());
    }
    
    @Test
    public void testDecompressBinaryData() throws IOException {
        // Test with binary data including null bytes
        byte[] testData = {0x00, 0x01, 0x02, (byte) 0xFF, (byte) 0xFE, 0x7F, (byte) 0x80};
        ByteArrayInputStream input = new ByteArrayInputStream(testData);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        
        decompressor.decompress(input, output, testData.length, testData.length);
        
        assertArrayEquals("Should handle binary data correctly", testData, output.toByteArray());
    }
    
    @Test
    public void testDecompressLargeData() throws IOException {
        // Test with data larger than typical buffer size
        byte[] testData = new byte[16384]; // 16KB
        for (int i = 0; i < testData.length; i++) {
            testData[i] = (byte) (i % 256);
        }
        
        ByteArrayInputStream input = new ByteArrayInputStream(testData);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        
        decompressor.decompress(input, output, testData.length, testData.length);
        
        assertArrayEquals("Should handle large data correctly", testData, output.toByteArray());
    }
    
    // Size validation tests
    
    @Test(expected = CorruptedArchiveException.class)
    public void testDecompressWithMismatchedSizes() throws IOException {
        byte[] testData = "Hello, World!".getBytes();
        ByteArrayInputStream input = new ByteArrayInputStream(testData);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        
        // LH0 requires compressed size == original size
        decompressor.decompress(input, output, testData.length - 1, testData.length);
    }
    
    @Test(expected = CorruptedArchiveException.class)
    public void testDecompressWithLargerCompressedSize() throws IOException {
        byte[] testData = "Hello, World!".getBytes();
        ByteArrayInputStream input = new ByteArrayInputStream(testData);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        
        // LH0 requires compressed size == original size
        decompressor.decompress(input, output, testData.length + 1, testData.length);
    }
    
    @Test
    public void testDecompressWithZeroSizes() throws IOException {
        byte[] testData = new byte[0];
        ByteArrayInputStream input = new ByteArrayInputStream(testData);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        
        // Both sizes are zero - should be valid for LH0
        decompressor.decompress(input, output, 0, 0);
        
        assertEquals("Zero sizes should produce empty output", 0, output.size());
    }
    
    // Input validation tests
    
    @Test(expected = IOException.class)
    public void testDecompressWithNullInput() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        decompressor.decompress(null, output, 0, 0);
    }
    
    @Test(expected = IOException.class)
    public void testDecompressWithNullOutput() throws IOException {
        ByteArrayInputStream input = new ByteArrayInputStream(new byte[0]);
        decompressor.decompress(input, null, 0, 0);
    }
    
    // Data integrity tests
    
    @Test(expected = CorruptedArchiveException.class)
    public void testDecompressWithInsufficientData() throws IOException {
        byte[] testData = "Hello".getBytes(); // 5 bytes
        ByteArrayInputStream input = new ByteArrayInputStream(testData);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        
        // Claim there are 10 bytes but only provide 5
        decompressor.decompress(input, output, 10, 10);
    }
    
    @Test
    public void testDecompressWithExactData() throws IOException {
        byte[] testData = "Exact data".getBytes();
        ByteArrayInputStream input = new ByteArrayInputStream(testData);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        
        decompressor.decompress(input, output, testData.length, testData.length);
        
        assertArrayEquals("Should handle exact data size correctly", testData, output.toByteArray());
    }
    
    @Test
    public void testDecompressWithExtraData() throws IOException {
        byte[] testData = "Hello, World! Extra data here".getBytes();
        byte[] expectedData = "Hello, World!".getBytes();
        
        ByteArrayInputStream input = new ByteArrayInputStream(testData);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        
        // Only read the first 13 bytes
        decompressor.decompress(input, output, expectedData.length, expectedData.length);
        
        assertArrayEquals("Should only read specified amount of data", expectedData, output.toByteArray());
    }
    
    // Performance tests
    
    @Test
    public void testDecompressPerformance() throws IOException {
        // Test with moderately large data to check performance
        byte[] testData = new byte[1024 * 1024]; // 1MB
        for (int i = 0; i < testData.length; i++) {
            testData[i] = (byte) (i % 256);
        }
        
        ByteArrayInputStream input = new ByteArrayInputStream(testData);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        
        long startTime = System.currentTimeMillis();
        decompressor.decompress(input, output, testData.length, testData.length);
        long endTime = System.currentTimeMillis();
        
        assertArrayEquals("Large data should be processed correctly", testData, output.toByteArray());
        
        // Performance check - should complete within reasonable time (10 seconds)
        long duration = endTime - startTime;
        assertTrue("Decompression should complete within 10 seconds, took " + duration + "ms", 
                  duration < 10000);
    }
    
    // Edge case tests
    
    @Test
    public void testDecompressWithSingleByte() throws IOException {
        byte[] testData = {0x42};
        ByteArrayInputStream input = new ByteArrayInputStream(testData);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        
        decompressor.decompress(input, output, 1, 1);
        
        assertArrayEquals("Should handle single byte correctly", testData, output.toByteArray());
    }
    
    @Test
    public void testDecompressWithAllZeros() throws IOException {
        byte[] testData = new byte[100]; // All zeros
        ByteArrayInputStream input = new ByteArrayInputStream(testData);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        
        decompressor.decompress(input, output, testData.length, testData.length);
        
        assertArrayEquals("Should handle all-zero data correctly", testData, output.toByteArray());
    }
    
    @Test
    public void testDecompressWithAllOnes() throws IOException {
        byte[] testData = new byte[100];
        for (int i = 0; i < testData.length; i++) {
            testData[i] = (byte) 0xFF;
        }
        
        ByteArrayInputStream input = new ByteArrayInputStream(testData);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        
        decompressor.decompress(input, output, testData.length, testData.length);
        
        assertArrayEquals("Should handle all-ones data correctly", testData, output.toByteArray());
    }
    
    @Test
    public void testDecompressWithRepeatingPattern() throws IOException {
        byte[] pattern = {0x01, 0x02, 0x03, 0x04};
        byte[] testData = new byte[1000];
        
        for (int i = 0; i < testData.length; i++) {
            testData[i] = pattern[i % pattern.length];
        }
        
        ByteArrayInputStream input = new ByteArrayInputStream(testData);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        
        decompressor.decompress(input, output, testData.length, testData.length);
        
        assertArrayEquals("Should handle repeating pattern correctly", testData, output.toByteArray());
    }
    
    // Unicode and text data tests
    
    @Test
    public void testDecompressWithUnicodeText() throws IOException {
        String unicodeText = "Hello 世界 🌍 Мир";
        byte[] testData = unicodeText.getBytes("UTF-8");
        
        ByteArrayInputStream input = new ByteArrayInputStream(testData);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        
        decompressor.decompress(input, output, testData.length, testData.length);
        
        assertArrayEquals("Should handle Unicode text correctly", testData, output.toByteArray());
        
        String result = new String(output.toByteArray(), "UTF-8");
        assertEquals("Unicode text should be preserved", unicodeText, result);
    }
    
    @Test
    public void testDecompressWithJapaneseText() throws IOException {
        String japaneseText = "これはテストです。日本語のファイル名を含むLZHアーカイブ。";
        byte[] testData = japaneseText.getBytes("UTF-8");
        
        ByteArrayInputStream input = new ByteArrayInputStream(testData);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        
        decompressor.decompress(input, output, testData.length, testData.length);
        
        assertArrayEquals("Should handle Japanese text correctly", testData, output.toByteArray());
        
        String result = new String(output.toByteArray(), "UTF-8");
        assertEquals("Japanese text should be preserved", japaneseText, result);
    }
    
    // Resource management tests
    
    @Test
    public void testMultipleDecompressions() throws IOException {
        // Test that the decompressor can be used multiple times
        for (int i = 0; i < 10; i++) {
            byte[] testData = ("Test data iteration " + i).getBytes();
            ByteArrayInputStream input = new ByteArrayInputStream(testData);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            
            decompressor.decompress(input, output, testData.length, testData.length);
            
            assertArrayEquals("Iteration " + i + " should work correctly", 
                             testData, output.toByteArray());
        }
    }
}