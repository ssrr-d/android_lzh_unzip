package com.example.lzh.model;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Date;
import java.util.List;

/**
 * Unit tests for LzhArchive model class
 */
public class LzhArchiveTest {
    
    private LzhArchive archive;
    private LzhEntry entry1;
    private LzhEntry entry2;
    private LzhEntry entry3;
    
    @Before
    public void setUp() {
        archive = new LzhArchive();
        
        // Create test entries
        entry1 = new LzhEntry("file1.txt", 1000, 500, "-lh5-", 0x1234, new Date());
        entry2 = new LzhEntry("file2.txt", 2000, 800, "-lh1-", 0x5678, new Date());
        entry3 = new LzhEntry("file3.txt", 500, 500, "-lh0-", 0x9ABC, new Date());
    }
    
    // Constructor tests
    
    @Test
    public void testDefaultConstructor() {
        LzhArchive newArchive = new LzhArchive();
        assertNotNull("Entries list should not be null", newArchive.getEntries());
        assertEquals("Entry count should be 0", 0, newArchive.getEntryCount());
        assertTrue("Entries list should be empty", newArchive.getEntries().isEmpty());
    }
    
    // Entry management tests
    
    @Test
    public void testAddEntry() {
        assertEquals("Initial entry count should be 0", 0, archive.getEntryCount());
        
        archive.addEntry(entry1);
        assertEquals("Entry count should be 1 after adding one entry", 1, archive.getEntryCount());
        
        archive.addEntry(entry2);
        assertEquals("Entry count should be 2 after adding two entries", 2, archive.getEntryCount());
    }
    
    @Test
    public void testGetEntries() {
        archive.addEntry(entry1);
        archive.addEntry(entry2);
        
        List<LzhEntry> entries = archive.getEntries();
        assertEquals("Should return correct number of entries", 2, entries.size());
        assertEquals("First entry should match", entry1, entries.get(0));
        assertEquals("Second entry should match", entry2, entries.get(1));
    }
    
    @Test
    public void testGetEntriesReturnsDefensiveCopy() {
        archive.addEntry(entry1);
        
        List<LzhEntry> entries1 = archive.getEntries();
        List<LzhEntry> entries2 = archive.getEntries();
        
        assertNotSame("Should return different list instances", entries1, entries2);
        assertEquals("Lists should have same content", entries1, entries2);
        
        // Modifying returned list should not affect archive
        entries1.clear();
        assertEquals("Archive should still have entries after clearing returned list", 
                    1, archive.getEntryCount());
    }
    
    @Test
    public void testGetEntry() {
        archive.addEntry(entry1);
        archive.addEntry(entry2);
        
        LzhEntry found = archive.getEntry("file1.txt");
        assertEquals("Should find correct entry by filename", entry1, found);
        
        found = archive.getEntry("file2.txt");
        assertEquals("Should find correct entry by filename", entry2, found);
        
        found = archive.getEntry("nonexistent.txt");
        assertNull("Should return null for non-existent file", found);
    }
    
    @Test
    public void testGetEntryWithNullFilename() {
        archive.addEntry(entry1);
        
        LzhEntry found = archive.getEntry(null);
        assertNull("Should return null for null filename", found);
    }
    
    @Test
    public void testGetEntryWithDuplicateFilenames() {
        LzhEntry duplicate = new LzhEntry("file1.txt", 1500, 750, "-lh1-", 0xDEF0, new Date());
        
        archive.addEntry(entry1);
        archive.addEntry(duplicate);
        
        LzhEntry found = archive.getEntry("file1.txt");
        assertEquals("Should return first entry with matching filename", entry1, found);
    }
    
    // Size calculation tests
    
    @Test
    public void testGetTotalSize() {
        assertEquals("Empty archive should have total size 0", 0, archive.getTotalSize());
        
        archive.addEntry(entry1); // 1000 bytes
        assertEquals("Total size should match single entry", 1000, archive.getTotalSize());
        
        archive.addEntry(entry2); // 2000 bytes
        assertEquals("Total size should be sum of entries", 3000, archive.getTotalSize());
        
        archive.addEntry(entry3); // 500 bytes
        assertEquals("Total size should be sum of all entries", 3500, archive.getTotalSize());
    }
    
    @Test
    public void testGetTotalCompressedSize() {
        assertEquals("Empty archive should have total compressed size 0", 0, archive.getTotalCompressedSize());
        
        archive.addEntry(entry1); // 500 bytes compressed
        assertEquals("Total compressed size should match single entry", 500, archive.getTotalCompressedSize());
        
        archive.addEntry(entry2); // 800 bytes compressed
        assertEquals("Total compressed size should be sum of entries", 1300, archive.getTotalCompressedSize());
        
        archive.addEntry(entry3); // 500 bytes compressed
        assertEquals("Total compressed size should be sum of all entries", 1800, archive.getTotalCompressedSize());
    }
    
    @Test
    public void testGetCompressionRatio() {
        // Empty archive
        assertEquals("Empty archive should have compression ratio 0.0", 0.0, archive.getCompressionRatio(), 0.001);
        
        // Single entry
        archive.addEntry(entry1); // 1000 -> 500 (0.5 ratio)
        assertEquals("Single entry compression ratio should be correct", 0.5, archive.getCompressionRatio(), 0.001);
        
        // Multiple entries
        archive.addEntry(entry2); // 2000 -> 800 (total: 3000 -> 1300)
        double expectedRatio = 1300.0 / 3000.0;
        assertEquals("Multiple entries compression ratio should be correct", 
                    expectedRatio, archive.getCompressionRatio(), 0.001);
        
        // Add uncompressed entry
        archive.addEntry(entry3); // 500 -> 500 (total: 3500 -> 1800)
        expectedRatio = 1800.0 / 3500.0;
        assertEquals("Compression ratio with uncompressed entry should be correct", 
                    expectedRatio, archive.getCompressionRatio(), 0.001);
    }
    
    @Test
    public void testGetCompressionRatioWithZeroOriginalSize() {
        LzhEntry zeroSizeEntry = new LzhEntry("empty.txt", 0, 0, "-lh0-", 0, new Date());
        archive.addEntry(zeroSizeEntry);
        
        assertEquals("Archive with zero original size should have compression ratio 0.0", 
                    0.0, archive.getCompressionRatio(), 0.001);
    }
    
    // Archive info tests
    
    @Test
    public void testGetArchiveInfoEmpty() {
        String info = archive.getArchiveInfo();
        
        assertTrue("Should contain total entries", info.contains("Total entries: 0"));
        assertTrue("Should contain total original size", info.contains("Total original size: 0 B"));
        assertTrue("Should contain total compressed size", info.contains("Total compressed size: 0 B"));
        assertTrue("Should contain compression ratio", info.contains("Compression ratio: 0.0%"));
        assertTrue("Should contain file entries section", info.contains("File entries:"));
    }
    
    @Test
    public void testGetArchiveInfoWithEntries() {
        archive.addEntry(entry1);
        archive.addEntry(entry2);
        
        String info = archive.getArchiveInfo();
        
        assertTrue("Should contain correct entry count", info.contains("Total entries: 2"));
        assertTrue("Should contain total original size", info.contains("Total original size: 2.9 KB"));
        assertTrue("Should contain total compressed size", info.contains("Total compressed size: 1.3 KB"));
        assertTrue("Should contain file1.txt", info.contains("file1.txt"));
        assertTrue("Should contain file2.txt", info.contains("file2.txt"));
        assertTrue("Should contain compression methods", info.contains("-lh5-"));
        assertTrue("Should contain compression methods", info.contains("-lh1-"));
    }
    
    @Test
    public void testGetArchiveInfoByteFormatting() {
        // Test different size formats
        LzhEntry smallEntry = new LzhEntry("small.txt", 512, 256, "-lh5-", 0, new Date());
        LzhEntry mediumEntry = new LzhEntry("medium.txt", 2048, 1024, "-lh5-", 0, new Date());
        LzhEntry largeEntry = new LzhEntry("large.txt", 2 * 1024 * 1024, 1024 * 1024, "-lh5-", 0, new Date());
        
        archive.addEntry(smallEntry);
        archive.addEntry(mediumEntry);
        archive.addEntry(largeEntry);
        
        String info = archive.getArchiveInfo();
        
        assertTrue("Should format bytes correctly", info.contains("512 B"));
        assertTrue("Should format KB correctly", info.contains("2.0 KB"));
        assertTrue("Should format MB correctly", info.contains("2.0 MB"));
    }
    
    // toString tests
    
    @Test
    public void testToString() {
        archive.addEntry(entry1);
        
        String toString = archive.toString();
        String archiveInfo = archive.getArchiveInfo();
        
        assertEquals("toString should return same as getArchiveInfo", archiveInfo, toString);
    }
    
    // Edge case tests
    
    @Test
    public void testLargeNumberOfEntries() {
        // Add many entries
        for (int i = 0; i < 1000; i++) {
            LzhEntry entry = new LzhEntry("file" + i + ".txt", 1000 + i, 500 + i, "-lh5-", i, new Date());
            archive.addEntry(entry);
        }
        
        assertEquals("Should handle large number of entries", 1000, archive.getEntryCount());
        
        // Test that calculations still work
        long expectedOriginalSize = 0;
        long expectedCompressedSize = 0;
        for (int i = 0; i < 1000; i++) {
            expectedOriginalSize += 1000 + i;
            expectedCompressedSize += 500 + i;
        }
        
        assertEquals("Total original size should be correct", expectedOriginalSize, archive.getTotalSize());
        assertEquals("Total compressed size should be correct", expectedCompressedSize, archive.getTotalCompressedSize());
    }
    
    @Test
    public void testEntriesWithSpecialCharacters() {
        LzhEntry japaneseEntry = new LzhEntry("テスト.txt", 1000, 500, "-lh5-", 0x1234, new Date());
        LzhEntry pathEntry = new LzhEntry("folder/subfolder/file.txt", 2000, 1000, "-lh1-", 0x5678, new Date());
        
        archive.addEntry(japaneseEntry);
        archive.addEntry(pathEntry);
        
        assertEquals("Should handle Japanese filename", japaneseEntry, archive.getEntry("テスト.txt"));
        assertEquals("Should handle path filename", pathEntry, archive.getEntry("folder/subfolder/file.txt"));
        
        String info = archive.getArchiveInfo();
        assertTrue("Archive info should contain Japanese filename", info.contains("テスト.txt"));
        assertTrue("Archive info should contain path filename", info.contains("folder/subfolder/file.txt"));
    }
    
    @Test
    public void testEntriesWithZeroSizes() {
        LzhEntry zeroEntry = new LzhEntry("zero.txt", 0, 0, "-lh0-", 0, new Date());
        archive.addEntry(zeroEntry);
        
        assertEquals("Should handle zero-size entry", 0, archive.getTotalSize());
        assertEquals("Should handle zero-size entry", 0, archive.getTotalCompressedSize());
        assertEquals("Compression ratio should be 0.0 for zero sizes", 0.0, archive.getCompressionRatio(), 0.001);
    }
    
    @Test
    public void testEntriesWithExpansion() {
        // Entry where compressed size is larger than original (expansion)
        LzhEntry expandedEntry = new LzhEntry("expanded.txt", 100, 150, "-lh0-", 0, new Date());
        archive.addEntry(expandedEntry);
        
        assertEquals("Should handle expansion case", 100, archive.getTotalSize());
        assertEquals("Should handle expansion case", 150, archive.getTotalCompressedSize());
        assertEquals("Compression ratio should be > 1.0 for expansion", 1.5, archive.getCompressionRatio(), 0.001);
    }
    
    @Test
    public void testGetEntryWithEmptyString() {
        LzhEntry emptyNameEntry = new LzhEntry("", 1000, 500, "-lh5-", 0, new Date());
        archive.addEntry(emptyNameEntry);
        
        LzhEntry found = archive.getEntry("");
        assertEquals("Should find entry with empty filename", emptyNameEntry, found);
    }
    
    @Test
    public void testArchiveInfoWithNullDates() {
        LzhEntry nullDateEntry = new LzhEntry("nulldate.txt", 1000, 500, "-lh5-", 0x1234, null);
        archive.addEntry(nullDateEntry);
        
        String info = archive.getArchiveInfo();
        assertTrue("Should handle null dates in archive info", info.contains("nulldate.txt"));
        // Should not crash when date is null
    }
}