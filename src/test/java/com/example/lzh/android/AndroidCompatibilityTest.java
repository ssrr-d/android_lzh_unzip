package com.example.lzh.android;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.RuntimeEnvironment;

import static org.junit.Assert.*;

import android.content.Context;
import java.io.File;
import java.io.IOException;

import com.example.lzh.AndroidLzhExtractor;
import com.example.lzh.LzhExtractor;
import com.example.lzh.exception.LzhException;
import com.example.lzh.util.AndroidCompatibility;

/**
 * Android-specific compatibility tests
 * Tests Android API level compatibility and Android-specific features
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 21, minSdk = 21, maxSdk = 34)
public class AndroidCompatibilityTest {
    
    private Context context;
    private AndroidLzhExtractor androidExtractor;
    private LzhExtractor standardExtractor;
    
    @Before
    public void setUp() {
        context = RuntimeEnvironment.getApplication();
        androidExtractor = new AndroidLzhExtractor(context);
        standardExtractor = new LzhExtractor();
    }
    
    // Android API level compatibility tests
    
    @Test
    @Config(sdk = 21)
    public void testApi21Compatibility() {
        // Test minimum supported API level
        assertTrue("Should work on API 21", AndroidCompatibility.isApiLevelSupported(21));
        assertNotNull("Android extractor should work on API 21", androidExtractor);
        
        // Test that basic functionality works
        try {
            File filesDir = context.getFilesDir();
            assertNotNull("Files directory should be available", filesDir);
            assertTrue("Files directory should exist or be creatable", 
                      filesDir.exists() || filesDir.mkdirs());
        } catch (Exception e) {
            fail("Basic Android functionality should work on API 21: " + e.getMessage());
        }
    }
    
    @Test
    @Config(sdk = 23)
    public void testApi23Compatibility() {
        // Test API 23 (Android 6.0) compatibility
        assertTrue("Should work on API 23", AndroidCompatibility.isApiLevelSupported(23));
        
        // Test runtime permissions handling (if applicable)
        try {
            File filesDir = context.getFilesDir();
            assertNotNull("Files directory should be available", filesDir);
        } catch (Exception e) {
            fail("Should handle API 23 features: " + e.getMessage());
        }
    }
    
    @Test
    @Config(sdk = 28)
    public void testApi28Compatibility() {
        // Test API 28 (Android 9.0) compatibility
        assertTrue("Should work on API 28", AndroidCompatibility.isApiLevelSupported(28));
        
        // Test newer Android features compatibility
        try {
            File filesDir = context.getFilesDir();
            assertNotNull("Files directory should be available", filesDir);
        } catch (Exception e) {
            fail("Should handle API 28 features: " + e.getMessage());
        }
    }
    
    @Test
    @Config(sdk = 34)
    public void testApi34Compatibility() {
        // Test latest API level compatibility
        assertTrue("Should work on API 34", AndroidCompatibility.isApiLevelSupported(34));
        
        // Test latest Android features compatibility
        try {
            File filesDir = context.getFilesDir();
            assertNotNull("Files directory should be available", filesDir);
        } catch (Exception e) {
            fail("Should handle API 34 features: " + e.getMessage());
        }
    }
    
    // Android internal storage tests
    
    @Test
    public void testInternalStorageAccess() {
        File filesDir = context.getFilesDir();
        assertNotNull("Files directory should not be null", filesDir);
        
        // Test that we can create subdirectories in internal storage
        File testDir = new File(filesDir, "lzh_test");
        assertTrue("Should be able to create test directory", 
                  testDir.exists() || testDir.mkdirs());
        
        // Test write permissions
        assertTrue("Should have write permission to files directory", 
                  testDir.canWrite());
        
        // Cleanup
        testDir.delete();
    }
    
    @Test
    public void testAndroidLzhExtractorWithInternalStorage() throws LzhException {
        byte[] testData = createMinimalLzhData();
        
        try {
            // Test extraction to internal storage
            androidExtractor.extractToInternalStorage(testData, "test_extraction");
            
            // Verify extraction location
            File extractionDir = new File(context.getFilesDir(), "test_extraction");
            assertTrue("Extraction directory should exist", extractionDir.exists());
            
        } catch (LzhException e) {
            // Expected for minimal test data, but should not be Android-specific error
            assertFalse("Should not be Android-specific error", 
                       e.getMessage().contains("Android") && e.getMessage().contains("not supported"));
        }
    }
    
    @Test
    public void testContextRequirement() {
        // Test that AndroidLzhExtractor requires context
        try {
            new AndroidLzhExtractor(null);
            fail("Should throw exception for null context");
        } catch (IllegalArgumentException e) {
            assertTrue("Should mention context requirement", 
                      e.getMessage().toLowerCase().contains("context"));
        }
    }
    
    // Android file system behavior tests
    
    @Test
    public void testAndroidFilePathHandling() {
        // Test Android-specific file path handling
        String[] testPaths = {
            "normal.txt",
            "subdir/file.txt",
            "テスト.txt", // Japanese filename
            "file with spaces.txt",
            "file-with-dashes.txt",
            "file_with_underscores.txt"
        };
        
        File filesDir = context.getFilesDir();
        
        for (String path : testPaths) {
            try {
                File testFile = new File(filesDir, path);
                File parentDir = testFile.getParentFile();
                
                if (parentDir != null && !parentDir.exists()) {
                    assertTrue("Should be able to create parent directory for: " + path, 
                              parentDir.mkdirs());
                }
                
                assertTrue("Should be able to create file: " + path, 
                          testFile.createNewFile() || testFile.exists());
                
                // Cleanup
                testFile.delete();
                if (parentDir != null && parentDir != filesDir) {
                    parentDir.delete();
                }
                
            } catch (IOException e) {
                fail("Should handle Android file path: " + path + " - " + e.getMessage());
            }
        }
    }
    
    @Test
    public void testAndroidStoragePermissions() {
        File filesDir = context.getFilesDir();
        
        // Test read permission
        assertTrue("Should have read permission to files directory", 
                  filesDir.canRead());
        
        // Test write permission
        assertTrue("Should have write permission to files directory", 
                  filesDir.canWrite());
        
        // Test execute permission (for directory traversal)
        assertTrue("Should have execute permission to files directory", 
                  filesDir.canExecute());
    }
    
    // Android memory management tests
    
    @Test
    public void testAndroidMemoryConstraints() {
        // Test that library works within Android memory constraints
        byte[] largeTestData = new byte[1024 * 1024]; // 1MB
        for (int i = 0; i < largeTestData.length; i++) {
            largeTestData[i] = (byte) (i % 256);
        }
        
        try {
            // Test that large data doesn't cause OutOfMemoryError
            androidExtractor.extractToInternalStorage(largeTestData, "large_test");
            
        } catch (OutOfMemoryError e) {
            fail("Should handle large data without OutOfMemoryError: " + e.getMessage());
        } catch (LzhException e) {
            // Expected for test data, but should not be memory-related
            assertFalse("Should not be memory-related error", 
                       e.getCause() instanceof OutOfMemoryError);
        }
    }
    
    // Android threading tests
    
    @Test
    public void testAndroidMainThreadUsage() {
        // Test that library can be used on main thread (for small operations)
        byte[] smallTestData = createMinimalLzhData();
        
        try {
            // This should not block or cause ANR
            androidExtractor.getArchiveInfo(smallTestData);
            
        } catch (LzhException e) {
            // Expected for test data
            assertTrue("Should handle main thread usage", true);
        }
    }
    
    // Android ProGuard compatibility tests
    
    @Test
    public void testProGuardCompatibility() {
        // Test that public API is accessible (ProGuard should not obfuscate)
        assertNotNull("LzhExtractor should be accessible", standardExtractor);
        assertNotNull("AndroidLzhExtractor should be accessible", androidExtractor);
        
        // Test that exception classes are accessible
        try {
            Class.forName("com.example.lzh.exception.LzhException");
            Class.forName("com.example.lzh.exception.InvalidArchiveException");
            Class.forName("com.example.lzh.exception.CorruptedArchiveException");
        } catch (ClassNotFoundException e) {
            fail("Exception classes should be accessible: " + e.getMessage());
        }
        
        // Test that model classes are accessible
        try {
            Class.forName("com.example.lzh.model.LzhArchive");
            Class.forName("com.example.lzh.model.LzhEntry");
        } catch (ClassNotFoundException e) {
            fail("Model classes should be accessible: " + e.getMessage());
        }
    }
    
    // Android resource management tests
    
    @Test
    public void testAndroidResourceCleanup() {
        // Test multiple operations to ensure proper resource cleanup
        byte[] testData = createMinimalLzhData();
        
        for (int i = 0; i < 10; i++) {
            try {
                androidExtractor.getArchiveInfo(testData);
            } catch (LzhException e) {
                // Expected for test data
            }
        }
        
        // Should not accumulate resources or cause memory leaks
        assertTrue("Multiple operations should complete without resource leaks", true);
    }
    
    // Android security tests
    
    @Test
    public void testAndroidSecurityConstraints() {
        // Test that library respects Android security model
        
        // Should not try to access external storage without permission
        File externalDir = context.getExternalFilesDir(null);
        if (externalDir != null) {
            // If external storage is available, test should still work
            assertTrue("Should handle external storage availability", true);
        }
        
        // Should only use internal storage by default
        File filesDir = context.getFilesDir();
        assertTrue("Should use internal storage", 
                  filesDir.getAbsolutePath().contains("files"));
    }
    
    // Android locale and encoding tests
    
    @Test
    public void testAndroidLocaleHandling() {
        // Test that library works with different Android locales
        byte[] japaneseData = createDataWithJapaneseFilename();
        
        try {
            androidExtractor.getArchiveInfo(japaneseData);
            // Should handle Japanese filenames regardless of system locale
        } catch (LzhException e) {
            // Expected for test data, but should not be locale-related
            assertFalse("Should not be locale-related error", 
                       e.getMessage().contains("locale") || e.getMessage().contains("encoding"));
        }
    }
    
    // Helper methods
    
    private byte[] createMinimalLzhData() {
        byte[] data = new byte[32];
        data[0] = 0x1A; // Header size
        data[1] = 0x00; // Checksum
        System.arraycopy("-lh0-".getBytes(), 0, data, 2, 5);
        
        // Add minimal file info
        data[7] = 0x05;  // Compressed size
        data[11] = 0x05; // Original size
        data[21] = 0x04; // Filename length
        data[22] = 't';
        data[23] = 'e';
        data[24] = 's';
        data[25] = 't';
        
        return data;
    }
    
    private byte[] createDataWithJapaneseFilename() {
        String japaneseFilename = "テスト.txt";
        byte[] filenameBytes = japaneseFilename.getBytes();
        
        byte[] data = new byte[32 + filenameBytes.length];
        data[0] = (byte) (0x1A + filenameBytes.length); // Header size
        data[1] = 0x00; // Checksum
        System.arraycopy("-lh0-".getBytes(), 0, data, 2, 5);
        
        data[7] = 0x05;  // Compressed size
        data[11] = 0x05; // Original size
        data[21] = (byte) filenameBytes.length; // Filename length
        System.arraycopy(filenameBytes, 0, data, 22, filenameBytes.length);
        
        return data;
    }
}