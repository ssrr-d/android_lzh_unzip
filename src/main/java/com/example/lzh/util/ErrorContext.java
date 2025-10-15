package com.example.lzh.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Collections;

/**
 * エラーコンテキスト情報を管理するユーティリティクラス
 * 詳細なエラー情報とデバッグ情報を提供
 * 要件 7.1, 7.2, 7.3, 7.4 に対応
 */
public class ErrorContext {
    
    private final String operation;
    private final Map<String, Object> contextData;
    private final long timestamp;
    private String fileName;
    private String compressionMethod;
    private Long fileSize;
    private Long processedBytes;
    private String inputSource;
    private String outputPath;
    
    /**
     * エラーコンテキストを作成
     * @param operation 実行中の操作名
     */
    public ErrorContext(String operation) {
        this.operation = operation;
        this.contextData = new HashMap<>();
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * ファイル名を設定
     * @param fileName ファイル名
     * @return このインスタンス（メソッドチェーン用）
     */
    public ErrorContext withFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }
    
    /**
     * 圧縮方式を設定
     * @param compressionMethod 圧縮方式
     * @return このインスタンス（メソッドチェーン用）
     */
    public ErrorContext withCompressionMethod(String compressionMethod) {
        this.compressionMethod = compressionMethod;
        return this;
    }
    
    /**
     * ファイルサイズを設定
     * @param fileSize ファイルサイズ
     * @return このインスタンス（メソッドチェーン用）
     */
    public ErrorContext withFileSize(Long fileSize) {
        this.fileSize = fileSize;
        return this;
    }
    
    /**
     * 処理済みバイト数を設定
     * @param processedBytes 処理済みバイト数
     * @return このインスタンス（メソッドチェーン用）
     */
    public ErrorContext withProcessedBytes(Long processedBytes) {
        this.processedBytes = processedBytes;
        return this;
    }
    
    /**
     * 入力ソースを設定
     * @param inputSource 入力ソース（ファイルパス、ストリーム種別など）
     * @return このインスタンス（メソッドチェーン用）
     */
    public ErrorContext withInputSource(String inputSource) {
        this.inputSource = inputSource;
        return this;
    }
    
    /**
     * 出力パスを設定
     * @param outputPath 出力パス
     * @return このインスタンス（メソッドチェーン用）
     */
    public ErrorContext withOutputPath(String outputPath) {
        this.outputPath = outputPath;
        return this;
    }
    
    /**
     * カスタムデータを追加
     * @param key キー
     * @param value 値
     * @return このインスタンス（メソッドチェーン用）
     */
    public ErrorContext withData(String key, Object value) {
        this.contextData.put(key, value);
        return this;
    }
    
    /**
     * 複数のカスタムデータを追加
     * @param data データマップ
     * @return このインスタンス（メソッドチェーン用）
     */
    public ErrorContext withData(Map<String, Object> data) {
        this.contextData.putAll(data);
        return this;
    }
    
    /**
     * 操作名を取得
     * @return 操作名
     */
    public String getOperation() {
        return operation;
    }
    
    /**
     * ファイル名を取得
     * @return ファイル名
     */
    public String getFileName() {
        return fileName;
    }
    
    /**
     * 圧縮方式を取得
     * @return 圧縮方式
     */
    public String getCompressionMethod() {
        return compressionMethod;
    }
    
    /**
     * ファイルサイズを取得
     * @return ファイルサイズ
     */
    public Long getFileSize() {
        return fileSize;
    }
    
    /**
     * 処理済みバイト数を取得
     * @return 処理済みバイト数
     */
    public Long getProcessedBytes() {
        return processedBytes;
    }
    
    /**
     * 入力ソースを取得
     * @return 入力ソース
     */
    public String getInputSource() {
        return inputSource;
    }
    
    /**
     * 出力パスを取得
     * @return 出力パス
     */
    public String getOutputPath() {
        return outputPath;
    }
    
    /**
     * タイムスタンプを取得
     * @return タイムスタンプ
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * コンテキストデータを取得
     * @return コンテキストデータ（読み取り専用）
     */
    public Map<String, Object> getContextData() {
        return Collections.unmodifiableMap(contextData);
    }
    
    /**
     * 詳細なエラーメッセージを生成
     * @param baseMessage 基本エラーメッセージ
     * @return 詳細なエラーメッセージ
     */
    public String createDetailedMessage(String baseMessage) {
        StringBuilder sb = new StringBuilder(baseMessage);
        
        if (operation != null) {
            sb.append(" [操作: ").append(operation).append("]");
        }
        
        if (fileName != null) {
            sb.append(" [ファイル: ").append(fileName).append("]");
        }
        
        if (compressionMethod != null) {
            sb.append(" [圧縮方式: ").append(compressionMethod).append("]");
        }
        
        if (inputSource != null) {
            sb.append(" [入力: ").append(inputSource).append("]");
        }
        
        if (outputPath != null) {
            sb.append(" [出力: ").append(outputPath).append("]");
        }
        
        if (fileSize != null) {
            sb.append(" [サイズ: ").append(formatBytes(fileSize)).append("]");
        }
        
        if (processedBytes != null) {
            sb.append(" [処理済み: ").append(formatBytes(processedBytes));
            if (fileSize != null && fileSize > 0) {
                double progress = (double) processedBytes / fileSize * 100;
                sb.append(" (").append(String.format("%.1f", progress)).append("%)");
            }
            sb.append("]");
        }
        
        // カスタムデータを追加
        if (!contextData.isEmpty()) {
            sb.append(" [詳細: ");
            boolean first = true;
            for (Map.Entry<String, Object> entry : contextData.entrySet()) {
                if (!first) {
                    sb.append(", ");
                }
                sb.append(entry.getKey()).append("=").append(entry.getValue());
                first = false;
            }
            sb.append("]");
        }
        
        return sb.toString();
    }
    
    /**
     * デバッグ用の詳細情報を生成
     * @return デバッグ情報
     */
    public String createDebugInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== LZH エラーコンテキスト ===\n");
        sb.append("タイムスタンプ: ").append(new java.util.Date(timestamp)).append("\n");
        sb.append("操作: ").append(operation != null ? operation : "不明").append("\n");
        
        if (fileName != null) {
            sb.append("ファイル名: ").append(fileName).append("\n");
        }
        
        if (compressionMethod != null) {
            sb.append("圧縮方式: ").append(compressionMethod).append("\n");
        }
        
        if (inputSource != null) {
            sb.append("入力ソース: ").append(inputSource).append("\n");
        }
        
        if (outputPath != null) {
            sb.append("出力パス: ").append(outputPath).append("\n");
        }
        
        if (fileSize != null) {
            sb.append("ファイルサイズ: ").append(formatBytes(fileSize)).append("\n");
        }
        
        if (processedBytes != null) {
            sb.append("処理済みバイト: ").append(formatBytes(processedBytes)).append("\n");
            if (fileSize != null && fileSize > 0) {
                double progress = (double) processedBytes / fileSize * 100;
                sb.append("進捗: ").append(String.format("%.2f", progress)).append("%\n");
            }
        }
        
        if (!contextData.isEmpty()) {
            sb.append("追加データ:\n");
            for (Map.Entry<String, Object> entry : contextData.entrySet()) {
                sb.append("  ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
        }
        
        sb.append("========================");
        return sb.toString();
    }
    
    /**
     * バイト数を人間が読みやすい形式にフォーマット
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
    
    /**
     * エラーコンテキストのコピーを作成
     * @return コピーされたエラーコンテキスト
     */
    public ErrorContext copy() {
        ErrorContext copy = new ErrorContext(this.operation);
        copy.fileName = this.fileName;
        copy.compressionMethod = this.compressionMethod;
        copy.fileSize = this.fileSize;
        copy.processedBytes = this.processedBytes;
        copy.inputSource = this.inputSource;
        copy.outputPath = this.outputPath;
        copy.contextData.putAll(this.contextData);
        return copy;
    }
    
    @Override
    public String toString() {
        return createDetailedMessage("ErrorContext");
    }
}