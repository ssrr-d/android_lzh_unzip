package com.example.lzh.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.example.lzh.exception.LzhException;
import com.example.lzh.exception.InvalidArchiveException;
import com.example.lzh.exception.CorruptedArchiveException;
import com.example.lzh.exception.UnsupportedMethodException;
import com.example.lzh.exception.EncodingException;
import com.example.lzh.exception.FileNotFoundException;

/**
 * エラーハンドリング機能の検証ユーティリティ
 * 包括的なエラーハンドリングの実装が正しく動作することを確認する
 * 要件 7.1, 7.2, 7.3, 7.4 に対応
 */
public class ErrorHandlingValidator {
    
    private static final String TAG = "ErrorHandlingValidator";
    
    /**
     * 検証結果
     */
    public static class ValidationResult {
        private final boolean success;
        private final String message;
        private final List<String> details;
        
        public ValidationResult(boolean success, String message) {
            this.success = success;
            this.message = message;
            this.details = new ArrayList<>();
        }
        
        public ValidationResult(boolean success, String message, List<String> details) {
            this.success = success;
            this.message = message;
            this.details = new ArrayList<>(details);
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public List<String> getDetails() { return new ArrayList<>(details); }
        
        public void addDetail(String detail) {
            details.add(detail);
        }
    }
    
    /**
     * 包括的なエラーハンドリング機能を検証
     * @return 検証結果
     */
    public static ValidationResult validateComprehensiveErrorHandling() {
        LzhLogger.i(TAG, "包括的エラーハンドリング検証開始");
        
        List<String> details = new ArrayList<>();
        boolean allTestsPassed = true;
        
        // 1. 例外クラス階層の検証
        try {
            ValidationResult hierarchyResult = validateExceptionHierarchy();
            details.add("例外階層検証: " + (hierarchyResult.isSuccess() ? "成功" : "失敗"));
            details.addAll(hierarchyResult.getDetails());
            if (!hierarchyResult.isSuccess()) {
                allTestsPassed = false;
            }
        } catch (Exception e) {
            details.add("例外階層検証中にエラー: " + e.getMessage());
            allTestsPassed = false;
        }
        
        // 2. エラーコンテキスト機能の検証
        try {
            ValidationResult contextResult = validateErrorContext();
            details.add("エラーコンテキスト検証: " + (contextResult.isSuccess() ? "成功" : "失敗"));
            details.addAll(contextResult.getDetails());
            if (!contextResult.isSuccess()) {
                allTestsPassed = false;
            }
        } catch (Exception e) {
            details.add("エラーコンテキスト検証中にエラー: " + e.getMessage());
            allTestsPassed = false;
        }
        
        // 3. ログ機能の検証
        try {
            ValidationResult logResult = validateLoggingFunctionality();
            details.add("ログ機能検証: " + (logResult.isSuccess() ? "成功" : "失敗"));
            details.addAll(logResult.getDetails());
            if (!logResult.isSuccess()) {
                allTestsPassed = false;
            }
        } catch (Exception e) {
            details.add("ログ機能検証中にエラー: " + e.getMessage());
            allTestsPassed = false;
        }
        
        // 4. エラー回復機能の検証
        try {
            ValidationResult recoveryResult = validateErrorRecovery();
            details.add("エラー回復機能検証: " + (recoveryResult.isSuccess() ? "成功" : "失敗"));
            details.addAll(recoveryResult.getDetails());
            if (!recoveryResult.isSuccess()) {
                allTestsPassed = false;
            }
        } catch (Exception e) {
            details.add("エラー回復機能検証中にエラー: " + e.getMessage());
            allTestsPassed = false;
        }
        
        // 5. 包括的エラーハンドラーの検証
        try {
            ValidationResult handlerResult = validateComprehensiveErrorHandler();
            details.add("包括的エラーハンドラー検証: " + (handlerResult.isSuccess() ? "成功" : "失敗"));
            details.addAll(handlerResult.getDetails());
            if (!handlerResult.isSuccess()) {
                allTestsPassed = false;
            }
        } catch (Exception e) {
            details.add("包括的エラーハンドラー検証中にエラー: " + e.getMessage());
            allTestsPassed = false;
        }
        
        String resultMessage = allTestsPassed ? 
            "包括的エラーハンドリング検証が正常に完了しました" : 
            "包括的エラーハンドリング検証で問題が検出されました";
        
        LzhLogger.i(TAG, resultMessage);
        return new ValidationResult(allTestsPassed, resultMessage, details);
    }
    
    /**
     * 例外クラス階層の検証
     * @return 検証結果
     */
    private static ValidationResult validateExceptionHierarchy() {
        List<String> details = new ArrayList<>();
        boolean success = true;
        
        try {
            // 基底例外クラスの検証
            LzhException baseException = new LzhException("Test message");
            if (baseException.getMessage() == null || baseException.getTimestamp() <= 0) {
                details.add("基底例外クラスの初期化に問題があります");
                success = false;
            } else {
                details.add("基底例外クラス: 正常");
            }
            
            // 各派生例外クラスの検証
            InvalidArchiveException invalidEx = new InvalidArchiveException("Test invalid");
            if (!"INVALID_ARCHIVE".equals(invalidEx.getErrorCode())) {
                details.add("InvalidArchiveExceptionのエラーコードが正しくありません");
                success = false;
            } else {
                details.add("InvalidArchiveException: 正常");
            }
            
            CorruptedArchiveException corruptedEx = new CorruptedArchiveException("Test corrupted");
            if (!"CORRUPTED_ARCHIVE".equals(corruptedEx.getErrorCode())) {
                details.add("CorruptedArchiveExceptionのエラーコードが正しくありません");
                success = false;
            } else {
                details.add("CorruptedArchiveException: 正常");
            }
            
            UnsupportedMethodException unsupportedEx = new UnsupportedMethodException("Test unsupported");
            if (!"UNSUPPORTED_METHOD".equals(unsupportedEx.getErrorCode())) {
                details.add("UnsupportedMethodExceptionのエラーコードが正しくありません");
                success = false;
            } else {
                details.add("UnsupportedMethodException: 正常");
            }
            
            EncodingException encodingEx = new EncodingException("Test encoding");
            if (!"ENCODING_ERROR".equals(encodingEx.getErrorCode())) {
                details.add("EncodingExceptionのエラーコードが正しくありません");
                success = false;
            } else {
                details.add("EncodingException: 正常");
            }
            
            FileNotFoundException fileNotFoundEx = new FileNotFoundException("Test file not found");
            if (!"FILE_NOT_FOUND".equals(fileNotFoundEx.getErrorCode())) {
                details.add("FileNotFoundExceptionのエラーコードが正しくありません");
                success = false;
            } else {
                details.add("FileNotFoundException: 正常");
            }
            
        } catch (Exception e) {
            details.add("例外クラス階層検証中にエラー: " + e.getMessage());
            success = false;
        }
        
        return new ValidationResult(success, "例外クラス階層検証完了", details);
    }
    
    /**
     * エラーコンテキスト機能の検証
     * @return 検証結果
     */
    private static ValidationResult validateErrorContext() {
        List<String> details = new ArrayList<>();
        boolean success = true;
        
        try {
            // エラーコンテキストの作成と設定
            ErrorContext context = new ErrorContext("テスト操作")
                .withFileName("test.lzh")
                .withCompressionMethod("-lh1-")
                .withFileSize(1024L)
                .withProcessedBytes(512L)
                .withInputSource("test input")
                .withOutputPath("test output")
                .withData("customKey", "customValue");
            
            // 基本プロパティの検証
            if (!"テスト操作".equals(context.getOperation())) {
                details.add("操作名の設定に問題があります");
                success = false;
            }
            
            if (!"test.lzh".equals(context.getFileName())) {
                details.add("ファイル名の設定に問題があります");
                success = false;
            }
            
            if (!"-lh1-".equals(context.getCompressionMethod())) {
                details.add("圧縮方式の設定に問題があります");
                success = false;
            }
            
            if (!Long.valueOf(1024L).equals(context.getFileSize())) {
                details.add("ファイルサイズの設定に問題があります");
                success = false;
            }
            
            // 詳細メッセージの生成テスト
            String detailedMessage = context.createDetailedMessage("テストメッセージ");
            if (detailedMessage == null || !detailedMessage.contains("テストメッセージ")) {
                details.add("詳細メッセージの生成に問題があります");
                success = false;
            }
            
            // デバッグ情報の生成テスト
            String debugInfo = context.createDebugInfo();
            if (debugInfo == null || !debugInfo.contains("テスト操作")) {
                details.add("デバッグ情報の生成に問題があります");
                success = false;
            }
            
            // コピー機能のテスト
            ErrorContext copy = context.copy();
            if (!context.getOperation().equals(copy.getOperation())) {
                details.add("エラーコンテキストのコピー機能に問題があります");
                success = false;
            }
            
            if (success) {
                details.add("エラーコンテキストの全機能が正常に動作しています");
            }
            
        } catch (Exception e) {
            details.add("エラーコンテキスト検証中にエラー: " + e.getMessage());
            success = false;
        }
        
        return new ValidationResult(success, "エラーコンテキスト検証完了", details);
    }
    
    /**
     * ログ機能の検証
     * @return 検証結果
     */
    private static ValidationResult validateLoggingFunctionality() {
        List<String> details = new ArrayList<>();
        boolean success = true;
        
        try {
            // 基本ログ機能のテスト
            LzhLogger.d(TAG, "デバッグログテスト");
            LzhLogger.i(TAG, "情報ログテスト");
            LzhLogger.w(TAG, "警告ログテスト");
            LzhLogger.e(TAG, "エラーログテスト");
            
            // 拡張ログ機能のテスト
            LzhLogger.logOperationStart(TAG, "テスト操作", "詳細情報");
            LzhLogger.logOperationSuccess(TAG, "テスト操作", "成功詳細");
            LzhLogger.logOperationFailure(TAG, "テスト操作", "失敗理由", new RuntimeException("テスト例外"));
            
            LzhLogger.logPerformance(TAG, "テスト操作", 1000L, "追加情報");
            LzhLogger.logSecurityWarning(TAG, "テストセキュリティ問題", "詳細");
            LzhLogger.logResourceUsage(TAG, "メモリ", 1024L, "bytes");
            
            // 新しいログ機能のテスト
            LzhLogger.logExceptionDetails(TAG, "テスト操作", new IOException("テストI/Oエラー"));
            LzhLogger.logRecoverableError(TAG, "テスト操作", "回復可能エラー", "回復アクション");
            LzhLogger.logDataIntegrityError(TAG, "テストデータ", "期待値", "実際値", "コンテキスト");
            LzhLogger.logProgress(TAG, "テスト操作", 50L, 100L, "items");
            LzhLogger.logSystemState(TAG, "テストコンポーネント", "正常", "詳細状態");
            
            details.add("全てのログ機能が正常に実行されました");
            
        } catch (Exception e) {
            details.add("ログ機能検証中にエラー: " + e.getMessage());
            success = false;
        }
        
        return new ValidationResult(success, "ログ機能検証完了", details);
    }
    
    /**
     * エラー回復機能の検証
     * @return 検証結果
     */
    private static ValidationResult validateErrorRecovery() {
        List<String> details = new ArrayList<>();
        boolean success = true;
        
        try {
            // リトライ可能性の判定テスト
            boolean ioRetryable = ErrorRecovery.isRetryableException(new IOException("テストI/Oエラー"));
            if (!ioRetryable) {
                details.add("I/O例外がリトライ可能と判定されませんでした");
                success = false;
            }
            
            boolean corruptedRetryable = ErrorRecovery.isRetryableException(new CorruptedArchiveException("テスト破損"));
            if (corruptedRetryable) {
                details.add("破損アーカイブ例外がリトライ不可能と判定されませんでした");
                success = false;
            }
            
            // リトライ設定のテスト
            ErrorRecovery.RetryConfig config = new ErrorRecovery.RetryConfig()
                .maxRetries(2)
                .initialDelay(100L)
                .exponentialBackoff(true);
            
            if (config.getMaxRetries() != 2) {
                details.add("リトライ設定の最大回数設定に問題があります");
                success = false;
            }
            
            // メモリ回復のテスト
            boolean memoryRecovery = ErrorRecovery.attemptMemoryRecovery(new ErrorContext("テストメモリ回復"));
            details.add("メモリ回復テスト実行: " + (memoryRecovery ? "成功" : "失敗"));
            
            if (success) {
                details.add("エラー回復機能の基本動作が正常です");
            }
            
        } catch (Exception e) {
            details.add("エラー回復機能検証中にエラー: " + e.getMessage());
            success = false;
        }
        
        return new ValidationResult(success, "エラー回復機能検証完了", details);
    }
    
    /**
     * 包括的エラーハンドラーの検証
     * @return 検証結果
     */
    private static ValidationResult validateComprehensiveErrorHandler() {
        List<String> details = new ArrayList<>();
        boolean success = true;
        
        try {
            ErrorContext context = new ErrorContext("テストハンドラー");
            
            // 各種例外の変換テスト
            IOException ioEx = new IOException("テストI/Oエラー");
            LzhException convertedIo = ComprehensiveErrorHandler.handleException(ioEx, "テスト操作", context);
            if (convertedIo == null || !"IO_ERROR".equals(convertedIo.getErrorCode())) {
                details.add("I/O例外の変換に問題があります");
                success = false;
            }
            
            OutOfMemoryError memoryError = new OutOfMemoryError("テストメモリ不足");
            LzhException convertedMemory = ComprehensiveErrorHandler.handleException(memoryError, "テスト操作", context);
            if (convertedMemory == null || !"MEMORY_ERROR".equals(convertedMemory.getErrorCode())) {
                details.add("メモリエラーの変換に問題があります");
                success = false;
            }
            
            SecurityException securityEx = new SecurityException("テストセキュリティエラー");
            LzhException convertedSecurity = ComprehensiveErrorHandler.handleException(securityEx, "テスト操作", context);
            if (convertedSecurity == null || !"SECURITY_ERROR".equals(convertedSecurity.getErrorCode())) {
                details.add("セキュリティ例外の変換に問題があります");
                success = false;
            }
            
            // 回復可能性判定のテスト
            boolean ioRecoverable = ComprehensiveErrorHandler.isRecoverableError(ioEx);
            if (!ioRecoverable) {
                details.add("I/O例外の回復可能性判定に問題があります");
                success = false;
            }
            
            boolean securityRecoverable = ComprehensiveErrorHandler.isRecoverableError(securityEx);
            if (securityRecoverable) {
                details.add("セキュリティ例外の回復可能性判定に問題があります");
                success = false;
            }
            
            // 重要度判定のテスト
            int ioSeverity = ComprehensiveErrorHandler.getErrorSeverity(ioEx);
            if (ioSeverity != 2) { // 中重要度
                details.add("I/O例外の重要度判定に問題があります");
                success = false;
            }
            
            int memorySeverity = ComprehensiveErrorHandler.getErrorSeverity(memoryError);
            if (memorySeverity != 4) { // 致命的
                details.add("メモリエラーの重要度判定に問題があります");
                success = false;
            }
            
            // 推奨アクション取得のテスト
            String ioAction = ComprehensiveErrorHandler.getRecommendedAction(ioEx);
            if (ioAction == null || ioAction.isEmpty()) {
                details.add("I/O例外の推奨アクション取得に問題があります");
                success = false;
            }
            
            // エラー統計のテスト
            ComprehensiveErrorHandler.clearErrorStatistics();
            ComprehensiveErrorHandler.handleException(ioEx, "テスト", context);
            ComprehensiveErrorHandler.handleException(memoryError, "テスト", context);
            
            var stats = ComprehensiveErrorHandler.getErrorStatistics();
            if (stats.isEmpty()) {
                details.add("エラー統計の記録に問題があります");
                success = false;
            }
            
            if (success) {
                details.add("包括的エラーハンドラーの全機能が正常に動作しています");
            }
            
        } catch (Exception e) {
            details.add("包括的エラーハンドラー検証中にエラー: " + e.getMessage());
            success = false;
        }
        
        return new ValidationResult(success, "包括的エラーハンドラー検証完了", details);
    }
    
    /**
     * 検証結果をログに出力
     * @param result 検証結果
     */
    public static void logValidationResult(ValidationResult result) {
        if (result.isSuccess()) {
            LzhLogger.i(TAG, "✓ " + result.getMessage());
        } else {
            LzhLogger.e(TAG, "✗ " + result.getMessage());
        }
        
        for (String detail : result.getDetails()) {
            LzhLogger.i(TAG, "  - " + detail);
        }
    }
}