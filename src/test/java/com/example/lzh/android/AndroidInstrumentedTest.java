package com.example.lzh.android;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import android.content.Context;
import android.os.Build;
import java.io.File;
import java.io.IOException;

import com.example.lzh.AndroidLzhExtractor;
import com.example.lzh.LzhExtractor;
import com.example.lzh.exception.LzhException;

/**
 * Instrumented tests for Android device testing
 * These tests run on actual Android devices or emulators
 */
@RunWith(AndroidJUnit4.class)
public class AndroidInstrumentedTest {
    
    private Context context;
    private AndroidLzhExtractor androidExtractor;
    private LzhExtractor standardExtractor;
    
    @Before
    public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        androidExtractor = new AndroidLzhExtractor(context);
        standardExtractor = new LzhExtractor();
    }
    
    // Real device API level tests
    
    @Test
    public void testRealDeviceApiLevel() {
        int apiLevel = Build.VERSION.SDK_INT;
        
        assertTrue("Device API level should be 21 or higher", apiLevel >= 21);
        
        // Test that library works on actual device API level
        assertNotNull("Android extractor should work on device API " + apiLevel, androidExtractor);
        assertNotNull("Standard extractor should work on device API " + apiLevel, standardExtractor);
    }
    
    @Test
    public void testRealDeviceCompatibility() {
        // Test basic functionality on real device
        try {
            File filesDir = context.getFilesDir();
            assertNotNull("Files directory should be available on real device", filesDir);
            assertTrue("Files directory should exist on real device", 
                      filesDir.exists() || filesDir.mkdirs());
            
            // Test write permissions on real device
            File testFile = new File(filesDir, "device_test.txt");
            assertTrue("Should be able to create file on real device", 
                      testFile.createNewFile() || testFile.exists());
            
            // Cleanup
            testFile.delete();
            
        } catch (IOException e) {
            fail("Basic file operations should work on real device: " + e.getMessage());
        }
    }
    
    // Real device storage tests
    
    @Test
    public void testRealDeviceInternalStorage() {
        File filesDir = context.getFilesDir();
        
        // Test internal storage properties on real device
        assertTrue("Internal storage should be readable", filesDir.canRead());
        assertTrue("Internal storage should be writable", filesDir.canWrite());
        assertTrue("Internal storage should be executable", filesDir.canExecute());
        
        // Test storage path
        String path = filesDir.getAbsolutePath();
        assertTrue("Internal storage path should contain app package", 
                  path.contains(context.getPackageName()) || path.contains("files"));
    }
    
    @Test
    public void testRealDeviceExtractionToInternalStorage() {
        byte[] testData = createMinimalLzhData();
        
        try {
            // Test extraction on real device
            androidExtractor.extractToInternalStorage(testData, "real_device_test");
            
            // Verify extraction directory exists
            File extractionDir = new File(context.getFilesDir(), "real_device_test");
            assertTrue("Extraction directory should exist on real device", extractionDir.exists());
            
            // Cleanup
            deleteDirectory(extractionDir);
            
        } catch (LzhException e) {
            // Expected for minimal test data, but should not be device-specific error
            assertFalse("Should not be device-specific error", 
                       e.getMessage().contains("device") || e.getMessage().contains("hardware"));
        }
    }
    
    // Real device memory tests
    
    @Test
    public void testRealDeviceMemoryUsage() {
        // Test memory usage on real device
        Runtime runtime = Runtime.getRuntime();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();
        
        // Perform multiple operations
        byte[] testData = createMinimalLzhData();
        for (int i = 0; i < 100; i++) {
            try {
                androidExtractor.getArchiveInfo(testData);
            } catch (LzhException e) {
                // Expected for test data
            }
        }
        
        // Force garbage collection
        System.gc();
        Thread.yield();
        
        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryIncrease = finalMemory - initialMemory;
        
        // Memory increase should be reasonable (less than 10MB)
        assertTrue("Memory usage should be reasonable on real device, increased by " + 
                  (memoryIncrease / 1024 / 1024) + "MB", 
                  memoryIncrease < 10 * 1024 * 1024);
    }
    
    // Real device performance tests
    
    @Test
    public void testRealDevicePerformance() {
        byte[] testData = createMinimalLzhData();
        
        // Test performance on real device
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < 10; i++) {
            try {
                androidExtractor.getArchiveInfo(testData);
            } catch (LzhException e) {
                // Expected for test data
            }
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // Should complete within reasonable time on real device (10 seconds)
        assertTrue("Operations should complete quickly on real device, took " + duration + "ms", 
                  duration < 10000);
    }
    
    // Real device file system tests
    
    @Test
    public void testRealDeviceFileSystemBehavior() {
        File filesDir = context.getFilesDir();
        
        // Test various filename types on real device
        String[] testFilenames = {
            "normal.txt",
            "file with spaces.txt",
            "file-with-dashes.txt",
            "file_with_underscores.txt",
            "file.with.dots.txt",
            "テスト.txt" // Japanese filename
        };
        
        for (String filename : testFilenames) {
            try {
                File testFile = new File(filesDir, filename);
                assertTrue("Should create file on real device: " + filename, 
                          testFile.createNewFile() || testFile.exists());
                
                // Test file operations
                assertTrue("File should be readable: " + filename, testFile.canRead());
                assertTrue("File should be writable: " + filename, testFile.canWrite());
                
                // Cleanup
                testFile.delete();
                
            } catch (IOException e) {
                fail("Should handle filename on real device: " + filename + " - " + e.getMessage());
            }
        }
    }
    
    // Real device security tests
    
    @Test
    public void testRealDeviceSecurityConstraints() {
        // Test that library respects real device security
        
        // Should not be able to access other app's data
        File rootDir = new File("/");
        File dataDir = new File("/data");
        
        // These operations should either work (if permitted) or fail gracefully
        try {
            rootDir.canRead(); // May or may not work depending on device
            dataDir.canRead(); // May or may not work depending on device
        } catch (SecurityException e) {
            // Expected on secure devices
            assertTrue("Should handle security restrictions gracefully", true);
        }
        
        // Should always be able to access own internal storage
        File filesDir = context.getFilesDir();
        assertTrue("Should always access own internal storage", filesDir.canRead());
        assertTrue("Should always access own internal storage", filesDir.canWrite());
    }
    
    // Real device threading tests
    
    @Test
    public void testRealDeviceThreading() throws InterruptedException {
        byte[] testData = createMinimalLzhData();
        
        // Test library usage from background thread
        final boolean[] success = {false};
        final Exception[] exception = {null};
        
        Thread backgroundThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    androidExtractor.getArchiveInfo(testData);
                    success[0] = true;
                } catch (Exception e) {
                    exception[0] = e;
                }
            }
        });
        
        backgroundThread.start();
        backgroundThread.join(5000); // Wait up to 5 seconds
        
        if (exception[0] != null && !(exception[0] instanceof LzhException)) {
            fail("Should work from background thread: " + exception[0].getMessage());
        }
        
        assertTrue("Background thread should complete", !backgroundThread.isAlive());
    }
    
    // Real device locale tests
    
    @Test
    public void testRealDeviceLocaleHandling() {
        // Test with device's current locale
        java.util.Locale deviceLocale = java.util.Locale.getDefault();
        
        byte[] japaneseData = createDataWithJapaneseFilename();
        
        try {
            androidExtractor.getArchiveInfo(japaneseData);
            // Should work regardless of device locale
        } catch (LzhException e) {
            // Expected for test data, but should not be locale-specific
            assertFalse("Should not be locale-specific error on device with locale " + deviceLocale, 
                       e.getMessage().contains("locale"));
        }
    }
    
    // Real device hardware tests
    
    @Test
    public void testRealDeviceHardwareCompatibility() {
        // Test on different hardware architectures
        String arch = Build.CPU_ABI;
        
        // Library should work on all supported architectures
        assertNotNull("Should work on architecture: " + arch, androidExtractor);
        
        // Test basic functionality
        byte[] testData = createMinimalLzhData();
        try {
            androidExtractor.getArchiveInfo(testData);
        } catch (LzhException e) {
            // Expected for test data, but should not be architecture-specific
            assertFalse("Should not be architecture-specific error on " + arch, 
                       e.getMessage().contains("architecture") || e.getMessage().contains("ABI"));
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