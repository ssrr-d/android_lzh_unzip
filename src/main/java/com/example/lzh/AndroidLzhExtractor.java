package com.example.lzh;

import android.content.Context;
import java.io.File;
import java.io.InputStream;
import com.example.lzh.exception.LzhException;
import com.example.lzh.exception.InvalidArchiveException;
import com.example.lzh.model.LzhArchive;
import com.example.lzh.util.AndroidCompatibility;
import com.example.lzh.util.AndroidCompatibilityVerifier;
import com.example.lzh.util.FileManager;
import com.example.lzh.util.LzhLogger;
import com.example.lzh.util.ErrorContext;

/**
 * Android専用のLZH解凍クラス
 * Android固有の機能と制限に対応した解凍機能を提供
 * 要件 8.1, 8.2, 8.3, 8.4 に対応
 */
public class AndroidLzhExtractor extends LzhExtractor {
    
    private static final String TAG = "AndroidLzhExtractor";
    private final Context context;
    
    /**
     * AndroidLzhExtractorを作成
     * @param context Androidコンテキスト
     * @throws LzhException Android互換性チェックに失敗した場合
     */
    public AndroidLzhExtractor(Context context) throws LzhException {
        if (context == null) {
            throw new InvalidArchiveException("Android context cannot be null");
        }
        
        this.context = context.getApplicationContext();
        
        // Android互換性チェック
        performCompatibilityCheck();
        
        LzhLogger.i(TAG, "AndroidLzhExtractor初期化完了");
        AndroidCompatibility.logDiagnosticInfo(this.context);
    }
    
    /**
     * Android互換性チェックを実行
     * @throws LzhException 互換性に問題がある場合
     */
    private void performCompatibilityCheck() throws LzhException {
        ErrorContext errorContext = new ErrorContext("Android互換性チェック");
        
        // 包括的な互換性検証を実行
        AndroidCompatibilityVerifier.VerificationResult result = 
            AndroidCompatibilityVerifier.verifyCompatibility(this.context);
        
        if (!result.isCompatible()) {
            StringBuilder errorMessage = new StringBuilder("Android互換性チェックに失敗しました:");
            for (String issue : result.getIssues()) {
                errorMessage.append("\n- ").append(issue);
            }
            
            LzhLogger.e(TAG, errorMessage.toString());
            throw new LzhException(errorMessage.toString(), errorContext);
        }
        
        // 警告がある場合はログに出力
        if (result.hasWarnings()) {
            LzhLogger.w(TAG, "互換性チェックで警告が発生しました:");
            for (String warning : result.getWarnings()) {
                LzhLogger.w(TAG, "- " + warning);
            }
        }
        
        LzhLogger.i(TAG, "Android互換性チェック完了");
    }
    
    /**
     * Android内部ストレージに解凍
     * @param lzhFile LZHアーカイブファイル
     * @param subdirectory サブディレクトリ名（nullの場合はfiles直下）
     * @throws LzhException 解凍エラー
     */
    public void extractToInternalStorage(File lzhFile, String subdirectory) throws LzhException {
        ErrorContext errorContext = new ErrorContext("Android内部ストレージ解凍")
            .withInputSource(lzhFile != null ? lzhFile.getAbsolutePath() : "null")
            .withData("subdirectory", subdirectory);
        
        LzhLogger.logOperationStart(TAG, "内部ストレージ解凍", 
            "ファイル: " + (lzhFile != null ? lzhFile.getAbsolutePath() : "null") + 
            ", サブディレクトリ: " + subdirectory);
        
        try {
            // 事前チェック
            if (lzhFile != null) {
                long fileSize = lzhFile.length();
                if (!AndroidCompatibility.checkAndroidLimitations(context, fileSize)) {
                    LzhLogger.w(TAG, "Android制限チェックで警告が発生しました");
                }
            }
            
            // 出力ディレクトリを作成
            File outputDir = FileManager.createInternalStorageDirectory(context, subdirectory);
            errorContext.withOutputPath(outputDir.getAbsolutePath());
            
            // 通常の解凍処理を実行
            extract(lzhFile, outputDir);
            
            LzhLogger.logOperationSuccess(TAG, "内部ストレージ解凍", 
                "出力先: " + outputDir.getAbsolutePath());
            
        } catch (Exception e) {
            LzhLogger.logOperationFailure(TAG, "内部ストレージ解凍", "エラー", e);
            if (e instanceof LzhException) {
                throw e;
            } else {
                throw new LzhException("Failed to extract to internal storage", errorContext, e);
            }
        }
    }
    
    /**
     * Android内部ストレージに解凍（InputStreamから）
     * @param inputStream LZHアーカイブのストリーム
     * @param subdirectory サブディレクトリ名（nullの場合はfiles直下）
     * @throws LzhException 解凍エラー
     */
    public void extractToInternalStorage(InputStream inputStream, String subdirectory) throws LzhException {
        ErrorContext errorContext = new ErrorContext("Android内部ストレージ解凍")
            .withInputSource("InputStream")
            .withData("subdirectory", subdirectory);
        
        LzhLogger.logOperationStart(TAG, "内部ストレージ解凍（ストリーム）", 
            "サブディレクトリ: " + subdirectory);
        
        try {
            // 出力ディレクトリを作成
            File outputDir = FileManager.createInternalStorageDirectory(context, subdirectory);
            errorContext.withOutputPath(outputDir.getAbsolutePath());
            
            // 通常の解凍処理を実行
            extract(inputStream, outputDir);
            
            LzhLogger.logOperationSuccess(TAG, "内部ストレージ解凍（ストリーム）", 
                "出力先: " + outputDir.getAbsolutePath());
            
        } catch (Exception e) {
            LzhLogger.logOperationFailure(TAG, "内部ストレージ解凍（ストリーム）", "エラー", e);
            if (e instanceof LzhException) {
                throw e;
            } else {
                throw new LzhException("Failed to extract stream to internal storage", errorContext, e);
            }
        }
    }
    
    /**
     * Android内部ストレージに解凍（バイト配列から）
     * @param lzhData LZHアーカイブのバイト配列
     * @param subdirectory サブディレクトリ名（nullの場合はfiles直下）
     * @throws LzhException 解凍エラー
     */
    public void extractToInternalStorage(byte[] lzhData, String subdirectory) throws LzhException {
        ErrorContext errorContext = new ErrorContext("Android内部ストレージ解凍")
            .withInputSource("byte array")
            .withFileSize(lzhData != null ? (long) lzhData.length : 0L)
            .withData("subdirectory", subdirectory);
        
        LzhLogger.logOperationStart(TAG, "内部ストレージ解凍（バイト配列）", 
            "データサイズ: " + (lzhData != null ? lzhData.length : 0) + " bytes, " +
            "サブディレクトリ: " + subdirectory);
        
        try {
            // 事前チェック
            if (lzhData != null) {
                if (!AndroidCompatibility.checkAndroidLimitations(context, lzhData.length)) {
                    LzhLogger.w(TAG, "Android制限チェックで警告が発生しました");
                }
            }
            
            // 出力ディレクトリを作成
            File outputDir = FileManager.createInternalStorageDirectory(context, subdirectory);
            errorContext.withOutputPath(outputDir.getAbsolutePath());
            
            // 通常の解凍処理を実行
            extract(lzhData, outputDir);
            
            LzhLogger.logOperationSuccess(TAG, "内部ストレージ解凍（バイト配列）", 
                "出力先: " + outputDir.getAbsolutePath());
            
        } catch (Exception e) {
            LzhLogger.logOperationFailure(TAG, "内部ストレージ解凍（バイト配列）", "エラー", e);
            if (e instanceof LzhException) {
                throw e;
            } else {
                throw new LzhException("Failed to extract byte array to internal storage", errorContext, e);
            }
        }
    }
    
    /**
     * 特定ファイルをAndroid内部ストレージに解凍
     * @param lzhFile LZHアーカイブファイル
     * @param fileName 抽出するファイル名
     * @param subdirectory サブディレクトリ名（nullの場合はfiles直下）
     * @throws LzhException 解凍エラー
     */
    public void extractFileToInternalStorage(File lzhFile, String fileName, String subdirectory) throws LzhException {
        ErrorContext errorContext = new ErrorContext("Android内部ストレージファイル解凍")
            .withInputSource(lzhFile != null ? lzhFile.getAbsolutePath() : "null")
            .withFileName(fileName)
            .withData("subdirectory", subdirectory);
        
        LzhLogger.logOperationStart(TAG, "内部ストレージファイル解凍", 
            "ファイル: " + (lzhFile != null ? lzhFile.getAbsolutePath() : "null") + 
            ", 対象: " + fileName + ", サブディレクトリ: " + subdirectory);
        
        try {
            // 出力ディレクトリを作成
            File outputDir = FileManager.createInternalStorageDirectory(context, subdirectory);
            errorContext.withOutputPath(outputDir.getAbsolutePath());
            
            // 通常のファイル解凍処理を実行
            extractFile(lzhFile, fileName, outputDir);
            
            LzhLogger.logOperationSuccess(TAG, "内部ストレージファイル解凍", 
                "出力先: " + outputDir.getAbsolutePath());
            
        } catch (Exception e) {
            LzhLogger.logOperationFailure(TAG, "内部ストレージファイル解凍", "エラー", e);
            if (e instanceof LzhException) {
                throw e;
            } else {
                throw new LzhException("Failed to extract file to internal storage", errorContext, e);
            }
        }
    }
    
    /**
     * 内部ストレージの使用可能容量を取得
     * @return 使用可能バイト数
     */
    public long getAvailableInternalStorageSpace() {
        return FileManager.getAvailableInternalStorageSpace(context);
    }
    
    /**
     * ストレージ情報を取得
     * @return ストレージ情報、取得できない場合はnull
     */
    public AndroidCompatibility.StorageInfo getStorageInfo() {
        return AndroidCompatibility.getInternalStorageInfo(context);
    }
    
    /**
     * メモリ情報を取得
     * @return メモリ情報
     */
    public AndroidCompatibility.MemoryInfo getMemoryInfo() {
        return AndroidCompatibility.getMemoryInfo();
    }
    
    /**
     * 診断情報をログに出力
     */
    public void logDiagnosticInfo() {
        AndroidCompatibility.logDiagnosticInfo(context);
    }
    
    /**
     * 使用中のAndroidコンテキストを取得
     * @return Androidコンテキスト
     */
    public Context getContext() {
        return context;
    }
}