package com.example.lzh.model;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Date;

/**
 * Unit tests for LzhEntry model class
 */
public class LzhEntryTest {
    
    private LzhEntry entry;
    private Date testDate;
    
    @Before
    public void setUp() {
        entry = new LzhEntry();
        testDate = new Date();
    }
    
    // Constructor tests
    
    @Test
    public void testDefaultConstructor() {
        LzhEntry defaultEntry = new LzhEntry();
        assertNull("Default filename should be null", defaultEntry.getFileName());
        assertEquals("Default original size should be 0", 0, defaultEntry.getOriginalSize());
        assertEquals("Default compressed size should be 0", 0, defaultEntry.getCompressedSize());
        assertNull("Default compression method should be null", defaultEntry.getCompressionMethod());
        assertEquals("Default CRC16 should be 0", 0, defaultEntry.getCrc16());
        assertNull("Default last modified should be null", defaultEntry.getLastModified());
    }
    
    @Test
    public void testParameterizedConstructor() {
        String fileName = "test.txt";
        long originalSize = 1024;
        long compressedSize = 512;
        String compressionMethod = "-lh5-";
        int crc16 = 0x1234;
        Date lastModified = new Date();
        
        LzhEntry paramEntry = new LzhEntry(fileName, originalSize, compressedSize, 
                                          compressionMethod, crc16, lastModified);
        
        assertEquals("Filename should match", fileName, paramEntry.getFileName());
        assertEquals("Original size should match", originalSize, paramEntry.getOriginalSize());
        assertEquals("Compressed size should match", compressedSize, paramEntry.getCompressedSize());
        assertEquals("Compression method should match", compressionMethod, paramEntry.getCompressionMethod());
        assertEquals("CRC16 should match", crc16, paramEntry.getCrc16());
        assertEquals("Last modified should match", lastModified, paramEntry.getLastModified());
    }
    
    @Test
    public void testParameterizedConstructorWithNullDate() {
        LzhEntry paramEntry = new LzhEntry("test.txt", 1024, 512, "-lh5-", 0x1234, null);
        assertNull("Last modified should be null when passed null", paramEntry.getLastModified());
    }
    
    // Getter and setter tests
    
    @Test
    public void testFileNameGetterSetter() {
        String fileName = "example.txt";
        entry.setFileName(fileName);
        assertEquals("Filename should match set value", fileName, entry.getFileName());
        
        entry.setFileName(null);
        assertNull("Filename should be null when set to null", entry.getFileName());
    }
    
    @Test
    public void testOriginalSizeGetterSetter() {
        long originalSize = 2048;
        entry.setOriginalSize(originalSize);
        assertEquals("Original size should match set value", originalSize, entry.getOriginalSize());
        
        entry.setOriginalSize(0);
        assertEquals("Original size should be 0 when set to 0", 0, entry.getOriginalSize());
        
        entry.setOriginalSize(-1);
        assertEquals("Original size should handle negative values", -1, entry.getOriginalSize());
    }
    
    @Test
    public void testCompressedSizeGetterSetter() {
        long compressedSize = 1024;
        entry.setCompressedSize(compressedSize);
        assertEquals("Compressed size should match set value", compressedSize, entry.getCompressedSize());
        
        entry.setCompressedSize(0);
        assertEquals("Compressed size should be 0 when set to 0", 0, entry.getCompressedSize());
    }
    
    @Test
    public void testCompressionMethodGetterSetter() {
        String method = "-lh1-";
        entry.setCompressionMethod(method);
        assertEquals("Compression method should match set value", method, entry.getCompressionMethod());
        
        entry.setCompressionMethod(null);
        assertNull("Compression method should be null when set to null", entry.getCompressionMethod());
    }
    
    @Test
    public void testCrc16GetterSetter() {
        int crc16 = 0xABCD;
        entry.setCrc16(crc16);
        assertEquals("CRC16 should match set value", crc16, entry.getCrc16());
        
        entry.setCrc16(0);
        assertEquals("CRC16 should be 0 when set to 0", 0, entry.getCrc16());
        
        entry.setCrc16(-1);
        assertEquals("CRC16 should handle negative values", -1, entry.getCrc16());
    }
    
    @Test
    public void testLastModifiedGetterSetter() {
        Date date = new Date();
        entry.setLastModified(date);
        assertEquals("Last modified should match set value", date, entry.getLastModified());
        
        // Test that the date is copied (defensive copying)
        Date originalDate = new Date();
        entry.setLastModified(originalDate);
        Date retrievedDate = entry.getLastModified();
        assertNotSame("Retrieved date should be a copy", originalDate, retrievedDate);
        assertEquals("Retrieved date should equal original", originalDate, retrievedDate);
        
        entry.setLastModified(null);
        assertNull("Last modified should be null when set to null", entry.getLastModified());
    }
    
    // Compression ratio tests
    
    @Test
    public void testCompressionRatioNormalCase() {
        entry.setOriginalSize(1000);
        entry.setCompressedSize(500);
        assertEquals("Compression ratio should be 0.5", 0.5, entry.getCompressionRatio(), 0.001);
    }
    
    @Test
    public void testCompressionRatioZeroOriginalSize() {
        entry.setOriginalSize(0);
        entry.setCompressedSize(100);
        assertEquals("Compression ratio should be 0.0 for zero original size", 0.0, entry.getCompressionRatio(), 0.001);
    }
    
    @Test
    public void testCompressionRatioNoCompression() {
        entry.setOriginalSize(1000);
        entry.setCompressedSize(1000);
        assertEquals("Compression ratio should be 1.0 for no compression", 1.0, entry.getCompressionRatio(), 0.001);
    }
    
    @Test
    public void testCompressionRatioExpansion() {
        entry.setOriginalSize(1000);
        entry.setCompressedSize(1200);
        assertEquals("Compression ratio should be 1.2 for expansion", 1.2, entry.getCompressionRatio(), 0.001);
    }
    
    // Detailed info tests
    
    @Test
    public void testGetDetailedInfoComplete() {
        entry.setFileName("test.txt");
        entry.setCompressionMethod("-lh5-");
        entry.setOriginalSize(2048);
        entry.setCompressedSize(1024);
        entry.setCrc16(0x1234);
        entry.setLastModified(testDate);
        
        String info = entry.getDetailedInfo();
        
        assertTrue("Should contain filename", info.contains("test.txt"));
        assertTrue("Should contain compression method", info.contains("-lh5-"));
        assertTrue("Should contain original size", info.contains("2.0 KB"));
        assertTrue("Should contain compressed size", info.contains("1.0 KB"));
        assertTrue("Should contain compression ratio", info.contains("50.0%"));
        assertTrue("Should contain CRC16", info.contains("0x1234"));
        assertTrue("Should contain last modified", info.contains(testDate.toString()));
    }
    
    @Test
    public void testGetDetailedInfoWithNullDate() {
        entry.setFileName("test.txt");
        entry.setCompressionMethod("-lh0-");
        entry.setOriginalSize(1024);
        entry.setCompressedSize(1024);
        entry.setCrc16(0x5678);
        entry.setLastModified(null);
        
        String info = entry.getDetailedInfo();
        
        assertTrue("Should contain filename", info.contains("test.txt"));
        assertTrue("Should contain compression method", info.contains("-lh0-"));
        assertFalse("Should not contain 'Last modified' when date is null", 
                   info.contains("Last modified:"));
    }
    
    // Byte formatting tests
    
    @Test
    public void testByteFormatting() {
        // Test bytes
        entry.setOriginalSize(512);
        entry.setCompressedSize(256);
        String info = entry.getDetailedInfo();
        assertTrue("Should format bytes correctly", info.contains("512 B"));
        assertTrue("Should format bytes correctly", info.contains("256 B"));
        
        // Test KB
        entry.setOriginalSize(2048);
        entry.setCompressedSize(1536);
        info = entry.getDetailedInfo();
        assertTrue("Should format KB correctly", info.contains("2.0 KB"));
        assertTrue("Should format KB correctly", info.contains("1.5 KB"));
        
        // Test MB
        entry.setOriginalSize(2 * 1024 * 1024);
        entry.setCompressedSize(1024 * 1024);
        info = entry.getDetailedInfo();
        assertTrue("Should format MB correctly", info.contains("2.0 MB"));
        assertTrue("Should format MB correctly", info.contains("1.0 MB"));
        
        // Test GB
        entry.setOriginalSize(3L * 1024 * 1024 * 1024);
        entry.setCompressedSize(2L * 1024 * 1024 * 1024);
        info = entry.getDetailedInfo();
        assertTrue("Should format GB correctly", info.contains("3.0 GB"));
        assertTrue("Should format GB correctly", info.contains("2.0 GB"));
    }
    
    // toString tests
    
    @Test
    public void testToString() {
        entry.setFileName("example.txt");
        entry.setOriginalSize(1024);
        entry.setCompressedSize(512);
        entry.setCompressionMethod("-lh1-");
        entry.setCrc16(0xABCD);
        entry.setLastModified(testDate);
        
        String toString = entry.toString();
        
        assertTrue("toString should contain filename", toString.contains("example.txt"));
        assertTrue("toString should contain original size", toString.contains("1024"));
        assertTrue("toString should contain compressed size", toString.contains("512"));
        assertTrue("toString should contain compression method", toString.contains("-lh1-"));
        assertTrue("toString should contain CRC16", toString.contains("43981")); // 0xABCD in decimal
        assertTrue("toString should contain last modified", toString.contains(testDate.toString()));
    }
    
    @Test
    public void testToStringWithNulls() {
        // Test toString with null values
        String toString = entry.toString();
        
        assertTrue("toString should handle null filename", toString.contains("fileName=null"));
        assertTrue("toString should handle null compression method", toString.contains("compressionMethod=null"));
        assertTrue("toString should handle null last modified", toString.contains("lastModified=null"));
    }
    
    // Edge case tests
    
    @Test
    public void testLargeFileSizes() {
        long largeSize = Long.MAX_VALUE;
        entry.setOriginalSize(largeSize);
        entry.setCompressedSize(largeSize / 2);
        
        assertEquals("Should handle large original size", largeSize, entry.getOriginalSize());
        assertEquals("Should handle large compressed size", largeSize / 2, entry.getCompressedSize());
        
        double ratio = entry.getCompressionRatio();
        assertEquals("Compression ratio should be calculated correctly for large numbers", 
                    0.5, ratio, 0.001);
    }
    
    @Test
    public void testSpecialCharactersInFilename() {
        String specialFilename = "テスト ファイル.txt";
        entry.setFileName(specialFilename);
        assertEquals("Should handle special characters in filename", 
                    specialFilename, entry.getFileName());
        
        String info = entry.getDetailedInfo();
        assertTrue("Detailed info should contain special characters", 
                  info.contains(specialFilename));
    }
    
    @Test
    public void testAllCompressionMethods() {
        String[] methods = {"-lh0-", "-lh1-", "-lh5-", "-lh6-", "-lh7-"};
        
        for (String method : methods) {
            entry.setCompressionMethod(method);
            assertEquals("Should handle compression method: " + method, 
                        method, entry.getCompressionMethod());
            
            String info = entry.getDetailedInfo();
            assertTrue("Detailed info should contain method: " + method, 
                      info.contains(method));
        }
    }
}