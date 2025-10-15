package com.example.lzh.util;

import android.util.Log;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * LZHライブラリ専用のログ管理ユーティリティ
 * Android環境とJava標準環境の両方に対応し、詳細なエラーメッセージとログ出力機能を提供
 * 要件 7.1, 7.2, 7.3, 7.4 に対応
 */
public class LzhLogger {
    
    private static final String TAG = "LzhLibrary";
    private static final boolean USE_ANDROID_LOG = isAndroidEnvironment();
    
    // ログレベル定数
    public static final int VERBOSE = 0;
    public static final int DEBUG = 1;
    public static final int INFO = 2;
    public static final int WARN = 3;
    public static final int ERROR = 4;
    
    private static int currentLogLevel = INFO;
    
    /**
     * Android環境かどうかを判定
     * @return Android環境の場合true
     */
    private static boolean isAndroidEnvironment() {
        try {
            Class.forName("android.util.Log");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
    
    /**
     * ログレベルを設定
     * @param level ログレベル
     */
    public static void setLogLevel(int level) {
        currentLogLevel = level;
    }
    
    /**
     * 現在のログレベルを取得
     * @return 現在のログレベル
     */
    public static int getLogLevel() {
        return currentLogLevel;
    }
    
    /**
     * VERBOSEレベルのログを出力
     * @param tag ログタグ
     * @param message メッセージ
     */
    public static void v(String tag, String message) {
        log(VERBOSE, tag, message, null);
    }
    
    /**
     * VERBOSEレベルのログを出力（例外付き）
     * @param tag ログタグ
     * @param message メッセージ
     * @param throwable 例外
     */
    public static void v(String tag, String message, Throwable throwable) {
        log(VERBOSE, tag, message, throwable);
    }
    
    /**
     * DEBUGレベルのログを出力
     * @param tag ログタグ
     * @param message メッセージ
     */
    public static void d(String tag, String message) {
        log(DEBUG, tag, message, null);
    }
    
    /**
     * DEBUGレベルのログを出力（例外付き）
     * @param tag ログタグ
     * @param message メッセージ
     * @param throwable 例外
     */
    public static void d(String tag, String message, Throwable throwable) {
        log(DEBUG, tag, message, throwable);
    }
    
    /**
     * INFOレベルのログを出力
     * @param tag ログタグ
     * @param message メッセージ
     */
    public static void i(String tag, String message) {
        log(INFO, tag, message, null);
    }
    
    /**
     * INFOレベルのログを出力（例外付き）
     * @param tag ログタグ
     * @param message メッセージ
     * @param throwable 例外
     */
    public static void i(String tag, String message, Throwable throwable) {
        log(INFO, tag, message, throwable);
    }
    
    /**
     * WARNレベルのログを出力
     * @param tag ログタグ
     * @param message メッセージ
     */
    public static void w(String tag, String message) {
        log(WARN, tag, message, null);
    }
    
    /**
     * WARNレベルのログを出力（例外付き）
     * @param tag ログタグ
     * @param message メッセージ
     * @param throwable 例外
     */
    public static void w(String tag, String message, Throwable throwable) {
        log(WARN, tag, message, throwable);
    }
    
    /**
     * ERRORレベルのログを出力
     * @param tag ログタグ
     * @param message メッセージ
     */
    public static void e(String tag, String message) {
        log(ERROR, tag, message, null);
    }
    
    /**
     * ERRORレベルのログを出力（例外付き）
     * @param tag ログタグ
     * @param message メッセージ
     * @param throwable 例外
     */
    public static void e(String tag, String message, Throwable throwable) {
        log(ERROR, tag, message, throwable);
    }
    
    /**
     * 統一されたログ出力メソッド
     * @param level ログレベル
     * @param tag ログタグ
     * @param message メッセージ
     * @param throwable 例外（nullの場合もある）
     */
    private static void log(int level, String tag, String message, Throwable throwable) {
        if (level < currentLogLevel) {
            return;
        }
        
        String fullTag = TAG + ":" + tag;
        String fullMessage = formatMessage(message, throwable);
        
        if (USE_ANDROID_LOG) {
            logToAndroid(level, fullTag, fullMessage, throwable);
        } else {
            logToJavaUtil(level, fullTag, fullMessage, throwable);
        }
    }
    
    /**
     * Androidログシステムに出力
     * @param level ログレベル
     * @param tag ログタグ
     * @param message メッセージ
     * @param throwable 例外
     */
    private static void logToAndroid(int level, String tag, String message, Throwable throwable) {
        switch (level) {
            case VERBOSE:
                if (throwable != null) {
                    Log.v(tag, message, throwable);
                } else {
                    Log.v(tag, message);
                }
                break;
            case DEBUG:
                if (throwable != null) {
                    Log.d(tag, message, throwable);
                } else {
                    Log.d(tag, message);
                }
                break;
            case INFO:
                if (throwable != null) {
                    Log.i(tag, message, throwable);
                } else {
                    Log.i(tag, message);
                }
                break;
            case WARN:
                if (throwable != null) {
                    Log.w(tag, message, throwable);
                } else {
                    Log.w(tag, message);
                }
                break;
            case ERROR:
                if (throwable != null) {
                    Log.e(tag, message, throwable);
                } else {
                    Log.e(tag, message);
                }
                break;
        }
    }
    
    /**
     * Java標準ログシステムに出力
     * @param level ログレベル
     * @param tag ログタグ
     * @param message メッセージ
     * @param throwable 例外
     */
    private static void logToJavaUtil(int level, String tag, String message, Throwable throwable) {
        Logger logger = Logger.getLogger(tag);
        Level javaLevel;
        
        switch (level) {
            case VERBOSE:
                javaLevel = Level.FINEST;
                break;
            case DEBUG:
                javaLevel = Level.FINE;
                break;
            case INFO:
                javaLevel = Level.INFO;
                break;
            case WARN:
                javaLevel = Level.WARNING;
                break;
            case ERROR:
                javaLevel = Level.SEVERE;
                break;
            default:
                javaLevel = Level.INFO;
        }
        
        if (throwable != null) {
            logger.log(javaLevel, message, throwable);
        } else {
            logger.log(javaLevel, message);
        }
    }
    
    /**
     * メッセージをフォーマット
     * @param message 基本メッセージ
     * @param throwable 例外
     * @return フォーマットされたメッセージ
     */
    private static String formatMessage(String message, Throwable throwable) {
        if (throwable == null) {
            return message;
        }
        
        StringBuilder sb = new StringBuilder(message);
        if (!message.endsWith(": ") && !message.endsWith(":")) {
            sb.append(": ");
        }
        sb.append(throwable.getClass().getSimpleName());
        if (throwable.getMessage() != null) {
            sb.append(" - ").append(throwable.getMessage());
        }
        
        return sb.toString();
    }
    
    /**
     * 操作の開始をログに記録
     * @param tag ログタグ
     * @param operation 操作名
     * @param details 詳細情報
     */
    public static void logOperationStart(String tag, String operation, String details) {
        i(tag, "開始: " + operation + (details != null ? " - " + details : ""));
    }
    
    /**
     * 操作の成功をログに記録
     * @param tag ログタグ
     * @param operation 操作名
     * @param details 詳細情報
     */
    public static void logOperationSuccess(String tag, String operation, String details) {
        i(tag, "成功: " + operation + (details != null ? " - " + details : ""));
    }
    
    /**
     * 操作の失敗をログに記録
     * @param tag ログタグ
     * @param operation 操作名
     * @param error エラー情報
     * @param throwable 例外
     */
    public static void logOperationFailure(String tag, String operation, String error, Throwable throwable) {
        e(tag, "失敗: " + operation + " - " + error, throwable);
    }
    
    /**
     * パフォーマンス情報をログに記録
     * @param tag ログタグ
     * @param operation 操作名
     * @param durationMs 実行時間（ミリ秒）
     * @param additionalInfo 追加情報
     */
    public static void logPerformance(String tag, String operation, long durationMs, String additionalInfo) {
        i(tag, "パフォーマンス: " + operation + " - " + durationMs + "ms" + 
          (additionalInfo != null ? " (" + additionalInfo + ")" : ""));
    }
    
    /**
     * セキュリティ関連の警告をログに記録
     * @param tag ログタグ
     * @param securityIssue セキュリティ問題の説明
     * @param details 詳細情報
     */
    public static void logSecurityWarning(String tag, String securityIssue, String details) {
        w(tag, "セキュリティ警告: " + securityIssue + (details != null ? " - " + details : ""));
    }
    
    /**
     * リソース使用量をログに記録
     * @param tag ログタグ
     * @param resourceType リソースタイプ
     * @param usage 使用量
     * @param unit 単位
     */
    public static void logResourceUsage(String tag, String resourceType, long usage, String unit) {
        d(tag, "リソース使用量: " + resourceType + " - " + usage + " " + unit);
    }
    
    /**
     * 例外の詳細情報をログに記録
     * @param tag ログタグ
     * @param operation 実行中の操作
     * @param exception 例外
     */
    public static void logExceptionDetails(String tag, String operation, Throwable exception) {
        if (exception == null) {
            return;
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("例外詳細 [").append(operation).append("]: ");
        sb.append(exception.getClass().getSimpleName());
        
        if (exception.getMessage() != null) {
            sb.append(" - ").append(exception.getMessage());
        }
        
        // スタックトレースの最初の数行を含める
        StackTraceElement[] stackTrace = exception.getStackTrace();
        if (stackTrace != null && stackTrace.length > 0) {
            sb.append("\n  at ").append(stackTrace[0].toString());
            if (stackTrace.length > 1) {
                sb.append("\n  at ").append(stackTrace[1].toString());
            }
        }
        
        // 原因となった例外も記録
        Throwable cause = exception.getCause();
        if (cause != null) {
            sb.append("\n原因: ").append(cause.getClass().getSimpleName());
            if (cause.getMessage() != null) {
                sb.append(" - ").append(cause.getMessage());
            }
        }
        
        e(tag, sb.toString(), exception);
    }
    
    /**
     * 回復可能なエラーをログに記録
     * @param tag ログタグ
     * @param operation 操作名
     * @param error エラー内容
     * @param recoveryAction 回復アクション
     */
    public static void logRecoverableError(String tag, String operation, String error, String recoveryAction) {
        w(tag, "回復可能エラー [" + operation + "]: " + error + " - 回復アクション: " + recoveryAction);
    }
    
    /**
     * データ整合性エラーをログに記録
     * @param tag ログタグ
     * @param dataType データタイプ
     * @param expected 期待値
     * @param actual 実際の値
     * @param context コンテキスト情報
     */
    public static void logDataIntegrityError(String tag, String dataType, Object expected, Object actual, String context) {
        e(tag, "データ整合性エラー [" + dataType + "]: 期待値=" + expected + ", 実際値=" + actual + 
             (context != null ? " (" + context + ")" : ""));
    }
    
    /**
     * 進捗情報をログに記録
     * @param tag ログタグ
     * @param operation 操作名
     * @param current 現在の進捗
     * @param total 総量
     * @param unit 単位
     */
    public static void logProgress(String tag, String operation, long current, long total, String unit) {
        if (total > 0) {
            double percentage = (double) current / total * 100;
            d(tag, "進捗 [" + operation + "]: " + current + "/" + total + " " + unit + 
                  " (" + String.format("%.1f", percentage) + "%)");
        } else {
            d(tag, "進捗 [" + operation + "]: " + current + " " + unit);
        }
    }
    
    /**
     * システム状態をログに記録
     * @param tag ログタグ
     * @param component コンポーネント名
     * @param state 状態
     * @param details 詳細情報
     */
    public static void logSystemState(String tag, String component, String state, String details) {
        i(tag, "システム状態 [" + component + "]: " + state + 
          (details != null ? " - " + details : ""));
    }
}