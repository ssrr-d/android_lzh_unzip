package com.example.lzh.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import com.example.lzh.exception.LzhException;
import com.example.lzh.exception.InvalidArchiveException;
import com.example.lzh.exception.CorruptedArchiveException;
import com.example.lzh.exception.UnsupportedMethodException;
import com.example.lzh.exception.EncodingException;
import com.example.lzh.exception.FileNotFoundException;

/**
 * 包括的なエラーハンドリング統合クラス
 * 全ての例外ケースに対する適切な処理と詳細なエラーメッセージ、ログ出力機能を提供
 * 要件 7.1, 7.2, 7.3, 7.4 に対応
 */
public class ComprehensiveErrorHandler {
    
    private static final String TAG = "ComprehensiveErrorHandler";
    
    /** エラー統計情報 */
    private static final Map<String, AtomicLong> errorStatistics = new ConcurrentHashMap<>();
    
    /** 最近のエラー履歴（最大100件） */
    private static final List<ErrorRecord> recentErrors = new ArrayList<>();
    private static final int MAX_ERROR_HISTORY = 100;
    
    /** エラー記録 */
    public static class ErrorRecord {
        private final long timestamp;
        private final String errorType;
        private final String operation;
        private final String message;
        private final ErrorContext context;
        
        public ErrorRecord(String errorType, String operation, String message, ErrorContext context) {
            this.timestamp = System.currentTimeMillis();
            this.errorType = errorType;
            this.operation = operation;
            this.message = message;
            this.context = context;
        }
        
        public long getTimestamp() { return timestamp; }
        public String getErrorType() { return errorType; }
        public String getOperation() { return operation; }
        public String getMessage() { return message; }
        public ErrorContext getContext() { return context; }
    }
    
    /**
     * 例外を包括的に処理し、適切なLZH例外に変換
     * @param exception 元の例外
     * @param operation 実行中の操作
     * @param context エラーコンテキスト
     * @return 適切に処理されたLZH例外
     */
    public static LzhException handleException(Throwable exception, String operation, ErrorContext context) {
        if (exception == null) {
            return new LzhException("Unknown error occurred", context);
        }
        
        // 統計情報を更新
        incrementErrorStatistics(exception.getClass().getSimpleName());
        
        // エラー履歴に記録
        recordError(exception.getClass().getSimpleName(), operation, exception.getMessage(), context);
        
        // 詳細ログを出力
        LzhLogger.logExceptionDetails(TAG, operation, exception);
        
        // 既にLZH例外の場合はそのまま返す
        if (exception instanceof LzhException) {
            LzhException lzhEx = (LzhException) exception;
            if (lzhEx.getErrorContext() == null && context != null) {
                lzhEx.setErrorContext(context);
            }
            return lzhEx;
        }
        
        // 例外タイプに応じて適切なLZH例外に変換
        return convertToLzhException(exception, operation, context);
    }
    
    /**
     * 例外を適切なLZH例外に変換
     * @param exception 元の例外
     * @param operation 操作名
     * @param context エラーコンテキスト
     * @return 変換されたLZH例外
     */
    private static LzhException convertToLzhException(Throwable exception, String operation, ErrorContext context) {
        String exceptionType = exception.getClass().getSimpleName();
        String message = exception.getMessage();
        
        // IOException系の処理
        if (exception instanceof IOException) {
            if (message != null) {
                String lowerMessage = message.toLowerCase();
                
                // ファイル関連エラー
                if (lowerMessage.contains("file not found") || lowerMessage.contains("no such file")) {
                    return new FileNotFoundException("File not found: " + message, context, exception);
                }
                
                // 権限エラー
                if (lowerMessage.contains("permission") || lowerMessage.contains("access denied")) {
                    return new LzhException("Permission denied: " + message, "PERMISSION_ERROR", exception);
                }
                
                // ディスク容量エラー
                if (lowerMessage.contains("no space") || lowerMessage.contains("disk full")) {
                    return new LzhException("Insufficient disk space: " + message, "DISK_FULL", exception);
                }
                
                // ネットワーク関連エラー
                if (lowerMessage.contains("connection") || lowerMessage.contains("network")) {
                    return new LzhException("Network error: " + message, "NETWORK_ERROR", exception);
                }
                
                // データ破損エラー
                if (lowerMessage.contains("corrupt") || lowerMessage.contains("invalid") || 
                    lowerMessage.contains("unexpected end")) {
                    return CorruptedArchiveException.forDataIntegrityError(message, context);
                }
            }
            
            // 一般的なI/Oエラー
            return new LzhException("I/O error during " + operation + ": " + message, "IO_ERROR", exception);
        }
        
        // メモリ関連エラー
        if (exception instanceof OutOfMemoryError) {
            LzhLogger.logResourceUsage(TAG, "メモリ不足発生時", 
                Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory(), "bytes");
            return new LzhException("Out of memory during " + operation, "MEMORY_ERROR", exception);
        }
        
        // セキュリティ関連エラー
        if (exception instanceof SecurityException) {
            LzhLogger.logSecurityWarning(TAG, "セキュリティ例外", operation + ": " + message);
            return new LzhException("Security error: " + message, "SECURITY_ERROR", exception);
        }
        
        // 引数エラー
        if (exception instanceof IllegalArgumentException) {
            return InvalidArchiveException.forInputValidation(message, context);
        }
        
        // 状態エラー
        if (exception instanceof IllegalStateException) {
            return new LzhException("Invalid state during " + operation + ": " + message, "STATE_ERROR", exception);
        }
        
        // 中断エラー
        if (exception instanceof InterruptedException) {
            Thread.currentThread().interrupt();
            return new LzhException("Operation interrupted: " + operation, "INTERRUPTED", exception);
        }
        
        // その他のランタイム例外
        if (exception instanceof RuntimeException) {
            return new LzhException("Runtime error during " + operation + ": " + message, "RUNTIME_ERROR", exception);
        }
        
        // その他の例外
        return new LzhException("Unexpected error during " + operation + ": " + message, "UNKNOWN_ERROR", exception);
    }
    
    /**
     * エラー統計情報を更新
     * @param errorType エラータイプ
     */
    private static void incrementErrorStatistics(String errorType) {
        errorStatistics.computeIfAbsent(errorType, k -> new AtomicLong(0)).incrementAndGet();
    }
    
    /**
     * エラーを履歴に記録
     * @param errorType エラータイプ
     * @param operation 操作名
     * @param message エラーメッセージ
     * @param context エラーコンテキスト
     */
    private static synchronized void recordError(String errorType, String operation, String message, ErrorContext context) {
        ErrorRecord record = new ErrorRecord(errorType, operation, message, context);
        recentErrors.add(record);
        
        // 履歴サイズを制限
        while (recentErrors.size() > MAX_ERROR_HISTORY) {
            recentErrors.remove(0);
        }
    }
    
    /**
     * エラー統計情報を取得
     * @return エラー統計マップ
     */
    public static Map<String, Long> getErrorStatistics() {
        Map<String, Long> stats = new HashMap<>();
        for (Map.Entry<String, AtomicLong> entry : errorStatistics.entrySet()) {
            stats.put(entry.getKey(), entry.getValue().get());
        }
        return stats;
    }
    
    /**
     * 最近のエラー履歴を取得
     * @return エラー履歴のコピー
     */
    public static synchronized List<ErrorRecord> getRecentErrors() {
        return new ArrayList<>(recentErrors);
    }
    
    /**
     * エラー統計をクリア
     */
    public static void clearErrorStatistics() {
        errorStatistics.clear();
        synchronized (ComprehensiveErrorHandler.class) {
            recentErrors.clear();
        }
    }
    
    /**
     * エラー統計情報をログに出力
     */
    public static void logErrorStatistics() {
        Map<String, Long> stats = getErrorStatistics();
        if (stats.isEmpty()) {
            LzhLogger.i(TAG, "エラー統計: エラーは発生していません");
            return;
        }
        
        LzhLogger.i(TAG, "=== エラー統計情報 ===");
        long totalErrors = 0;
        for (Map.Entry<String, Long> entry : stats.entrySet()) {
            long count = entry.getValue();
            totalErrors += count;
            LzhLogger.i(TAG, entry.getKey() + ": " + count + " 回");
        }
        LzhLogger.i(TAG, "総エラー数: " + totalErrors);
        
        // 最近のエラーも表示
        List<ErrorRecord> recent = getRecentErrors();
        if (!recent.isEmpty()) {
            LzhLogger.i(TAG, "=== 最近のエラー (最新" + Math.min(5, recent.size()) + "件) ===");
            for (int i = Math.max(0, recent.size() - 5); i < recent.size(); i++) {
                ErrorRecord record = recent.get(i);
                LzhLogger.i(TAG, String.format("[%s] %s - %s: %s", 
                    new java.util.Date(record.getTimestamp()).toString(),
                    record.getErrorType(),
                    record.getOperation(),
                    record.getMessage()));
            }
        }
    }
    
    /**
     * 回復可能なエラーかどうかを判定
     * @param exception 例外
     * @return 回復可能な場合true
     */
    public static boolean isRecoverableError(Throwable exception) {
        if (exception == null) {
            return false;
        }
        
        // LZH例外の場合
        if (exception instanceof LzhException) {
            // 破損アーカイブや無効アーカイブは回復不可能
            if (exception instanceof CorruptedArchiveException || 
                exception instanceof InvalidArchiveException) {
                return false;
            }
            
            // エンコーディングエラーは部分的に回復可能
            if (exception instanceof EncodingException) {
                return true;
            }
            
            // ファイル未発見は回復不可能
            if (exception instanceof FileNotFoundException) {
                return false;
            }
            
            // サポートされていないメソッドは回復不可能
            if (exception instanceof UnsupportedMethodException) {
                return false;
            }
            
            // その他のLZH例外は回復可能とする
            return true;
        }
        
        // I/O例外は一般的に回復可能
        if (exception instanceof IOException) {
            return true;
        }
        
        // メモリ不足は回復可能（ガベージコレクション等で）
        if (exception instanceof OutOfMemoryError) {
            return true;
        }
        
        // セキュリティ例外は回復不可能
        if (exception instanceof SecurityException) {
            return false;
        }
        
        // 引数エラーは回復不可能
        if (exception instanceof IllegalArgumentException) {
            return false;
        }
        
        // 中断は回復不可能
        if (exception instanceof InterruptedException) {
            return false;
        }
        
        // その他は基本的に回復可能とする
        return true;
    }
    
    /**
     * エラーの重要度を判定
     * @param exception 例外
     * @return 重要度（1=低, 2=中, 3=高, 4=致命的）
     */
    public static int getErrorSeverity(Throwable exception) {
        if (exception == null) {
            return 2; // 中程度
        }
        
        // 致命的エラー
        if (exception instanceof OutOfMemoryError || 
            exception instanceof SecurityException ||
            exception instanceof CorruptedArchiveException) {
            return 4;
        }
        
        // 高重要度エラー
        if (exception instanceof InvalidArchiveException ||
            exception instanceof UnsupportedMethodException ||
            exception instanceof IllegalArgumentException) {
            return 3;
        }
        
        // 中重要度エラー
        if (exception instanceof IOException ||
            exception instanceof EncodingException ||
            exception instanceof FileNotFoundException) {
            return 2;
        }
        
        // 低重要度エラー
        return 1;
    }
    
    /**
     * エラーに対する推奨アクションを取得
     * @param exception 例外
     * @return 推奨アクション
     */
    public static String getRecommendedAction(Throwable exception) {
        if (exception == null) {
            return "詳細な調査が必要です";
        }
        
        if (exception instanceof CorruptedArchiveException) {
            return "アーカイブファイルが破損しています。別のファイルを使用してください";
        }
        
        if (exception instanceof InvalidArchiveException) {
            return "有効なLZHアーカイブファイルを指定してください";
        }
        
        if (exception instanceof UnsupportedMethodException) {
            return "サポートされている圧縮方式（LH0, LH1, LH5）のアーカイブを使用してください";
        }
        
        if (exception instanceof FileNotFoundException) {
            return "指定されたファイルがアーカイブ内に存在するか確認してください";
        }
        
        if (exception instanceof EncodingException) {
            return "ファイル名の文字エンコーディングに問題があります。別のエンコーディングを試してください";
        }
        
        if (exception instanceof OutOfMemoryError) {
            return "メモリ不足です。より小さなファイルを処理するか、利用可能メモリを増やしてください";
        }
        
        if (exception instanceof SecurityException) {
            return "ファイルアクセス権限を確認してください";
        }
        
        if (exception instanceof IOException) {
            String message = exception.getMessage();
            if (message != null) {
                String lower = message.toLowerCase();
                if (lower.contains("permission") || lower.contains("access denied")) {
                    return "ファイルアクセス権限を確認してください";
                }
                if (lower.contains("no space") || lower.contains("disk full")) {
                    return "ディスク容量を確保してください";
                }
                if (lower.contains("file not found")) {
                    return "ファイルパスが正しいか確認してください";
                }
            }
            return "I/Oエラーが発生しました。ファイルシステムの状態を確認してください";
        }
        
        return "予期しないエラーが発生しました。ログを確認して詳細を調査してください";
    }
}