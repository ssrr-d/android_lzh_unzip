package com.example.lzh.util;

import android.content.Context;
import android.os.Build;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Android互換性検証ユーティリティ
 * API 21以上での動作確認とバグ修正のための検証機能を提供
 * 要件 8.1, 8.2, 8.3, 8.4 に対応
 */
public class AndroidCompatibilityVerifier {
    
    private static final String TAG = "AndroidCompatibilityVerifier";
    
    /**
     * 互換性検証結果
     */
    public static class VerificationResult {
        private final boolean isCompatible;
        private final List<String> issues;
        private final List<String> warnings;
        
        public VerificationResult(boolean isCompatible, List<String> issues, List<String> warnings) {
            this.isCompatible = isCompatible;
            this.issues = new ArrayList<>(issues);
            this.warnings = new ArrayList<>(warnings);
        }
        
        public boolean isCompatible() { return isCompatible; }
        public List<String> getIssues() { return new ArrayList<>(issues); }
        public List<String> getWarnings() { return new ArrayList<>(warnings); }
        
        public boolean hasIssues() { return !issues.isEmpty(); }
        public boolean hasWarnings() { return !warnings.isEmpty(); }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("VerificationResult{compatible=").append(isCompatible);
            if (hasIssues()) {
                sb.append(", issues=").append(issues.size());
            }
            if (hasWarnings()) {
                sb.append(", warnings=").append(warnings.size());
            }
            sb.append("}");
            return sb.toString();
        }
    }
    
    /**
     * 包括的なAndroid互換性検証を実行
     * @param context Androidコンテキスト
     * @return 検証結果
     */
    public static VerificationResult verifyCompatibility(Context context) {
        List<String> issues = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        // APIレベル検証
        verifyApiLevel(issues, warnings);
        
        // ストレージ検証
        verifyStorage(context, issues, warnings);
        
        // メモリ検証
        verifyMemory(issues, warnings);
        
        // Java機能検証
        verifyJavaFeatures(issues, warnings);
        
        // Android固有機能検証
        verifyAndroidFeatures(context, issues, warnings);
        
        boolean isCompatible = issues.isEmpty();
        
        // 結果をログに出力
        logVerificationResult(isCompatible, issues, warnings);
        
        return new VerificationResult(isCompatible, issues, warnings);
    }
    
    /**
     * APIレベルの検証
     */
    private static void verifyApiLevel(List<String> issues, List<String> warnings) {
        int currentApi = Build.VERSION.SDK_INT;
        
        if (currentApi < AndroidCompatibility.MIN_API_LEVEL) {
            issues.add("サポートされていないAPIレベル: " + currentApi + " (最小要求: " + AndroidCompatibility.MIN_API_LEVEL + ")");
        } else if (currentApi == AndroidCompatibility.MIN_API_LEVEL) {
            warnings.add("最小APIレベルで動作中: " + currentApi);
        }
        
        // 特定のAPIレベルでの既知の問題をチェック
        if (currentApi >= 21 && currentApi <= 23) {
            warnings.add("Android 5.x-6.0での動作: メモリ管理に注意が必要");
        }
        
        if (currentApi >= 29) {
            warnings.add("Android 10以上: スコープストレージの影響を確認");
        }
    }
    
    /**
     * ストレージの検証
     */
    private static void verifyStorage(Context context, List<String> issues, List<String> warnings) {
        if (context == null) {
            issues.add("コンテキストがnull");
            return;
        }
        
        try {
            File filesDir = context.getFilesDir();
            if (filesDir == null) {
                issues.add("内部ストレージディレクトリを取得できません");
                return;
            }
            
            if (!filesDir.exists()) {
                issues.add("内部ストレージディレクトリが存在しません");
                return;
            }
            
            if (!filesDir.canWrite()) {
                issues.add("内部ストレージに書き込み権限がありません");
                return;
            }
            
            // 容量チェック
            long freeSpace = filesDir.getFreeSpace();
            long totalSpace = filesDir.getTotalSpace();
            
            if (freeSpace < 10 * 1024 * 1024) { // 10MB未満
                warnings.add("利用可能ストレージ容量が少ない: " + formatBytes(freeSpace));
            }
            
            double usagePercent = (double)(totalSpace - freeSpace) / totalSpace * 100;
            if (usagePercent > 90) {
                warnings.add("ストレージ使用率が高い: " + String.format("%.1f%%", usagePercent));
            }
            
        } catch (Exception e) {
            issues.add("ストレージ検証中にエラー: " + e.getMessage());
        }
    }
    
    /**
     * メモリの検証
     */
    private static void verifyMemory(List<String> issues, List<String> warnings) {
        try {
            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            
            // 最大メモリが少なすぎる場合
            if (maxMemory < 64 * 1024 * 1024) { // 64MB未満
                warnings.add("利用可能最大メモリが少ない: " + formatBytes(maxMemory));
            }
            
            // メモリ使用率が高い場合
            double usagePercent = (double)usedMemory / totalMemory * 100;
            if (usagePercent > 80) {
                warnings.add("メモリ使用率が高い: " + String.format("%.1f%%", usagePercent));
            }
            
            // OutOfMemoryErrorのリスクチェック
            long availableMemory = maxMemory - usedMemory;
            if (availableMemory < 16 * 1024 * 1024) { // 16MB未満
                warnings.add("利用可能メモリが少ない: " + formatBytes(availableMemory));
            }
            
        } catch (Exception e) {
            issues.add("メモリ検証中にエラー: " + e.getMessage());
        }
    }
    
    /**
     * Java機能の検証
     */
    private static void verifyJavaFeatures(List<String> issues, List<String> warnings) {
        try {
            // Java 8機能のテスト
            testLambdaSupport(issues, warnings);
            testStreamSupport(issues, warnings);
            testOptionalSupport(issues, warnings);
            
        } catch (Exception e) {
            issues.add("Java機能検証中にエラー: " + e.getMessage());
        }
    }
    
    /**
     * Lambda式のサポート確認
     */
    private static void testLambdaSupport(List<String> issues, List<String> warnings) {
        try {
            Runnable lambda = () -> {
                // Lambda式のテスト
            };
            lambda.run();
        } catch (Exception e) {
            issues.add("Lambda式がサポートされていません: " + e.getMessage());
        }
    }
    
    /**
     * Stream APIのサポート確認
     */
    private static void testStreamSupport(List<String> issues, List<String> warnings) {
        try {
            // Stream APIは Android API 24以上またはdesugaringが必要
            if (Build.VERSION.SDK_INT < 24) {
                warnings.add("Stream API使用時はdesugaringが必要 (API " + Build.VERSION.SDK_INT + ")");
            }
        } catch (Exception e) {
            warnings.add("Stream API検証中にエラー: " + e.getMessage());
        }
    }
    
    /**
     * Optional APIのサポート確認
     */
    private static void testOptionalSupport(List<String> issues, List<String> warnings) {
        try {
            // Optional APIは Android API 24以上またはdesugaringが必要
            if (Build.VERSION.SDK_INT < 24) {
                warnings.add("Optional API使用時はdesugaringが必要 (API " + Build.VERSION.SDK_INT + ")");
            }
        } catch (Exception e) {
            warnings.add("Optional API検証中にエラー: " + e.getMessage());
        }
    }
    
    /**
     * Android固有機能の検証
     */
    private static void verifyAndroidFeatures(Context context, List<String> issues, List<String> warnings) {
        try {
            // パッケージマネージャーの確認
            if (context.getPackageManager() == null) {
                issues.add("PackageManagerを取得できません");
            }
            
            // アプリケーション情報の確認
            if (context.getApplicationInfo() == null) {
                issues.add("ApplicationInfoを取得できません");
            }
            
            // リソースの確認
            if (context.getResources() == null) {
                issues.add("Resourcesを取得できません");
            }
            
        } catch (Exception e) {
            issues.add("Android機能検証中にエラー: " + e.getMessage());
        }
    }
    
    /**
     * 検証結果をログに出力
     */
    private static void logVerificationResult(boolean isCompatible, List<String> issues, List<String> warnings) {
        LzhLogger.i(TAG, "=== Android互換性検証結果 ===");
        LzhLogger.i(TAG, "互換性: " + (isCompatible ? "OK" : "NG"));
        
        if (!issues.isEmpty()) {
            LzhLogger.e(TAG, "問題 (" + issues.size() + "件):");
            for (int i = 0; i < issues.size(); i++) {
                LzhLogger.e(TAG, "  " + (i + 1) + ". " + issues.get(i));
            }
        }
        
        if (!warnings.isEmpty()) {
            LzhLogger.w(TAG, "警告 (" + warnings.size() + "件):");
            for (int i = 0; i < warnings.size(); i++) {
                LzhLogger.w(TAG, "  " + (i + 1) + ". " + warnings.get(i));
            }
        }
        
        if (isCompatible && warnings.isEmpty()) {
            LzhLogger.i(TAG, "すべての検証項目をクリアしました");
        }
        
        LzhLogger.i(TAG, "========================");
    }
    
    /**
     * バイト数を人間が読みやすい形式にフォーマット
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
     * 簡易互換性チェック（例外なし）
     * @param context Androidコンテキスト
     * @return 互換性がある場合true
     */
    public static boolean isCompatible(Context context) {
        try {
            VerificationResult result = verifyCompatibility(context);
            return result.isCompatible();
        } catch (Exception e) {
            LzhLogger.e(TAG, "互換性チェック中にエラーが発生", e);
            return false;
        }
    }
    
    /**
     * 診断情報の出力
     * @param context Androidコンテキスト
     */
    public static void logDiagnosticInfo(Context context) {
        LzhLogger.i(TAG, "=== Android診断情報 ===");
        
        // 基本情報
        LzhLogger.i(TAG, "デバイス: " + Build.MANUFACTURER + " " + Build.MODEL);
        LzhLogger.i(TAG, "Android: " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")");
        LzhLogger.i(TAG, "ABI: " + Build.SUPPORTED_ABIS[0]);
        
        // アプリ情報
        if (context != null) {
            LzhLogger.i(TAG, "パッケージ: " + context.getPackageName());
            LzhLogger.i(TAG, "データディレクトリ: " + context.getFilesDir());
        }
        
        // メモリ情報
        AndroidCompatibility.MemoryInfo memInfo = AndroidCompatibility.getMemoryInfo();
        LzhLogger.i(TAG, "メモリ: " + memInfo);
        
        // ストレージ情報
        if (context != null) {
            AndroidCompatibility.StorageInfo storageInfo = AndroidCompatibility.getInternalStorageInfo(context);
            if (storageInfo != null) {
                LzhLogger.i(TAG, "ストレージ: " + storageInfo);
            }
        }
        
        LzhLogger.i(TAG, "==================");
    }
}