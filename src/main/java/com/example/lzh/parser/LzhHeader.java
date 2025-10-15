package com.example.lzh.parser;

import java.io.InputStream;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Logger;
import com.example.lzh.model.LzhEntry;
import com.example.lzh.exception.InvalidArchiveException;
import com.example.lzh.exception.CorruptedArchiveException;
import com.example.lzh.util.EncodingDetector;

/**
 * LZHヘッダー解析クラス
 */
public class LzhHeader {
    
    private static final Logger logger = Logger.getLogger(LzhHeader.class.getName());
    
    // LZHヘッダーの固定フィールドサイズ
    private static final int MIN_HEADER_SIZE = 21;
    private static final int COMPRESSION_METHOD_SIZE = 5;
    
    /**
     * ヘッダーを解析してLzhEntryを作成
     * @param input 入力ストリーム
     * @return 解析されたエントリ
     * @throws IOException I/Oエラー
     */
    public static LzhEntry parseHeader(InputStream input) throws IOException {
        // ヘッダーサイズを読み取り
        int headerSize = input.read();
        if (headerSize == -1) {
            return null; // ストリーム終端
        }
        if (headerSize == 0) {
            return null; // 終端マーカー
        }
        
        if (headerSize < MIN_HEADER_SIZE) {
            throw new InvalidArchiveException("Invalid header size: " + headerSize);
        }
        
        // 残りのヘッダーデータを読み取り
        byte[] headerData = new byte[headerSize];
        headerData[0] = (byte) headerSize;
        
        int bytesRead = input.read(headerData, 1, headerSize - 1);
        if (bytesRead != headerSize - 1) {
            throw new CorruptedArchiveException("Incomplete header data");
        }
        
        return parseHeaderData(headerData);
    }
    
    /**
     * ヘッダーデータを解析してLzhEntryを作成
     * @param headerData ヘッダーのバイト配列
     * @return 解析されたエントリ
     * @throws IOException 解析エラー
     */
    private static LzhEntry parseHeaderData(byte[] headerData) throws IOException {
        if (headerData.length < MIN_HEADER_SIZE) {
            throw new InvalidArchiveException("Header too short");
        }
        
        int offset = 1; // ヘッダーサイズはすでに読み取り済み
        
        // チェックサム (1byte)
        int checksum = headerData[offset++] & 0xFF;
        
        // 圧縮方式 (5bytes)
        byte[] methodBytes = new byte[COMPRESSION_METHOD_SIZE];
        System.arraycopy(headerData, offset, methodBytes, 0, COMPRESSION_METHOD_SIZE);
        String compressionMethod = new String(methodBytes, "ASCII");
        offset += COMPRESSION_METHOD_SIZE;
        
        // 圧縮後サイズ (4bytes, little-endian)
        long compressedSize = readLittleEndianInt(headerData, offset);
        offset += 4;
        
        // 元サイズ (4bytes, little-endian)
        long originalSize = readLittleEndianInt(headerData, offset);
        offset += 4;
        
        // 最終更新日時 (4bytes)
        Date lastModified = parseDateTime(headerData, offset);
        offset += 4;
        
        // ファイル属性 (1byte)
        int fileAttribute = headerData[offset++] & 0xFF;
        
        // ファイル名長 (1byte)
        int fileNameLength = headerData[offset++] & 0xFF;
        
        // ファイル名
        if (offset + fileNameLength > headerData.length) {
            throw new CorruptedArchiveException("Invalid filename length");
        }
        
        byte[] fileNameBytes = new byte[fileNameLength];
        System.arraycopy(headerData, offset, fileNameBytes, 0, fileNameLength);
        
        // ファイル名をデコードし、警告があればログに記録
        EncodingDetector.DecodeResult decodeResult = EncodingDetector.decodeFileNameWithWarning(fileNameBytes);
        String fileName = decodeResult.getDecodedName();
        
        if (decodeResult.getWarning() != null) {
            logger.warning("ファイル名のエンコーディング警告: " + decodeResult.getWarning() + 
                          " (ファイル名: " + fileName + ")");
        }
        
        // LzhEntryを作成
        LzhEntry entry = new LzhEntry();
        entry.setFileName(fileName);
        entry.setOriginalSize(originalSize);
        entry.setCompressedSize(compressedSize);
        entry.setCompressionMethod(compressionMethod);
        entry.setCrc16(checksum);
        entry.setLastModified(lastModified);
        
        return entry;
    }
    
    /**
     * Little-endianで4バイト整数を読み取り
     * @param data バイト配列
     * @param offset オフセット
     * @return 読み取った整数値
     */
    private static long readLittleEndianInt(byte[] data, int offset) {
        return ((long)(data[offset] & 0xFF)) |
               ((long)(data[offset + 1] & 0xFF) << 8) |
               ((long)(data[offset + 2] & 0xFF) << 16) |
               ((long)(data[offset + 3] & 0xFF) << 24);
    }
    

    
    /**
     * 日時をパース
     * @param data バイト配列
     * @param offset オフセット
     * @return パースされた日時
     */
    private static Date parseDateTime(byte[] data, int offset) {
        // LZH形式の日時は4バイトのDOS形式
        long dosDateTime = readLittleEndianInt(data, offset);
        
        // DOS日時形式をJavaのDateに変換
        int dosTime = (int)(dosDateTime & 0xFFFF);
        int dosDate = (int)((dosDateTime >> 16) & 0xFFFF);
        
        // DOS時刻の解析 (hhhhhmmmmmmsssss)
        int seconds = (dosTime & 0x1F) * 2;
        int minutes = (dosTime >> 5) & 0x3F;
        int hours = (dosTime >> 11) & 0x1F;
        
        // DOS日付の解析 (yyyyyyymmmmddddd)
        int day = dosDate & 0x1F;
        int month = ((dosDate >> 5) & 0x0F) - 1; // Javaの月は0ベース
        int year = ((dosDate >> 9) & 0x7F) + 1980;
        
        // 無効な日時の場合は現在時刻を返す
        if (year < 1980 || month < 0 || month > 11 || day < 1 || day > 31 ||
            hours > 23 || minutes > 59 || seconds > 59) {
            return new Date();
        }
        
        // Java Calendarを使用して日時を作成
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.set(year, month, day, hours, minutes, seconds);
        calendar.set(java.util.Calendar.MILLISECOND, 0);
        
        return calendar.getTime();
    }
}