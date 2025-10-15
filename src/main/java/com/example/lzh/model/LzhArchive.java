package com.example.lzh.model;

import java.util.List;
import java.util.ArrayList;

/**
 * LZHアーカイブ全体を表現するクラス
 */
public class LzhArchive {
    private List<LzhEntry> entries;
    
    public LzhArchive() {
        this.entries = new ArrayList<>();
    }
    
    /**
     * アーカイブ内のエントリリストを取得
     * @return エントリリスト
     */
    public List<LzhEntry> getEntries() {
        return new ArrayList<>(entries);
    }
    
    /**
     * 指定されたファイル名のエントリを取得
     * @param fileName ファイル名
     * @return エントリ（見つからない場合はnull）
     */
    public LzhEntry getEntry(String fileName) {
        for (LzhEntry entry : entries) {
            if (entry.getFileName().equals(fileName)) {
                return entry;
            }
        }
        return null;
    }
    
    /**
     * エントリ数を取得
     * @return エントリ数
     */
    public int getEntryCount() {
        return entries.size();
    }
    
    /**
     * 全ファイルの合計サイズを取得
     * @return 合計サイズ（バイト）
     */
    public long getTotalSize() {
        long total = 0;
        for (LzhEntry entry : entries) {
            total += entry.getOriginalSize();
        }
        return total;
    }
    
    /**
     * エントリを追加（内部使用）
     * @param entry 追加するエントリ
     */
    public void addEntry(LzhEntry entry) {
        entries.add(entry);
    }
    
    /**
     * 全ファイルの合計圧縮サイズを取得
     * @return 合計圧縮サイズ（バイト）
     */
    public long getTotalCompressedSize() {
        long total = 0;
        for (LzhEntry entry : entries) {
            total += entry.getCompressedSize();
        }
        return total;
    }
    
    /**
     * 圧縮率を計算
     * @return 圧縮率（0.0-1.0）
     */
    public double getCompressionRatio() {
        long originalSize = getTotalSize();
        if (originalSize == 0) {
            return 0.0;
        }
        long compressedSize = getTotalCompressedSize();
        return (double) compressedSize / originalSize;
    }
    
    /**
     * アーカイブの詳細情報を文字列で取得
     * @return アーカイブ情報の文字列表現
     */
    public String getArchiveInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("LZH Archive Information:\n");
        sb.append("  Total entries: ").append(getEntryCount()).append("\n");
        sb.append("  Total original size: ").append(formatBytes(getTotalSize())).append("\n");
        sb.append("  Total compressed size: ").append(formatBytes(getTotalCompressedSize())).append("\n");
        sb.append("  Compression ratio: ").append(String.format("%.1f%%", getCompressionRatio() * 100)).append("\n");
        sb.append("\nFile entries:\n");
        
        for (int i = 0; i < entries.size(); i++) {
            LzhEntry entry = entries.get(i);
            sb.append(String.format("  %d. %s\n", i + 1, entry.getFileName()));
            sb.append(String.format("     Method: %s, Size: %s -> %s (%.1f%%)\n",
                entry.getCompressionMethod(),
                formatBytes(entry.getOriginalSize()),
                formatBytes(entry.getCompressedSize()),
                entry.getOriginalSize() > 0 ? (double) entry.getCompressedSize() / entry.getOriginalSize() * 100 : 0.0));
            if (entry.getLastModified() != null) {
                sb.append("     Modified: ").append(entry.getLastModified()).append("\n");
            }
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
        return getArchiveInfo();
    }
}