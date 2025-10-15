package com.example.lzh.util;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test class for EncodingDetector to verify UTF-8 and Shift_JIS detection
 */
public class EncodingDetectorTest {
    
    @Test
    public void testUtf8Detection() {
        // UTF-8 encoded Japanese text: "テスト.txt"
        byte[] utf8Bytes = {(byte)0xE3, (byte)0x83, (byte)0x86, (byte)0xE3, (byte)0x82, (byte)0xB9, 
                           (byte)0xE3, (byte)0x83, (byte)0x88, 0x2E, 0x74, 0x78, 0x74};
        
        EncodingDetector.DetectionResult result = EncodingDetector.detectEncodingWithConfidence(utf8Bytes);
        assertEquals("UTF-8", result.getEncoding());
        assertTrue("Should be confident about UTF-8 detection", result.isConfident());
    }
    
    @Test
    public void testShiftJisDetection() {
        // Shift_JIS encoded Japanese text: "テスト.txt"
        byte[] shiftJisBytes = {(byte)0x83, (byte)0x65, (byte)0x83, (byte)0x58, (byte)0x83, (byte)0x67, 
                               0x2E, 0x74, 0x78, 0x74};
        
        EncodingDetector.DetectionResult result = EncodingDetector.detectEncodingWithConfidence(shiftJisBytes);
        assertEquals("Shift_JIS", result.getEncoding());
        assertTrue("Should be confident about Shift_JIS detection", result.isConfident());
    }
    
    @Test
    public void testAsciiDetection() {
        // ASCII text: "test.txt"
        byte[] asciiBytes = {0x74, 0x65, 0x73, 0x74, 0x2E, 0x74, 0x78, 0x74};
        
        EncodingDetector.DetectionResult result = EncodingDetector.detectEncodingWithConfidence(asciiBytes);
        assertEquals("UTF-8", result.getEncoding());
        assertTrue("Should be confident about ASCII/UTF-8 detection", result.isConfident());
        assertNull("Should not have warning for ASCII", result.getWarning());
    }
    
    @Test
    public void testDefaultEncodingFallback() {
        // Invalid bytes that don't match any encoding
        byte[] invalidBytes = {(byte)0xFF, (byte)0xFE, (byte)0xFD};
        
        EncodingDetector.DetectionResult result = EncodingDetector.detectEncodingWithConfidence(invalidBytes);
        assertEquals("UTF-8", result.getEncoding());
        assertFalse("Should not be confident about detection", result.isConfident());
        assertNotNull("Should have warning", result.getWarning());
    }
    
    @Test
    public void testFileNameDecoding() {
        // UTF-8 encoded Japanese filename
        byte[] utf8Filename = {(byte)0xE3, (byte)0x83, (byte)0x86, (byte)0xE3, (byte)0x82, (byte)0xB9, 
                              (byte)0xE3, (byte)0x83, (byte)0x88, 0x2E, 0x74, 0x78, 0x74};
        
        EncodingDetector.DecodeResult result = EncodingDetector.decodeFileNameWithWarning(utf8Filename);
        assertEquals("テスト.txt", result.getDecodedName());
        assertNull("Should not have warning for valid UTF-8", result.getWarning());
    }
    
    @Test
    public void testFileNameDecodingWithWarning() {
        // Shift_JIS encoded Japanese filename
        byte[] shiftJisFilename = {(byte)0x83, (byte)0x65, (byte)0x83, (byte)0x58, (byte)0x83, (byte)0x67, 
                                  0x2E, 0x74, 0x78, 0x74};
        
        EncodingDetector.DecodeResult result = EncodingDetector.decodeFileNameWithWarning(shiftJisFilename);
        assertEquals("テスト.txt", result.getDecodedName());
        // May or may not have warning depending on detection confidence
    }
    
    // Additional comprehensive tests for edge cases
    
    @Test
    public void testEmptyInput() {
        byte[] emptyBytes = {};
        
        EncodingDetector.DetectionResult result = EncodingDetector.detectEncodingWithConfidence(emptyBytes);
        assertEquals("UTF-8", result.getEncoding());
        assertTrue("Should be confident about empty input", result.isConfident());
        assertNull("Should not have warning for empty input", result.getWarning());
    }
    
    @Test
    public void testNullInput() {
        EncodingDetector.DetectionResult result = EncodingDetector.detectEncodingWithConfidence(null);
        assertEquals("UTF-8", result.getEncoding());
        assertTrue("Should be confident about null input", result.isConfident());
        assertNull("Should not have warning for null input", result.getWarning());
    }
    
    @Test
    public void testMixedEncodingAmbiguousCase() {
        // Bytes that could be valid in both UTF-8 and Shift_JIS
        byte[] ambiguousBytes = {(byte)0x82, (byte)0xA0}; // Could be valid in both encodings
        
        EncodingDetector.DetectionResult result = EncodingDetector.detectEncodingWithConfidence(ambiguousBytes);
        assertNotNull("Should return an encoding", result.getEncoding());
        // Should have low confidence or warning for ambiguous cases
        assertTrue("Should either have low confidence or warning", 
                  !result.isConfident() || result.getWarning() != null);
    }
    
    @Test
    public void testInvalidUtf8Sequence() {
        // Invalid UTF-8: incomplete multi-byte sequence
        byte[] invalidUtf8 = {(byte)0xE3, (byte)0x83}; // Incomplete 3-byte sequence
        
        EncodingDetector.DetectionResult result = EncodingDetector.detectEncodingWithConfidence(invalidUtf8);
        assertNotNull("Should return an encoding", result.getEncoding());
        assertFalse("Should not be confident about invalid UTF-8", result.isConfident());
    }
    
    @Test
    public void testInvalidShiftJisSequence() {
        // Invalid Shift_JIS: first byte in range but invalid second byte
        byte[] invalidShiftJis = {(byte)0x81, (byte)0x30}; // Invalid second byte
        
        EncodingDetector.DetectionResult result = EncodingDetector.detectEncodingWithConfidence(invalidShiftJis);
        assertNotNull("Should return an encoding", result.getEncoding());
        // Should handle gracefully
    }
    
    @Test
    public void testHalfWidthKatakana() {
        // Half-width katakana in Shift_JIS: "ｱｲｳ"
        byte[] halfWidthKatakana = {(byte)0xB1, (byte)0xB2, (byte)0xB3};
        
        EncodingDetector.DetectionResult result = EncodingDetector.detectEncodingWithConfidence(halfWidthKatakana);
        assertEquals("Shift_JIS", result.getEncoding());
        assertTrue("Should be confident about half-width katakana", result.isConfident());
    }
    
    @Test
    public void testLongJapaneseText() {
        // Longer UTF-8 Japanese text: "これはテストファイルです.txt"
        byte[] longUtf8Text = {
            (byte)0xE3, (byte)0x81, (byte)0x93, (byte)0xE3, (byte)0x82, (byte)0x8C, // これ
            (byte)0xE3, (byte)0x81, (byte)0xAF, // は
            (byte)0xE3, (byte)0x83, (byte)0x86, (byte)0xE3, (byte)0x82, (byte)0xB9, (byte)0xE3, (byte)0x83, (byte)0x88, // テスト
            (byte)0xE3, (byte)0x83, (byte)0x95, (byte)0xE3, (byte)0x82, (byte)0xA1, (byte)0xE3, (byte)0x82, (byte)0xA4, (byte)0xE3, (byte)0x83, (byte)0xAB, // ファイル
            (byte)0xE3, (byte)0x81, (byte)0xA7, (byte)0xE3, (byte)0x81, (byte)0x99, // です
            0x2E, 0x74, 0x78, 0x74 // .txt
        };
        
        EncodingDetector.DetectionResult result = EncodingDetector.detectEncodingWithConfidence(longUtf8Text);
        assertEquals("UTF-8", result.getEncoding());
        assertTrue("Should be confident about long UTF-8 text", result.isConfident());
        
        EncodingDetector.DecodeResult decodeResult = EncodingDetector.decodeFileNameWithWarning(longUtf8Text);
        assertEquals("これはテストファイルです.txt", decodeResult.getDecodedName());
        assertNull("Should not have warning for valid long UTF-8", decodeResult.getWarning());
    }
    
    @Test
    public void testFallbackDecoding() {
        // Test fallback to ISO-8859-1 for completely invalid data
        byte[] invalidData = {(byte)0xFF, (byte)0xFE, (byte)0xFD, (byte)0xFC};
        
        EncodingDetector.DecodeResult result = EncodingDetector.decodeFileNameWithWarning(invalidData);
        assertNotNull("Should return decoded name even for invalid data", result.getDecodedName());
        assertNotNull("Should have warning for fallback decoding", result.getWarning());
        assertTrue("Warning should mention fallback", result.getWarning().contains("ISO-8859-1"));
    }
    
    @Test
    public void testSimpleApiMethods() {
        // Test the simple API methods that don't return detailed results
        byte[] utf8Bytes = {(byte)0xE3, (byte)0x83, (byte)0x86, (byte)0xE3, (byte)0x82, (byte)0xB9, (byte)0xE3, (byte)0x83, (byte)0x88};
        
        String encoding = EncodingDetector.detectEncoding(utf8Bytes);
        assertEquals("UTF-8", encoding);
        
        String decoded = EncodingDetector.decodeFileName(utf8Bytes);
        assertEquals("テスト", decoded);
    }
}