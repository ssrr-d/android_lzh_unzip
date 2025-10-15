package com.example.lzh.util;

import android.content.Context;
import android.os.Build;
import java.io.File;

/**
 * Android互換性ユーティリティクラス
 * API 21以上での動作確認とAndroid固有の機能を提供
 * 要件 8.1, 8.2, 8.3, 8.4 に対応
 */
public class AndroidCompatibility {
    
    private static final String TAG = "AndroidCompatibility";
    
    // サポートする最小APIレベル
    public static final int MIN_API_LEVEL = 21; // Android 5.0 (Lollipop)
    
    /**
     * 現在のAndroidバージョンがサポートされているかチェック
     * @return サポートされている場合true
     */
    public static boolean isApiLevelSupported() {
        return Build.VERSION.SDK_INT >= MIN_API_LEVEL;
    }
    
    /**
     * 現在のAPIレベルを取得
     * @return 現在のAPIレベル
     */
    public static int getCurrentApiLevel() {
        return Build.VERSION.SDK_INT;
    }
    
    /**
     * Android互換性情報をログに出力
     */
    public static void logCompatibilityInfo() {
        LzhLogger.i(TAG, "Android互換性情報:");
        LzhLogger.i(TAG, "  現在のAPIレベル: " + getCurrentApiLevel());
        LzhLogger.i(TAG, "  最小サポートAPIレベル: " + MIN_API_LEVEL);
        LzhLogger.i(TAG, "  サポート状況: " + (isApiLevelSupported() ? "サポート済み" : "未サポート"));
        LzhLogger.i(TAG, "  Androidバージョン: " + Build.VERSION.RELEASE);
        LzhLogger.i(TAG, "  デバイス情報: " + Build.MANUFACTURER + " " + Build.MODEL);
    }
    
    /**
     * 内部ストレージの利用可能性をチェック
     * @param context Androidコンテキスト
     * @return 利用可能な場合true
     */
    public static boolean isInternalStorageAvailable(Context context) {
        if (context == null) {
            LzhLogger.w(TAG, "コンテキストがnullです");
            return false;
        }
        
        try {
            File filesDir = context.getFilesDir();
            if (filesDir == null) {
                LzhLogger.w(TAG, "内部ストレージディレクトリを取得できません");
                return false;
            }
            
            if (!filesDir.exists()) {
                LzhLogger.w(TAG, "内部ストレージディレクトリが存在しません");
                return false;
            }
            
            if (!filesDir.canWrite()) {
                LzhLogger.w(TAG, "内部ストレージディレクトリに書き込み権限がありません");
                return false;
            }
            
            return true;
        } catch (Exception e) {
            LzhLogger.e(TAG, "内部ストレージ可用性チェック中にエラーが発生", e);
            return false;
        }
    }
    
    /**
     * 内部ストレージの容量情報を取得
     * @param context Androidコンテキスト
     * @return 容量情報、取得できない場合はnull
     */
    public static StorageInfo getInternalStorageInfo(Context context) {
        if (context == null) {
            return null;
        }
        
        try {
            File filesDir = context.getFilesDir();
            if (filesDir == null) {
                return null;
            }
            
            long totalSpace = filesDir.getTotalSpace();
            long freeSpace = filesDir.getFreeSpace();
            long usableSpace = filesDir.getUsableSpace();
            
            return new StorageInfo(totalSpace, freeSpace, usableSpace);
        } catch (Exception e) {
            LzhLogger.e(TAG, "ストレージ情報取得中にエラーが発生", e);
            return null;
        }
    }
    
    /**
     * ストレージ情報を保持するクラス
     */
    public static class StorageInfo {
        private final long totalSpace;
        private final long freeSpace;
        private final long usableSpace;
        
        public StorageInfo(long totalSpace, long freeSpace, long usableSpace) {
            this.totalSpace = totalSpace;
            this.freeSpace = freeSpace;
            this.usableSpace = usableSpace;
        }
        
        public long getTotalSpace() { return totalSpace; }
        public long getFreeSpace() { return freeSpace; }
        public long getUsableSpace() { return usableSpace; }
        
        public String getFormattedTotalSpace() { return formatBytes(totalSpace); }
        public String getFormattedFreeSpace() { return formatBytes(freeSpace); }
        public String getFormattedUsableSpace() { return formatBytes(usableSpace); }
        
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
            return String.format("StorageInfo{total=%s, free=%s, usable=%s}", 
                               getFormattedTotalSpace(), getFormattedFreeSpace(), getFormattedUsableSpace());
        }
    }
    
    /**
     * メモリ使用量をチェック
     * @return メモリ情報
     */
    public static MemoryInfo getMemoryInfo() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        return new MemoryInfo(maxMemory, totalMemory, usedMemory, freeMemory);
    }
    
    /**
     * メモリ情報を保持するクラス
     */
    public static class MemoryInfo {
        private final long maxMemory;
        private final long totalMemory;
        private final long usedMemory;
        private final long freeMemory;
        
        public MemoryInfo(long maxMemory, long totalMemory, long usedMemory, long freeMemory) {
            this.maxMemory = maxMemory;
            this.totalMemory = totalMemory;
            this.usedMemory = usedMemory;
            this.freeMemory = freeMemory;
        }
        
        public long getMaxMemory() { return maxMemory; }
        public long getTotalMemory() { return totalMemory; }
        public long getUsedMemory() { return usedMemory; }
        public long getFreeMemory() { return freeMemory; }
        
        public double getUsagePercentage() {
            return (double) usedMemory / totalMemory * 100.0;
        }
        
        public boolean isMemoryLow() {
            return getUsagePercentage() > 80.0; // 80%以上使用している場合
        }
        
        private String formatBytes(long bytes) {
            if (bytes < 1024 * 1024) {
                return String.format("%.1f KB", bytes / 1024.0);
            } else if (bytes < 1024 * 1024 * 1024) {
                return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
            } else {
                return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
            }
        }
        
        @Override
        public String toString() {
            return String.format("MemoryInfo{max=%s, total=%s, used=%s (%.1f%%), free=%s}", 
                               formatBytes(maxMemory), formatBytes(totalMemory), 
                               formatBytes(usedMemory), getUsagePercentage(), formatBytes(freeMemory));
        }
    }
    
    /**
     * Android固有の制限をチェック
     * @param context Androidコンテキスト
     * @param estimatedDataSize 処理予定のデータサイズ
     * @return 制限に問題がない場合true
     */
    public static boolean checkAndroidLimitations(Context context, long estimatedDataSize) {
        boolean allChecksPass = true;
        
        // APIレベルチェック
        if (!isApiLevelSupported()) {
            LzhLogger.e(TAG, "サポートされていないAPIレベル: " + getCurrentApiLevel());
            allChecksPass = false;
        }
        
        // 内部ストレージチェック
        if (!isInternalStorageAvailable(context)) {
            LzhLogger.e(TAG, "内部ストレージが利用できません");
            allChecksPass = false;
        }
        
        // ストレージ容量チェック
        StorageInfo storageInfo = getInternalStorageInfo(context);
        if (storageInfo != null) {
            if (storageInfo.getUsableSpace() < estimatedDataSize) {
                LzhLogger.w(TAG, String.format("ストレージ容量不足: 必要=%s, 利用可能=%s", 
                                              formatBytes(estimatedDataSize), storageInfo.getFormattedUsableSpace()));
                allChecksPass = false;
            }
        }
        
        // メモリチェック
        MemoryInfo memoryInfo = getMemoryInfo();
        if (memoryInfo.isMemoryLow()) {
            LzhLogger.w(TAG, "メモリ使用量が高い: " + memoryInfo.getUsagePercentage() + "%");
            // メモリ不足は警告のみで処理は継続
        }
        
        return allChecksPass;
    }
    
    /**
     * バイト数を人間が読みやすい形式にフォーマット
     * @param bytes バイト数
     * @return フォーマットされた文字列
     */
    private static String formatBytes(long bytes) {
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
     * 診断情報をログに出力
     * @param context Androidコンテキスト
     */
    public static void logDiagnosticInfo(Context context) {
        LzhLogger.i(TAG, "=== Android診断情報 ===");
        
        // 基本情報
        logCompatibilityInfo();
        
        // ストレージ情報
        StorageInfo storageInfo = getInternalStorageInfo(context);
        if (storageInfo != null) {
            LzhLogger.i(TAG, "内部ストレージ情報: " + storageInfo);
        }
        
        // メモリ情報
        MemoryInfo memoryInfo = getMemoryInfo();
        LzhLogger.i(TAG, "メモリ情報: " + memoryInfo);
        
        LzhLogger.i(TAG, "=====================");
    }
}