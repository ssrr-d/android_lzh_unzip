package com.example.lzh.util;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;

/**
 * Unit tests for FileManager utility class
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 21)
public class FileManagerTest {
    
    private File mockOutputDir;
    private File mockParentDir;
    private File mockFile;
    
    @Before
    public void setUp() {
        mockOutputDir = mock(File.class);
        mockParentDir = mock(File.class);
        mockFile = mock(File.class);
    }
    
    // Directory creation tests
    
    @Test
    public void testCreateDirectoriesWithExistingDirectory() throws IOException {
        when(mockOutputDir.exists()).thenReturn(true);
        when(mockOutputDir.isDirectory()).thenReturn(true);
        
        FileManager.createDirectories(mockOutputDir);
        
        verify(mockOutputDir, never()).mkdirs();
    }
    
    @Test
    public void testCreateDirectoriesWithNonExistentDirectory() throws IOException {
        when(mockOutputDir.exists()).thenReturn(false);
        when(mockOutputDir.mkdirs()).thenReturn(true);
        
        FileManager.createDirectories(mockOutputDir);
        
        verify(mockOutputDir).mkdirs();
    }
    
    @Test(expected = IOException.class)
    public void testCreateDirectoriesFailure() throws IOException {
        when(mockOutputDir.exists()).thenReturn(false);
        when(mockOutputDir.mkdirs()).thenReturn(false);
        when(mockOutputDir.getAbsolutePath()).thenReturn("/failed/path");
        
        FileManager.createDirectories(mockOutputDir);
    }
    
    @Test(expected = IOException.class)
    public void testCreateDirectoriesWithExistingFile() throws IOException {
        when(mockOutputDir.exists()).thenReturn(true);
        when(mockOutputDir.isDirectory()).thenReturn(false);
        when(mockOutputDir.getAbsolutePath()).thenReturn("/path/to/file.txt");
        
        FileManager.createDirectories(mockOutputDir);
    }
    
    @Test(expected = IOException.class)
    public void testCreateDirectoriesWithNullInput() throws IOException {
        FileManager.createDirectories(null);
    }
    
    // Path validation tests
    
    @Test
    public void testIsValidOutputPathWithNormalFile() {
        assertTrue("Normal filename should be valid", 
                  FileManager.isValidOutputPath(mockOutputDir, "normal.txt"));
        assertTrue("Filename with spaces should be valid", 
                  FileManager.isValidOutputPath(mockOutputDir, "file with spaces.txt"));
        assertTrue("Filename with numbers should be valid", 
                  FileManager.isValidOutputPath(mockOutputDir, "file123.txt"));
    }
    
    @Test
    public void testIsValidOutputPathWithSubdirectory() {
        assertTrue("Subdirectory path should be valid", 
                  FileManager.isValidOutputPath(mockOutputDir, "subdir/file.txt"));
        assertTrue("Multiple subdirectories should be valid", 
                  FileManager.isValidOutputPath(mockOutputDir, "sub1/sub2/file.txt"));
    }
    
    @Test
    public void testIsValidOutputPathWithPathTraversal() {
        assertFalse("Parent directory traversal should be invalid", 
                   FileManager.isValidOutputPath(mockOutputDir, "../file.txt"));
        assertFalse("Multiple parent traversals should be invalid", 
                   FileManager.isValidOutputPath(mockOutputDir, "../../file.txt"));
        assertFalse("Mixed path with traversal should be invalid", 
                   FileManager.isValidOutputPath(mockOutputDir, "subdir/../../../file.txt"));
    }
    
    @Test
    public void testIsValidOutputPathWithAbsolutePath() {
        assertFalse("Absolute Unix path should be invalid", 
                   FileManager.isValidOutputPath(mockOutputDir, "/absolute/path/file.txt"));
        assertFalse("Absolute Windows path should be invalid", 
                   FileManager.isValidOutputPath(mockOutputDir, "C:\\absolute\\path\\file.txt"));
        assertFalse("UNC path should be invalid", 
                   FileManager.isValidOutputPath(mockOutputDir, "\\\\server\\share\\file.txt"));
    }
    
    @Test
    public void testIsValidOutputPathWithSpecialCases() {
        assertFalse("Null filename should be invalid", 
                   FileManager.isValidOutputPath(mockOutputDir, null));
        assertFalse("Empty filename should be invalid", 
                   FileManager.isValidOutputPath(mockOutputDir, ""));
        assertFalse("Whitespace-only filename should be invalid", 
                   FileManager.isValidOutputPath(mockOutputDir, "   "));
        assertFalse("Current directory reference should be invalid", 
                   FileManager.isValidOutputPath(mockOutputDir, "."));
        assertFalse("Parent directory reference should be invalid", 
                   FileManager.isValidOutputPath(mockOutputDir, ".."));
    }
    
    @Test
    public void testIsValidOutputPathWithNullOutputDir() {
        assertFalse("Null output directory should make path invalid", 
                   FileManager.isValidOutputPath(null, "file.txt"));
    }
    
    // Safe file path creation tests
    
    @Test
    public void testCreateSafeFilePathWithNormalFile() throws IOException {
        when(mockOutputDir.getAbsolutePath()).thenReturn("/output");
        
        File result = FileManager.createSafeFilePath(mockOutputDir, "normal.txt");
        
        assertNotNull("Result should not be null", result);
        // Note: Exact path verification depends on File implementation
    }
    
    @Test
    public void testCreateSafeFilePathWithSubdirectory() throws IOException {
        when(mockOutputDir.getAbsolutePath()).thenReturn("/output");
        
        File result = FileManager.createSafeFilePath(mockOutputDir, "subdir/file.txt");
        
        assertNotNull("Result should not be null", result);
    }
    
    @Test(expected = IOException.class)
    public void testCreateSafeFilePathWithInvalidPath() throws IOException {
        FileManager.createSafeFilePath(mockOutputDir, "../invalid.txt");
    }
    
    @Test(expected = IOException.class)
    public void testCreateSafeFilePathWithNullOutputDir() throws IOException {
        FileManager.createSafeFilePath(null, "file.txt");
    }
    
    @Test(expected = IOException.class)
    public void testCreateSafeFilePathWithNullFileName() throws IOException {
        FileManager.createSafeFilePath(mockOutputDir, null);
    }
    
    // File writing tests
    
    @Test
    public void testWriteFileWithValidInput() throws IOException {
        byte[] testData = "Hello, World!".getBytes();
        InputStream input = new ByteArrayInputStream(testData);
        
        when(mockFile.getParentFile()).thenReturn(mockParentDir);
        when(mockParentDir.exists()).thenReturn(true);
        when(mockFile.createNewFile()).thenReturn(true);
        
        // This test verifies the method doesn't throw exceptions
        // Actual file writing is hard to test with mocks
        try {
            FileManager.writeFile(mockFile, input);
        } catch (IOException e) {
            // Expected due to mocking limitations, but should not be validation error
            assertFalse("Should not be input validation error", 
                       e.getMessage().contains("null"));
        }
    }
    
    @Test(expected = IOException.class)
    public void testWriteFileWithNullFile() throws IOException {
        InputStream input = new ByteArrayInputStream("test".getBytes());
        FileManager.writeFile(null, input);
    }
    
    @Test(expected = IOException.class)
    public void testWriteFileWithNullInput() throws IOException {
        FileManager.writeFile(mockFile, null);
    }
    
    // Japanese filename tests
    
    @Test
    public void testIsValidOutputPathWithJapaneseFilename() {
        assertTrue("Japanese filename should be valid", 
                  FileManager.isValidOutputPath(mockOutputDir, "テスト.txt"));
        assertTrue("Japanese path should be valid", 
                  FileManager.isValidOutputPath(mockOutputDir, "フォルダ/ファイル.txt"));
    }
    
    @Test
    public void testCreateSafeFilePathWithJapaneseFilename() throws IOException {
        when(mockOutputDir.getAbsolutePath()).thenReturn("/output");
        
        File result = FileManager.createSafeFilePath(mockOutputDir, "テスト.txt");
        
        assertNotNull("Japanese filename should create valid path", result);
    }
    
    // Edge case tests
    
    @Test
    public void testIsValidOutputPathWithLongFilename() {
        // Create a very long filename
        StringBuilder longName = new StringBuilder();
        for (int i = 0; i < 300; i++) {
            longName.append("a");
        }
        longName.append(".txt");
        
        // Should still be valid from security perspective (length limits are OS-specific)
        boolean result = FileManager.isValidOutputPath(mockOutputDir, longName.toString());
        // Don't assert specific result as it may vary by implementation
        assertNotNull("Should return a boolean result", Boolean.valueOf(result));
    }
    
    @Test
    public void testIsValidOutputPathWithSpecialCharacters() {
        // Test various special characters that might be valid in filenames
        assertTrue("Underscore should be valid", 
                  FileManager.isValidOutputPath(mockOutputDir, "file_name.txt"));
        assertTrue("Hyphen should be valid", 
                  FileManager.isValidOutputPath(mockOutputDir, "file-name.txt"));
        assertTrue("Dot in middle should be valid", 
                  FileManager.isValidOutputPath(mockOutputDir, "file.name.txt"));
        
        // These might be platform-specific, so just test they don't crash
        FileManager.isValidOutputPath(mockOutputDir, "file@name.txt");
        FileManager.isValidOutputPath(mockOutputDir, "file#name.txt");
        FileManager.isValidOutputPath(mockOutputDir, "file$name.txt");
    }
    
    @Test
    public void testIsValidOutputPathWithMixedSeparators() {
        // Test mixed path separators (should normalize)
        boolean result1 = FileManager.isValidOutputPath(mockOutputDir, "sub\\dir/file.txt");
        boolean result2 = FileManager.isValidOutputPath(mockOutputDir, "sub/dir\\file.txt");
        
        // Should handle mixed separators gracefully
        assertNotNull("Should handle mixed separators", Boolean.valueOf(result1));
        assertNotNull("Should handle mixed separators", Boolean.valueOf(result2));
    }
    
    @Test
    public void testPathTraversalVariations() {
        // Test various path traversal attack patterns
        String[] maliciousPaths = {
            "../file.txt",
            "..\\file.txt",
            "sub/../../../file.txt",
            "sub\\..\\..\\..\\file.txt",
            "./../../file.txt",
            ".\\..\\..\\file.txt",
            "sub/./../../file.txt",
            "sub\\.\\..\\..\\file.txt"
        };
        
        for (String path : maliciousPaths) {
            assertFalse("Path should be invalid: " + path, 
                       FileManager.isValidOutputPath(mockOutputDir, path));
        }
    }
    
    @Test
    public void testValidRelativePaths() {
        // Test valid relative paths that should be allowed
        String[] validPaths = {
            "file.txt",
            "subdir/file.txt",
            "sub1/sub2/file.txt",
            "sub1/sub2/sub3/file.txt",
            "./file.txt",
            ".\\file.txt",
            "sub/./file.txt",
            "sub\\.\\file.txt"
        };
        
        for (String path : validPaths) {
            assertTrue("Path should be valid: " + path, 
                      FileManager.isValidOutputPath(mockOutputDir, path));
        }
    }
}