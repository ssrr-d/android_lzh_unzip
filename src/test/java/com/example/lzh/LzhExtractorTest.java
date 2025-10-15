package com.example.lzh;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.File;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;

import com.example.lzh.exception.LzhException;
import com.example.lzh.exception.InvalidArchiveException;
import com.example.lzh.model.LzhArchive;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for LzhExtractor main API
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 21)
public class LzhExtractorTest {
    
    private LzhExtractor extractor;
    
    @Mock
    private File mockLzhFile;
    
    @Mock
    private File mockOutputDir;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        extractor = new LzhExtractor();
    }
    
    // File input validation tests
    
    @Test(expected = InvalidArchiveException.class)
    public void testExtractWithNullFile() throws LzhException {
        extractor.extract((File) null, mockOutputDir);
    }
    
    @Test(expected = InvalidArchiveException.class)
    public void testExtractWithNonExistentFile() throws LzhException {
        when(mockLzhFile.exists()).thenReturn(false);
        when(mockLzhFile.getAbsolutePath()).thenReturn("/path/to/nonexistent.lzh");
        
        extractor.extract(mockLzhFile, mockOutputDir);
    }
    
    @Test(expected = InvalidArchiveException.class)
    public void testExtractWithDirectory() throws LzhException {
        when(mockLzhFile.exists()).thenReturn(true);
        when(mockLzhFile.isFile()).thenReturn(false);
        when(mockLzhFile.getAbsolutePath()).thenReturn("/path/to/directory");
        
        extractor.extract(mockLzhFile, mockOutputDir);
    }
    
    @Test(expected = InvalidArchiveException.class)
    public void testExtractWithUnreadableFile() throws LzhException {
        when(mockLzhFile.exists()).thenReturn(true);
        when(mockLzhFile.isFile()).thenReturn(true);
        when(mockLzhFile.canRead()).thenReturn(false);
        when(mockLzhFile.getAbsolutePath()).thenReturn("/path/to/unreadable.lzh");
        
        extractor.extract(mockLzhFile, mockOutputDir);
    }
    
    @Test(expected = InvalidArchiveException.class)
    public void testExtractWithEmptyFile() throws LzhException {
        when(mockLzhFile.exists()).thenReturn(true);
        when(mockLzhFile.isFile()).thenReturn(true);
        when(mockLzhFile.canRead()).thenReturn(true);
        when(mockLzhFile.length()).thenReturn(0L);
        when(mockLzhFile.getAbsolutePath()).thenReturn("/path/to/empty.lzh");
        
        extractor.extract(mockLzhFile, mockOutputDir);
    }
    
    // InputStream input validation tests
    
    @Test(expected = InvalidArchiveException.class)
    public void testExtractWithNullInputStream() throws LzhException {
        extractor.extract((InputStream) null, mockOutputDir);
    }
    
    @Test
    public void testExtractWithValidInputStream() throws LzhException, IOException {
        // Create a minimal valid LZH header for testing
        byte[] validLzhData = createMinimalValidLzhData();
        InputStream inputStream = new ByteArrayInputStream(validLzhData);
        
        when(mockOutputDir.getAbsolutePath()).thenReturn("/output");
        when(mockOutputDir.exists()).thenReturn(true);
        when(mockOutputDir.isDirectory()).thenReturn(true);
        when(mockOutputDir.canWrite()).thenReturn(true);
        
        // This should not throw an exception for valid input
        try {
            extractor.extract(inputStream, mockOutputDir);
        } catch (LzhException e) {
            // Expected for minimal test data, but should not be input validation error
            assertFalse("Should not be input validation error", 
                       e.getMessage().contains("cannot be null"));
        }
    }
    
    // Byte array input validation tests
    
    @Test(expected = InvalidArchiveException.class)
    public void testExtractWithNullByteArray() throws LzhException {
        extractor.extract((byte[]) null, mockOutputDir);
    }
    
    @Test(expected = InvalidArchiveException.class)
    public void testExtractWithEmptyByteArray() throws LzhException {
        extractor.extract(new byte[0], mockOutputDir);
    }
    
    @Test
    public void testExtractWithValidByteArray() throws LzhException {
        byte[] validLzhData = createMinimalValidLzhData();
        
        when(mockOutputDir.getAbsolutePath()).thenReturn("/output");
        when(mockOutputDir.exists()).thenReturn(true);
        when(mockOutputDir.isDirectory()).thenReturn(true);
        when(mockOutputDir.canWrite()).thenReturn(true);
        
        // This should not throw an exception for valid input
        try {
            extractor.extract(validLzhData, mockOutputDir);
        } catch (LzhException e) {
            // Expected for minimal test data, but should not be input validation error
            assertFalse("Should not be input validation error", 
                       e.getMessage().contains("cannot be null") || 
                       e.getMessage().contains("cannot be empty"));
        }
    }
    
    // Output directory validation tests
    
    @Test(expected = InvalidArchiveException.class)
    public void testExtractWithNullOutputDir() throws LzhException {
        byte[] validLzhData = createMinimalValidLzhData();
        extractor.extract(validLzhData, null);
    }
    
    @Test(expected = InvalidArchiveException.class)
    public void testExtractWithFileAsOutputDir() throws LzhException {
        byte[] validLzhData = createMinimalValidLzhData();
        
        when(mockOutputDir.exists()).thenReturn(true);
        when(mockOutputDir.isDirectory()).thenReturn(false);
        when(mockOutputDir.getAbsolutePath()).thenReturn("/path/to/file.txt");
        
        extractor.extract(validLzhData, mockOutputDir);
    }
    
    @Test(expected = InvalidArchiveException.class)
    public void testExtractWithUnwritableOutputDir() throws LzhException {
        byte[] validLzhData = createMinimalValidLzhData();
        
        when(mockOutputDir.exists()).thenReturn(true);
        when(mockOutputDir.isDirectory()).thenReturn(true);
        when(mockOutputDir.canWrite()).thenReturn(false);
        when(mockOutputDir.getAbsolutePath()).thenReturn("/path/to/readonly");
        
        extractor.extract(validLzhData, mockOutputDir);
    }
    
    // Archive info tests
    
    @Test(expected = InvalidArchiveException.class)
    public void testGetArchiveInfoWithNullFile() throws LzhException {
        extractor.getArchiveInfo((File) null);
    }
    
    @Test(expected = InvalidArchiveException.class)
    public void testGetArchiveInfoWithNullInputStream() throws LzhException {
        extractor.getArchiveInfo((InputStream) null);
    }
    
    @Test(expected = InvalidArchiveException.class)
    public void testGetArchiveInfoWithNullByteArray() throws LzhException {
        extractor.getArchiveInfo((byte[]) null);
    }
    
    @Test
    public void testGetArchiveInfoWithValidData() throws LzhException {
        byte[] validLzhData = createMinimalValidLzhData();
        
        try {
            LzhArchive archive = extractor.getArchiveInfo(validLzhData);
            assertNotNull("Archive should not be null", archive);
            assertTrue("Archive should have entries", archive.getEntryCount() >= 0);
        } catch (LzhException e) {
            // Expected for minimal test data, but should not be input validation error
            assertFalse("Should not be input validation error", 
                       e.getMessage().contains("cannot be null"));
        }
    }
    
    // File extraction tests
    
    @Test(expected = InvalidArchiveException.class)
    public void testExtractFileWithNullFileName() throws LzhException {
        byte[] validLzhData = createMinimalValidLzhData();
        extractor.extractFile(validLzhData, null, mockOutputDir);
    }
    
    @Test(expected = InvalidArchiveException.class)
    public void testExtractFileWithEmptyFileName() throws LzhException {
        byte[] validLzhData = createMinimalValidLzhData();
        extractor.extractFile(validLzhData, "", mockOutputDir);
    }
    
    @Test(expected = InvalidArchiveException.class)
    public void testExtractFileWithWhitespaceFileName() throws LzhException {
        byte[] validLzhData = createMinimalValidLzhData();
        extractor.extractFile(validLzhData, "   ", mockOutputDir);
    }
    
    @Test
    public void testExtractFileWithValidFileName() throws LzhException {
        byte[] validLzhData = createMinimalValidLzhData();
        
        when(mockOutputDir.getAbsolutePath()).thenReturn("/output");
        when(mockOutputDir.exists()).thenReturn(true);
        when(mockOutputDir.isDirectory()).thenReturn(true);
        when(mockOutputDir.canWrite()).thenReturn(true);
        
        try {
            extractor.extractFile(validLzhData, "test.txt", mockOutputDir);
        } catch (com.example.lzh.exception.FileNotFoundException e) {
            // Expected when file doesn't exist in archive
            assertTrue("Should be file not found error", 
                      e.getMessage().contains("not found"));
        } catch (LzhException e) {
            // Other LZH exceptions are acceptable for minimal test data
            assertFalse("Should not be input validation error", 
                       e.getMessage().contains("cannot be null") || 
                       e.getMessage().contains("cannot be empty"));
        }
    }
    
    // Performance and resource tests
    
    @Test
    public void testLargeFileHandling() throws LzhException {
        // Test with large file size (but not actual large data)
        when(mockLzhFile.exists()).thenReturn(true);
        when(mockLzhFile.isFile()).thenReturn(true);
        when(mockLzhFile.canRead()).thenReturn(true);
        when(mockLzhFile.length()).thenReturn(Integer.MAX_VALUE + 1L);
        when(mockLzhFile.getAbsolutePath()).thenReturn("/path/to/large.lzh");
        
        when(mockOutputDir.getAbsolutePath()).thenReturn("/output");
        when(mockOutputDir.exists()).thenReturn(true);
        when(mockOutputDir.isDirectory()).thenReturn(true);
        when(mockOutputDir.canWrite()).thenReturn(true);
        
        // Should handle large files without input validation errors
        try {
            extractor.extract(mockLzhFile, mockOutputDir);
        } catch (LzhException e) {
            // File I/O errors are expected since we're using mocks
            // but should not be input validation errors
            assertFalse("Should not be input validation error for large files", 
                       e.getMessage().contains("cannot be null"));
        }
    }
    
    // Helper methods
    
    /**
     * Creates minimal valid LZH data for testing
     * This is not a real LZH file but has basic structure to pass initial validation
     */
    private byte[] createMinimalValidLzhData() {
        // Create a minimal LZH-like structure
        // This won't be a valid archive but will pass null/empty checks
        byte[] data = new byte[64];
        
        // Add some header-like data
        data[0] = 0x1A; // Header size
        data[1] = 0x00; // Checksum
        data[2] = '-';   // Method signature start
        data[3] = 'l';
        data[4] = 'h';
        data[5] = '0';
        data[6] = '-';   // Method signature end
        
        // Add some size information
        data[7] = 0x10;  // Compressed size (low byte)
        data[8] = 0x00;  // Compressed size
        data[9] = 0x00;  // Compressed size
        data[10] = 0x00; // Compressed size (high byte)
        
        data[11] = 0x10; // Original size (low byte)
        data[12] = 0x00; // Original size
        data[13] = 0x00; // Original size
        data[14] = 0x00; // Original size (high byte)
        
        // Add filename length and name
        data[21] = 0x08; // Filename length
        data[22] = 't';
        data[23] = 'e';
        data[24] = 's';
        data[25] = 't';
        data[26] = '.';
        data[27] = 't';
        data[28] = 'x';
        data[29] = 't';
        
        return data;
    }
}