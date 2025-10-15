package com.example.lzh.model;

import java.util.Date;

/**
 * LZHアーカイブ内の個別ファイルエントリを表現するクラス
 */
public class LzhEntry {
    private String fileName;
    private long originalSize;
    private long compressedSize;
    private String compressionMethod;
    private int crc16;
    private Date lastModified;
    
    public LzhEntry() {
        // デフォルトコンストラクタ
    }
    
    public LzhEntry(String fileName, long originalSize, long compressedSize, 
                   String compressionMethod, int crc16, Date lastModified) {
        this.fileName = fileName;
        this.originalSize = originalSize;
        this.compressedSize = compressedSize;
        this.compressionMethod = compressionMethod;
        this.crc16 = crc16;
        this.lastModified = lastModified != null ? new Date(lastModified.getTime()) : null;
    }
    
    /**
     * ファイル名を取得
     * @return ファイル名
     */
    public String getFileName() {
        return fileName;
    }
    
    /**
     * ファイル名を設定
     * @param fileName ファイル名
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    /**
     * 元のファイルサイズを取得
     * @return 元のファイルサイズ（バイト）
     */
    public long getOriginalSize() {
        return originalSize;
    }
    
    /**
     * 元のファイルサイズを設定
     * @param originalSize 元のファイルサイズ（バイト）
     */
    public void setOriginalSize(long originalSize) {
        this.originalSize = originalSize;
    }
    
    /**
     * 圧縮後のファイルサイズを取得
     * @return 圧縮後のファイルサイズ（バイト）
     */
    public long getCompressedSize() {
        return compressedSize;
    }
    
    /**
     * 圧縮後のファイルサイズを設定
     * @param compressedSize 圧縮後のファイルサイズ（バイト）
     */
    public void setCompressedSize(long compressedSize) {
        this.compressedSize = compressedSize;
    }
    
    /**
     * 圧縮方式を取得
     * @return 圧縮方式
     */
    public String getCompressionMethod() {
        return compressionMethod;
    }
    
    /**
     * 圧縮方式を設定
     * @param compressionMethod 圧縮方式
     */
    public void setCompressionMethod(String compressionMethod) {
        this.compressionMethod = compressionMethod;
    }
    
    /**
     * CRC16チェックサムを取得
     * @return CRC16チェックサム
     */
    public int getCrc16() {
        return crc16;
    }
    
    /**
     * CRC16チェックサムを設定
     * @param crc16 CRC16チェックサム
     */
    public void setCrc16(int crc16) {
        this.crc16 = crc16;
    }
    
    /**
     * 最終更新日時を取得
     * @return 最終更新日時
     */
    public Date getLastModified() {
        return lastModified != null ? new Date(lastModified.getTime()) : null;
    }
    
    /**
     * 最終更新日時を設定
     * @param lastModified 最終更新日時
     */
    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified != null ? new Date(lastModified.getTime()) : null;
    }
    
    /**
     * 圧縮率を計算
     * @return 圧縮率（0.0-1.0）
     */
    public double getCompressionRatio() {
        if (originalSize == 0) {
            return 0.0;
        }
        return (double) compressedSize / originalSize;
    }
    
    /**
     * エントリの詳細情報を文字列で取得
     * @return エントリ情報の文字列表現
     */
    public String getDetailedInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("File: ").append(fileName).append("\n");
        sb.append("  Compression method: ").append(compressionMethod).append("\n");
        sb.append("  Original size: ").append(formatBytes(originalSize)).append("\n");
        sb.append("  Compressed size: ").append(formatBytes(compressedSize)).append("\n");
        sb.append("  Compression ratio: ").append(String.format("%.1f%%", getCompressionRatio() * 100)).append("\n");
        sb.append("  CRC16: 0x").append(String.format("%04X", crc16)).append("\n");
        if (lastModified != null) {
            sb.append("  Last modified: ").append(lastModified).append("\n");
        }
        return sb.toString();
    }
    
    /**
     * バイト数を人間が読みやすい形式でフォーマット
     * @param bytes バイト数
     * @return フォーマットされた文字列
     */
    private String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }
    
    @Override
    public String toString() {
        return "LzhEntry{" +
                "fileName='" + fileName + '\'' +
                ", originalSize=" + originalSize +
                ", compressedSize=" + compressedSize +
                ", compressionMethod='" + compressionMethod + '\'' +
                ", crc16=" + crc16 +
                ", lastModified=" + lastModified +
                '}';
    }
}