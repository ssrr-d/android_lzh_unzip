package com.example.lzh.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import com.example.lzh.exception.LzhException;
import com.example.lzh.exception.CorruptedArchiveException;
import com.example.lzh.exception.InvalidArchiveException;

/**
 * エラー回復とリトライ機能を提供するユーティリティクラス
 * 包括的なエラーハンドリングの一部として、回復可能なエラーに対する対処を行う
 * 要件 7.1, 7.2, 7.3, 7.4 に対応
 */
public class ErrorRecovery {
    
    private static final String TAG = "ErrorRecovery";
    
    /** デフォルトのリトライ回数 */
    public static final int DEFAULT_RETRY_COUNT = 3;
    
    /** デフォルトのリトライ間隔（ミリ秒） */
    public static final long DEFAULT_RETRY_DELAY_MS = 1000;
    
    /** 最大リトライ間隔（ミリ秒） */
    public static final long MAX_RETRY_DELAY_MS = 10000;
    
    /**
     * リトライ可能な操作を表すインターフェース
     */
    @FunctionalInterface
    public interface RetryableOperation<T> {
        T execute() throws Exception;
    }
    
    /**
     * リトライ設定
     */
    public static class RetryConfig {
        private int maxRetries = DEFAULT_RETRY_COUNT;
        private long initialDelayMs = DEFAULT_RETRY_DELAY_MS;
        private double backoffMultiplier = 2.0;
        private long maxDelayMs = MAX_RETRY_DELAY_MS;
        private boolean exponentialBackoff = true;
        
        public RetryConfig maxRetries(int maxRetries) {
            this.maxRetries = Math.max(0, maxRetries);
            return this;
        }
        
        public RetryConfig initialDelay(long delayMs) {
            this.initialDelayMs = Math.max(0, delayMs);
            return this;
        }
        
        public RetryConfig backoffMultiplier(double multiplier) {
            this.backoffMultiplier = Math.max(1.0, multiplier);
            return this;
        }
        
        public RetryConfig maxDelay(long maxDelayMs) {
            this.maxDelayMs = Math.max(initialDelayMs, maxDelayMs);
            return this;
        }
        
        public RetryConfig exponentialBackoff(boolean enabled) {
            this.exponentialBackoff = enabled;
            return this;
        }
        
        public int getMaxRetries() { return maxRetries; }
        public long getInitialDelayMs() { return initialDelayMs; }
        public double getBackoffMultiplier() { return backoffMultiplier; }
        public long getMaxDelayMs() { return maxDelayMs; }
        public boolean isExponentialBackoff() { return exponentialBackoff; }
    }
    
    /**
     * リトライ結果
     */
    public static class RetryResult<T> {
        private final T result;
        private final int attemptCount;
        private final List<Exception> failures;
        private final boolean succeeded;
        
        public RetryResult(T result, int attemptCount, List<Exception> failures) {
            this.result = result;
            this.attemptCount = attemptCount;
            this.failures = new ArrayList<>(failures);
            this.succeeded = result != null || failures.isEmpty();
        }
        
        public T getResult() { return result; }
        public int getAttemptCount() { return attemptCount; }
        public List<Exception> getFailures() { return new ArrayList<>(failures); }
        public boolean isSucceeded() { return succeeded; }
        public Exception getLastFailure() { 
            return failures.isEmpty() ? null : failures.get(failures.size() - 1);
        }
    }
    
    /**
     * 指定された操作をリトライ機能付きで実行
     * @param operation 実行する操作
     * @param config リトライ設定
     * @param context エラーコンテキスト
     * @return リトライ結果
     */
    public static <T> RetryResult<T> executeWithRetry(RetryableOperation<T> operation, 
                                                     RetryConfig config, 
                                                     ErrorContext context) {
        List<Exception> failures = new ArrayList<>();
        AtomicInteger attemptCount = new AtomicInteger(0);
        
        LzhLogger.d(TAG, "リトライ実行開始: 最大" + config.getMaxRetries() + "回");
        
        for (int attempt = 0; attempt <= config.getMaxRetries(); attempt++) {
            attemptCount.incrementAndGet();
            
            try {
                if (attempt > 0) {
                    long delay = calculateDelay(attempt - 1, config);
                    LzhLogger.d(TAG, "リトライ " + attempt + "/" + config.getMaxRetries() + 
                               " - " + delay + "ms待機後に実行");
                    
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        LzhLogger.w(TAG, "リトライ待機中に中断されました", e);
                        break;
                    }
                }
                
                T result = operation.execute();
                
                if (attempt > 0) {
                    LzhLogger.logRecoverableError(TAG, "リトライ操作", 
                        "前回の失敗から回復", "試行回数: " + attemptCount.get());
                }
                
                return new RetryResult<>(result, attemptCount.get(), failures);
                
            } catch (Exception e) {
                failures.add(e);
                
                LzhLogger.logExceptionDetails(TAG, "リトライ操作 (試行 " + attemptCount.get() + ")", e);
                
                // 回復不可能なエラーかチェック
                if (!isRetryableException(e)) {
                    LzhLogger.e(TAG, "回復不可能なエラーのためリトライを中止: " + e.getClass().getSimpleName());
                    break;
                }
                
                if (attempt < config.getMaxRetries()) {
                    LzhLogger.logRecoverableError(TAG, "リトライ操作", 
                        e.getClass().getSimpleName() + ": " + e.getMessage(), 
                        "次の試行を準備中");
                }
            }
        }
        
        LzhLogger.e(TAG, "すべてのリトライが失敗しました。試行回数: " + attemptCount.get());
        return new RetryResult<>(null, attemptCount.get(), failures);
    }
    
    /**
     * デフォルト設定でリトライ実行
     * @param operation 実行する操作
     * @param context エラーコンテキスト
     * @return リトライ結果
     */
    public static <T> RetryResult<T> executeWithRetry(RetryableOperation<T> operation, ErrorContext context) {
        return executeWithRetry(operation, new RetryConfig(), context);
    }
    
    /**
     * リトライ間隔を計算
     * @param attemptNumber 試行回数（0から開始）
     * @param config リトライ設定
     * @return 待機時間（ミリ秒）
     */
    private static long calculateDelay(int attemptNumber, RetryConfig config) {
        if (!config.isExponentialBackoff()) {
            return config.getInitialDelayMs();
        }
        
        long delay = (long) (config.getInitialDelayMs() * Math.pow(config.getBackoffMultiplier(), attemptNumber));
        return Math.min(delay, config.getMaxDelayMs());
    }
    
    /**
     * 例外がリトライ可能かどうかを判定
     * @param exception 例外
     * @return リトライ可能な場合true
     */
    public static boolean isRetryableException(Exception exception) {
        if (exception == null) {
            return false;
        }
        
        // I/O関連のエラーは一般的にリトライ可能
        if (exception instanceof IOException) {
            return true;
        }
        
        // LZH例外の場合は種類によって判定
        if (exception instanceof LzhException) {
            // 破損アーカイブや無効アーカイブはリトライしても意味がない
            if (exception instanceof CorruptedArchiveException || 
                exception instanceof InvalidArchiveException) {
                return false;
            }
            // その他のLZH例外はリトライ可能とする
            return true;
        }
        
        // ランタイム例外は基本的にリトライ不可
        if (exception instanceof RuntimeException) {
            // ただし、一部の例外はリトライ可能
            String message = exception.getMessage();
            if (message != null) {
                String lowerMessage = message.toLowerCase();
                if (lowerMessage.contains("timeout") || 
                    lowerMessage.contains("connection") ||
                    lowerMessage.contains("network")) {
                    return true;
                }
            }
            return false;
        }
        
        // その他の例外は基本的にリトライ可能
        return true;
    }
    
    /**
     * ファイル操作の回復を試行
     * @param file 対象ファイル
     * @param context エラーコンテキスト
     * @return 回復に成功した場合true
     */
    public static boolean attemptFileRecovery(File file, ErrorContext context) {
        if (file == null) {
            return false;
        }
        
        LzhLogger.d(TAG, "ファイル回復を試行: " + file.getAbsolutePath());
        
        try {
            // ファイルが存在しない場合は親ディレクトリの作成を試行
            if (!file.exists()) {
                File parentDir = file.getParentFile();
                if (parentDir != null && !parentDir.exists()) {
                    boolean created = parentDir.mkdirs();
                    if (created) {
                        LzhLogger.logRecoverableError(TAG, "ファイル回復", 
                            "親ディレクトリが存在しない", "ディレクトリを作成しました: " + parentDir.getAbsolutePath());
                        return true;
                    }
                }
            }
            
            // ファイルが読み取り専用の場合は書き込み可能にする試行
            if (file.exists() && !file.canWrite()) {
                boolean writable = file.setWritable(true);
                if (writable) {
                    LzhLogger.logRecoverableError(TAG, "ファイル回復", 
                        "ファイルが書き込み不可", "書き込み権限を設定しました: " + file.getAbsolutePath());
                    return true;
                }
            }
            
        } catch (Exception e) {
            LzhLogger.w(TAG, "ファイル回復中にエラーが発生", e);
        }
        
        return false;
    }
    
    /**
     * メモリ不足エラーからの回復を試行
     * @param context エラーコンテキスト
     * @return 回復アクションを実行した場合true
     */
    public static boolean attemptMemoryRecovery(ErrorContext context) {
        LzhLogger.w(TAG, "メモリ回復を試行");
        
        try {
            // ガベージコレクションを実行
            System.gc();
            
            // 少し待機してメモリが解放されるのを待つ
            Thread.sleep(100);
            
            // 利用可能メモリをチェック
            Runtime runtime = Runtime.getRuntime();
            long freeMemory = runtime.freeMemory();
            long totalMemory = runtime.totalMemory();
            long maxMemory = runtime.maxMemory();
            
            LzhLogger.logResourceUsage(TAG, "メモリ回復後", freeMemory, "bytes (free)");
            LzhLogger.logResourceUsage(TAG, "メモリ回復後", totalMemory, "bytes (total)");
            LzhLogger.logResourceUsage(TAG, "メモリ回復後", maxMemory, "bytes (max)");
            
            // 利用可能メモリが十分にある場合は回復成功とする
            double freePercentage = (double) freeMemory / totalMemory * 100;
            if (freePercentage > 10.0) { // 10%以上の空きメモリがある
                LzhLogger.logRecoverableError(TAG, "メモリ回復", 
                    "メモリ不足", "ガベージコレクション実行 - 空きメモリ: " + String.format("%.1f", freePercentage) + "%");
                return true;
            }
            
        } catch (Exception e) {
            LzhLogger.w(TAG, "メモリ回復中にエラーが発生", e);
        }
        
        return false;
    }
    
    /**
     * 一時的なリソース不足からの回復を試行
     * @param resourceType リソースタイプ
     * @param context エラーコンテキスト
     * @return 回復アクションを実行した場合true
     */
    public static boolean attemptResourceRecovery(String resourceType, ErrorContext context) {
        LzhLogger.d(TAG, "リソース回復を試行: " + resourceType);
        
        try {
            // リソースタイプに応じた回復処理
            switch (resourceType.toLowerCase()) {
                case "memory":
                case "メモリ":
                    return attemptMemoryRecovery(context);
                    
                case "disk":
                case "ディスク":
                case "storage":
                case "ストレージ":
                    // ディスク容量の問題は基本的に回復不可能だが、
                    // 一時ファイルのクリーンアップなどを試行できる
                    LzhLogger.w(TAG, "ディスク容量不足 - 手動でのクリーンアップが必要です");
                    return false;
                    
                case "file":
                case "ファイル":
                    // ファイル関連の問題は個別に処理
                    return false;
                    
                default:
                    LzhLogger.w(TAG, "未知のリソースタイプ: " + resourceType);
                    return false;
            }
            
        } catch (Exception e) {
            LzhLogger.w(TAG, "リソース回復中にエラーが発生", e);
        }
        
        return false;
    }
}