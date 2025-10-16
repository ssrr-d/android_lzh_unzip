package com.example.lzh;

import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.logging.Logger;
import com.example.lzh.model.LzhArchive;
import com.example.lzh.model.LzhEntry;
import com.example.lzh.exception.LzhException;
import com.example.lzh.exception.InvalidArchiveException;
import com.example.lzh.exception.CorruptedArchiveException;
import com.example.lzh.exception.FileNotFoundException;
import com.example.lzh.parser.LzhHeader;
import com.example.lzh.util.FileManager;
import com.example.lzh.util.LzhLogger;
import com.example.lzh.util.ErrorContext;
import com.example.lzh.util.ComprehensiveErrorHandler;
import com.example.lzh.decompressor.LzhDecompressor;

/**
 * メインAPI - LZHアーカイブの解凍機能を提供
 * 包括的なエラーハンドリングと詳細なログ出力機能を含む
 * 要件 1.1, 1.2, 7.1, 7.2, 7.3, 7.4 に対応
 */
public class LzhExtractor {
    
    private static final String TAG = "LzhExtractor";
    private static final Logger logger = Logger.getLogger(LzhExtractor.class.getName());
    
    // パフォーマンス監視用
    private static final long PERFORMANCE_LOG_THRESHOLD_MS = 1000; // 1秒以上の処理でログ出力
    
    /**
     * ファイルから解凍
     * @param lzhFile LZHアーカイブファイル
     * @param outputDir 出力ディレクトリ
     * @throws LzhException 解凍エラー
     */
    public void extract(File lzhFile, File outputDir) throws LzhException {
        long startTime = System.currentTimeMillis();
        ErrorContext context = new ErrorContext("ファイルからの解凍")
            .withInputSource(lzhFile != null ? lzhFile.getAbsolutePath() : "null")
            .withOutputPath(outputDir != null ? outputDir.getAbsolutePath() : "null");
        
        LzhLogger.logOperationStart(TAG, "ファイル解凍", 
            "入力: " + (lzhFile != null ? lzhFile.getAbsolutePath() : "null") + 
            ", 出力: " + (outputDir != null ? outputDir.getAbsolutePath() : "null"));
        
        try {
            // 入力検証
            validateFileInput(lzhFile, context);
            validateOutputDirectory(outputDir, context);
            
            // ファイルサイズをコンテキストに追加
            context.withFileSize(lzhFile.length());
            
            try (FileInputStream fileInputStream = new FileInputStream(lzhFile)) {
                extract(fileInputStream, outputDir);
                
                long duration = System.currentTimeMillis() - startTime;
                LzhLogger.logOperationSuccess(TAG, "ファイル解凍", 
                    "処理時間: " + duration + "ms, ファイルサイズ: " + lzhFile.length() + " bytes");
                
                if (duration > PERFORMANCE_LOG_THRESHOLD_MS) {
                    LzhLogger.logPerformance(TAG, "ファイル解凍", duration, 
                        "ファイルサイズ: " + lzhFile.length() + " bytes");
                }
            }
        } catch (IOException e) {
            LzhLogger.logOperationFailure(TAG, "ファイル解凍", "I/Oエラー", e);
            throw new LzhException("Failed to read LZH file", context.withData("ioError", e.getMessage()), e);
        } catch (Exception e) {
            LzhLogger.logOperationFailure(TAG, "ファイル解凍", "エラー", e);
            throw ComprehensiveErrorHandler.handleException(e, "ファイル解凍", context);
        }
    }
    
    /**
     * InputStreamから解凍
     * @param inputStream LZHアーカイブのストリーム
     * @param outputDir 出力ディレクトリ
     * @throws LzhException 解凍エラー
     */
    public void extract(InputStream inputStream, File outputDir) throws LzhException {
        long startTime = System.currentTimeMillis();
        ErrorContext context = new ErrorContext("ストリームからの解凍")
            .withInputSource("InputStream")
            .withOutputPath(outputDir != null ? outputDir.getAbsolutePath() : "null");
        
        LzhLogger.logOperationStart(TAG, "ストリーム解凍", 
            "出力: " + (outputDir != null ? outputDir.getAbsolutePath() : "null"));
        
        try {
            // 入力検証
            validateStreamInput(inputStream, context);
            validateOutputDirectory(outputDir, context);
            
            // 出力ディレクトリを作成
            try {
                FileManager.createDirectories(outputDir);
                LzhLogger.d(TAG, "出力ディレクトリを作成しました: " + outputDir.getAbsolutePath());
            } catch (IOException e) {
                LzhLogger.e(TAG, "出力ディレクトリの作成に失敗しました", e);
                throw new LzhException("Failed to create output directory", 
                    context.withData("directoryError", e.getMessage()), e);
            }
            
            // アーカイブを解析して各エントリを解凍
            extractAllEntries(inputStream, outputDir, context);
            
            long duration = System.currentTimeMillis() - startTime;
            LzhLogger.logOperationSuccess(TAG, "ストリーム解凍", "処理時間: " + duration + "ms");
            
            if (duration > PERFORMANCE_LOG_THRESHOLD_MS) {
                LzhLogger.logPerformance(TAG, "ストリーム解凍", duration, null);
            }
            
        } catch (Exception e) {
            LzhLogger.logOperationFailure(TAG, "ストリーム解凍", "エラー", e);
            throw ComprehensiveErrorHandler.handleException(e, "ストリーム解凍", context);
        }
    }
    
    /**
     * バイト配列から解凍
     * @param lzhData LZHアーカイブのバイト配列
     * @param outputDir 出力ディレクトリ
     * @throws LzhException 解凍エラー
     */
    public void extract(byte[] lzhData, File outputDir) throws LzhException {
        long startTime = System.currentTimeMillis();
        ErrorContext context = new ErrorContext("バイト配列からの解凍")
            .withInputSource("byte array")
            .withFileSize(lzhData != null ? (long) lzhData.length : 0L)
            .withOutputPath(outputDir != null ? outputDir.getAbsolutePath() : "null");
        
        LzhLogger.logOperationStart(TAG, "バイト配列解凍", 
            "データサイズ: " + (lzhData != null ? lzhData.length : 0) + " bytes, " +
            "出力: " + (outputDir != null ? outputDir.getAbsolutePath() : "null"));
        
        try {
            // 入力検証
            validateByteArrayInput(lzhData, context);
            validateOutputDirectory(outputDir, context);
            
            try (ByteArrayInputStream byteInputStream = new ByteArrayInputStream(lzhData)) {
                extract(byteInputStream, outputDir);
                
                long duration = System.currentTimeMillis() - startTime;
                LzhLogger.logOperationSuccess(TAG, "バイト配列解凍", 
                    "処理時間: " + duration + "ms, データサイズ: " + lzhData.length + " bytes");
                
                if (duration > PERFORMANCE_LOG_THRESHOLD_MS) {
                    LzhLogger.logPerformance(TAG, "バイト配列解凍", duration, 
                        "データサイズ: " + lzhData.length + " bytes");
                }
            }
        } catch (IOException e) {
            LzhLogger.logOperationFailure(TAG, "バイト配列解凍", "I/Oエラー", e);
            throw new LzhException("Failed to extract LZH archive from byte array", context, e);
        } catch (Exception e) {
            LzhLogger.logOperationFailure(TAG, "バイト配列解凍", "エラー", e);
            throw ComprehensiveErrorHandler.handleException(e, "バイト配列解凍", context);
        }
    }
    
    /**
     * アーカイブ情報の取得（ファイルから）
     * @param lzhFile LZHアーカイブファイル
     * @return アーカイブ情報
     * @throws LzhException 解析エラー
     */
    public LzhArchive getArchiveInfo(File lzhFile) throws LzhException {
        ErrorContext context = new ErrorContext("アーカイブ情報取得")
            .withInputSource(lzhFile != null ? lzhFile.getAbsolutePath() : "null");
        
        LzhLogger.logOperationStart(TAG, "アーカイブ情報取得", 
            "ファイル: " + (lzhFile != null ? lzhFile.getAbsolutePath() : "null"));
        
        try {
            // 入力検証
            validateFileInput(lzhFile, context);
            
            try (FileInputStream fileInputStream = new FileInputStream(lzhFile)) {
                LzhArchive archive = getArchiveInfo(fileInputStream);
                LzhLogger.logOperationSuccess(TAG, "アーカイブ情報取得", 
                    "エントリ数: " + archive.getEntryCount());
                return archive;
            }
        } catch (IOException e) {
            LzhLogger.logOperationFailure(TAG, "アーカイブ情報取得", "I/Oエラー", e);
            throw new LzhException("Failed to read LZH file", context, e);
        } catch (LzhException e) {
            LzhLogger.logOperationFailure(TAG, "アーカイブ情報取得", "LZH処理エラー", e);
            if (e.getErrorContext() == null) {
                e.setErrorContext(context);
            }
            throw e;
        } catch (Exception e) {
            LzhLogger.logOperationFailure(TAG, "アーカイブ情報取得", "予期しないエラー", e);
            throw new LzhException("Unexpected error during archive info retrieval", context, e);
        }
    }
    
    /**
     * アーカイブ情報の取得（InputStreamから）
     * @param inputStream LZHアーカイブのストリーム
     * @return アーカイブ情報
     * @throws LzhException 解析エラー
     */
    public LzhArchive getArchiveInfo(InputStream inputStream) throws LzhException {
        ErrorContext context = new ErrorContext("アーカイブ情報取得")
            .withInputSource("InputStream");
        
        LzhLogger.logOperationStart(TAG, "アーカイブ情報取得（ストリーム）", null);
        
        try {
            // 入力検証
            validateStreamInput(inputStream, context);
            
            LzhArchive archive = parseArchiveInfo(inputStream, context);
            LzhLogger.logOperationSuccess(TAG, "アーカイブ情報取得（ストリーム）", 
                "エントリ数: " + archive.getEntryCount());
            return archive;
        } catch (IOException e) {
            LzhLogger.logOperationFailure(TAG, "アーカイブ情報取得（ストリーム）", "I/Oエラー", e);
            throw new LzhException("Failed to parse LZH archive information", context, e);
        } catch (LzhException e) {
            LzhLogger.logOperationFailure(TAG, "アーカイブ情報取得（ストリーム）", "LZH処理エラー", e);
            if (e.getErrorContext() == null) {
                e.setErrorContext(context);
            }
            throw e;
        } catch (Exception e) {
            LzhLogger.logOperationFailure(TAG, "アーカイブ情報取得（ストリーム）", "予期しないエラー", e);
            throw new LzhException("Unexpected error during archive info parsing", context, e);
        }
    }
    
    /**
     * アーカイブ情報の取得（バイト配列から）
     * @param lzhData LZHアーカイブのバイト配列
     * @return アーカイブ情報
     * @throws LzhException 解析エラー
     */
    public LzhArchive getArchiveInfo(byte[] lzhData) throws LzhException {
        ErrorContext context = new ErrorContext("アーカイブ情報取得")
            .withInputSource("byte array")
            .withFileSize(lzhData != null ? (long) lzhData.length : 0L);
        
        LzhLogger.logOperationStart(TAG, "アーカイブ情報取得（バイト配列）", 
            "データサイズ: " + (lzhData != null ? lzhData.length : 0) + " bytes");
        
        try {
            // 入力検証
            validateByteArrayInput(lzhData, context);
            
            try (ByteArrayInputStream byteInputStream = new ByteArrayInputStream(lzhData)) {
                LzhArchive archive = getArchiveInfo(byteInputStream);
                LzhLogger.logOperationSuccess(TAG, "アーカイブ情報取得（バイト配列）", 
                    "エントリ数: " + archive.getEntryCount());
                return archive;
            }
        } catch (IOException e) {
            LzhLogger.logOperationFailure(TAG, "アーカイブ情報取得（バイト配列）", "I/Oエラー", e);
            throw new LzhException("Failed to parse LZH archive information from byte array", context, e);
        } catch (LzhException e) {
            LzhLogger.logOperationFailure(TAG, "アーカイブ情報取得（バイト配列）", "LZH処理エラー", e);
            if (e.getErrorContext() == null) {
                e.setErrorContext(context);
            }
            throw e;
        } catch (Exception e) {
            LzhLogger.logOperationFailure(TAG, "アーカイブ情報取得（バイト配列）", "予期しないエラー", e);
            throw new LzhException("Unexpected error during byte array archive info parsing", context, e);
        }
    }
    
    /**
     * 特定ファイルの抽出（ファイルから）
     * @param lzhFile LZHアーカイブファイル
     * @param fileName 抽出するファイル名
     * @param outputDir 出力ディレクトリ
     * @throws LzhException 解凍エラー
     * @throws FileNotFoundException ファイルが見つからない場合
     */
    public void extractFile(File lzhFile, String fileName, File outputDir) throws LzhException {
        ErrorContext context = new ErrorContext("個別ファイル抽出")
            .withInputSource(lzhFile != null ? lzhFile.getAbsolutePath() : "null")
            .withFileName(fileName)
            .withOutputPath(outputDir != null ? outputDir.getAbsolutePath() : "null");
        
        LzhLogger.logOperationStart(TAG, "個別ファイル抽出", 
            "ファイル: " + fileName + ", 入力: " + (lzhFile != null ? lzhFile.getAbsolutePath() : "null"));
        
        try {
            // 入力検証
            validateFileInput(lzhFile, context);
            validateFileNameInput(fileName);
            validateOutputDirectory(outputDir, context);
            
            try (FileInputStream fileInputStream = new FileInputStream(lzhFile)) {
                extractFile(fileInputStream, fileName, outputDir);
                LzhLogger.logOperationSuccess(TAG, "個別ファイル抽出", "ファイル: " + fileName);
            }
        } catch (IOException e) {
            LzhLogger.logOperationFailure(TAG, "個別ファイル抽出", "I/Oエラー", e);
            throw new LzhException("Failed to read LZH file: " + lzhFile.getAbsolutePath(), context, e);
        } catch (Exception e) {
            LzhLogger.logOperationFailure(TAG, "個別ファイル抽出", "エラー", e);
            throw ComprehensiveErrorHandler.handleException(e, "個別ファイル抽出", context);
        }
    }
    
    /**
     * 特定ファイルの抽出（InputStreamから）
     * @param inputStream LZHアーカイブのストリーム
     * @param fileName 抽出するファイル名
     * @param outputDir 出力ディレクトリ
     * @throws LzhException 解凍エラー
     * @throws FileNotFoundException ファイルが見つからない場合
     */
    public void extractFile(InputStream inputStream, String fileName, File outputDir) throws LzhException {
        ErrorContext context = new ErrorContext("個別ファイル抽出")
            .withInputSource("InputStream")
            .withFileName(fileName)
            .withOutputPath(outputDir != null ? outputDir.getAbsolutePath() : "null");
        
        LzhLogger.logOperationStart(TAG, "個別ファイル抽出（ストリーム）", "ファイル: " + fileName);
        
        try {
            // 入力検証
            validateStreamInput(inputStream, context);
            validateFileNameInput(fileName);
            validateOutputDirectory(outputDir, context);
            
            // 出力ディレクトリを作成
            FileManager.createDirectories(outputDir);
            
            // 指定されたファイルを検索して解凍
            extractSpecificFile(inputStream, fileName, outputDir, context);
            
            LzhLogger.logOperationSuccess(TAG, "個別ファイル抽出（ストリーム）", "ファイル: " + fileName);
            
        } catch (IOException e) {
            LzhLogger.logOperationFailure(TAG, "個別ファイル抽出（ストリーム）", "I/Oエラー", e);
            throw new LzhException("Failed to extract file from LZH archive", context, e);
        } catch (Exception e) {
            LzhLogger.logOperationFailure(TAG, "個別ファイル抽出（ストリーム）", "エラー", e);
            throw ComprehensiveErrorHandler.handleException(e, "個別ファイル抽出（ストリーム）", context);
        }
    }
    
    /**
     * 特定ファイルの抽出（バイト配列から）
     * @param lzhData LZHアーカイブのバイト配列
     * @param fileName 抽出するファイル名
     * @param outputDir 出力ディレクトリ
     * @throws LzhException 解凍エラー
     * @throws FileNotFoundException ファイルが見つからない場合
     */
    public void extractFile(byte[] lzhData, String fileName, File outputDir) throws LzhException {
        ErrorContext context = new ErrorContext("個別ファイル抽出")
            .withInputSource("byte array")
            .withFileName(fileName)
            .withFileSize(lzhData != null ? (long) lzhData.length : 0L)
            .withOutputPath(outputDir != null ? outputDir.getAbsolutePath() : "null");
        
        LzhLogger.logOperationStart(TAG, "個別ファイル抽出（バイト配列）", 
            "ファイル: " + fileName + ", データサイズ: " + (lzhData != null ? lzhData.length : 0) + " bytes");
        
        try {
            // 入力検証
            validateByteArrayInput(lzhData, context);
            validateFileNameInput(fileName);
            validateOutputDirectory(outputDir, context);
            
            try (ByteArrayInputStream byteInputStream = new ByteArrayInputStream(lzhData)) {
                extractFile(byteInputStream, fileName, outputDir);
                LzhLogger.logOperationSuccess(TAG, "個別ファイル抽出（バイト配列）", "ファイル: " + fileName);
            }
        } catch (IOException e) {
            LzhLogger.logOperationFailure(TAG, "個別ファイル抽出（バイト配列）", "I/Oエラー", e);
            throw new LzhException("Failed to extract file from LZH archive byte array", context, e);
        } catch (Exception e) {
            LzhLogger.logOperationFailure(TAG, "個別ファイル抽出（バイト配列）", "エラー", e);
            throw ComprehensiveErrorHandler.handleException(e, "個別ファイル抽出（バイト配列）", context);
        }
    }
    
    /**
     * ファイル入力の検証
     * @param lzhFile LZHファイル
     * @param context エラーコンテキスト
     * @throws InvalidArchiveException 無効なファイル
     */
    private void validateFileInput(File lzhFile, ErrorContext context) throws InvalidArchiveException {
        if (lzhFile == null) {
            LzhLogger.e(TAG, "LZHファイルがnullです");
            throw InvalidArchiveException.forInputValidation("LZH file cannot be null", context);
        }
        
        String filePath = lzhFile.getAbsolutePath();
        context.withInputSource(filePath);
        
        if (!lzhFile.exists()) {
            LzhLogger.e(TAG, "LZHファイルが存在しません: " + filePath);
            throw InvalidArchiveException.forInputValidation("LZH file does not exist: " + filePath, context);
        }
        
        if (!lzhFile.isFile()) {
            LzhLogger.e(TAG, "指定されたパスはファイルではありません: " + filePath);
            throw InvalidArchiveException.forInputValidation("Path is not a file: " + filePath, context);
        }
        
        if (!lzhFile.canRead()) {
            LzhLogger.e(TAG, "LZHファイルを読み取れません: " + filePath);
            LzhLogger.logSecurityWarning(TAG, "ファイル読み取り権限なし", filePath);
            throw InvalidArchiveException.forInputValidation("Cannot read LZH file: " + filePath, context);
        }
        
        long fileSize = lzhFile.length();
        if (fileSize == 0) {
            LzhLogger.e(TAG, "LZHファイルが空です: " + filePath);
            throw InvalidArchiveException.forInputValidation("LZH file is empty: " + filePath, context);
        }
        
        // ファイルサイズの妥当性チェック
        if (fileSize > Integer.MAX_VALUE) {
            LzhLogger.w(TAG, "非常に大きなファイルです: " + fileSize + " bytes");
            LzhLogger.logResourceUsage(TAG, "ファイルサイズ", fileSize, "bytes");
        }
        
        LzhLogger.d(TAG, "ファイル入力検証完了: " + filePath + " (" + fileSize + " bytes)");
    }
    
    /**
     * ストリーム入力の検証
     * @param inputStream 入力ストリーム
     * @param context エラーコンテキスト
     * @throws InvalidArchiveException 無効なストリーム
     */
    private void validateStreamInput(InputStream inputStream, ErrorContext context) throws InvalidArchiveException {
        if (inputStream == null) {
            LzhLogger.e(TAG, "入力ストリームがnullです");
            throw InvalidArchiveException.forInputValidation("Input stream cannot be null", context);
        }
        
        // ストリームの可用性をチェック
        try {
            if (inputStream.available() == 0) {
                LzhLogger.w(TAG, "入力ストリームに利用可能なデータがありません");
            }
        } catch (IOException e) {
            LzhLogger.w(TAG, "ストリームの可用性チェックに失敗しました", e);
            // 可用性チェックの失敗は致命的ではないので続行
        }
        
        LzhLogger.d(TAG, "ストリーム入力検証完了");
    }
    
    /**
     * バイト配列入力の検証
     * @param lzhData バイト配列
     * @param context エラーコンテキスト
     * @throws InvalidArchiveException 無効なデータ
     */
    private void validateByteArrayInput(byte[] lzhData, ErrorContext context) throws InvalidArchiveException {
        if (lzhData == null) {
            LzhLogger.e(TAG, "LZHデータがnullです");
            throw InvalidArchiveException.forInputValidation("LZH data cannot be null", context);
        }
        
        if (lzhData.length == 0) {
            LzhLogger.e(TAG, "LZHデータが空です");
            throw InvalidArchiveException.forInputValidation("LZH data cannot be empty", context);
        }
        
        context.withFileSize((long) lzhData.length);
        LzhLogger.logResourceUsage(TAG, "バイト配列サイズ", lzhData.length, "bytes");
        LzhLogger.d(TAG, "バイト配列入力検証完了: " + lzhData.length + " bytes");
    }
    
    /**
     * 出力ディレクトリの検証
     * @param outputDir 出力ディレクトリ
     * @param context エラーコンテキスト
     * @throws InvalidArchiveException 無効なディレクトリ
     */
    private void validateOutputDirectory(File outputDir, ErrorContext context) throws InvalidArchiveException {
        if (outputDir == null) {
            LzhLogger.e(TAG, "出力ディレクトリがnullです");
            throw InvalidArchiveException.forInputValidation("Output directory cannot be null", context);
        }
        
        String outputPath = outputDir.getAbsolutePath();
        context.withOutputPath(outputPath);
        
        // 出力ディレクトリが存在する場合の検証
        if (outputDir.exists()) {
            if (!outputDir.isDirectory()) {
                LzhLogger.e(TAG, "出力パスがディレクトリではありません: " + outputPath);
                throw InvalidArchiveException.forInputValidation("Output path exists but is not a directory: " + outputPath, context);
            }
            
            if (!outputDir.canWrite()) {
                LzhLogger.e(TAG, "出力ディレクトリに書き込み権限がありません: " + outputPath);
                LzhLogger.logSecurityWarning(TAG, "ディレクトリ書き込み権限なし", outputPath);
                throw InvalidArchiveException.forInputValidation("Cannot write to output directory: " + outputPath, context);
            }
        } else {
            // 親ディレクトリの存在と書き込み権限をチェック
            File parentDir = outputDir.getParentFile();
            if (parentDir != null && parentDir.exists() && !parentDir.canWrite()) {
                LzhLogger.e(TAG, "親ディレクトリに書き込み権限がありません: " + parentDir.getAbsolutePath());
                LzhLogger.logSecurityWarning(TAG, "親ディレクトリ書き込み権限なし", parentDir.getAbsolutePath());
                throw InvalidArchiveException.forInputValidation("Cannot create directory in parent: " + parentDir.getAbsolutePath(), context);
            }
        }
        
        LzhLogger.d(TAG, "出力ディレクトリ検証完了: " + outputPath);
    }
    
    /**
     * ファイル名入力の検証
     * @param fileName ファイル名
     * @throws InvalidArchiveException 無効なファイル名
     */
    private void validateFileNameInput(String fileName) throws InvalidArchiveException {
        if (fileName == null) {
            LzhLogger.e(TAG, "ファイル名がnullです");
            throw InvalidArchiveException.forInputValidation("File name cannot be null", 
                new ErrorContext("ファイル名検証").withFileName("null"));
        }
        if (fileName.trim().isEmpty()) {
            LzhLogger.e(TAG, "ファイル名が空です");
            throw InvalidArchiveException.forInputValidation("File name cannot be empty", 
                new ErrorContext("ファイル名検証").withFileName("empty"));
        }
        
        // ファイル名の安全性チェック
        if (fileName.contains("..") || fileName.contains("\\") || fileName.startsWith("/")) {
            LzhLogger.logSecurityWarning(TAG, "安全でないファイル名", fileName);
            throw InvalidArchiveException.forInputValidation("Unsafe file name: " + fileName, 
                new ErrorContext("ファイル名検証").withFileName(fileName));
        }
        
        LzhLogger.d(TAG, "ファイル名検証完了: " + fileName);
    }
    
    /**
     * 指定されたファイルを検索して解凍
     * @param inputStream 入力ストリーム
     * @param targetFileName 抽出対象のファイル名
     * @param outputDir 出力ディレクトリ
     * @param context エラーコンテキスト
     * @throws IOException I/Oエラー
     * @throws LzhException 解凍エラー
     * @throws FileNotFoundException ファイルが見つからない場合
     */
    private void extractSpecificFile(InputStream inputStream, String targetFileName, File outputDir, ErrorContext context) 
            throws IOException, LzhException {
        
        LzhEntry entry;
        boolean fileFound = false;
        int entryCount = 0;
        
        LzhLogger.i(TAG, "ファイル検索開始: " + targetFileName);
        
        try {
            // アーカイブ内の各エントリを順次処理
            while ((entry = LzhHeader.parseHeader(inputStream)) != null) {
                entryCount++;
                
                ErrorContext entryContext = context.copy()
                    .withData("entryNumber", entryCount)
                    .withData("currentEntry", entry.getFileName());
                
                LzhLogger.d(TAG, "エントリ処理 " + entryCount + ": " + entry.getFileName());
                
                // ファイル名が一致するかチェック
                if (entry.getFileName().equals(targetFileName)) {
                    fileFound = true;
                    LzhLogger.i(TAG, "対象ファイル発見: " + targetFileName + 
                               " (" + entry.getCompressionMethod() + ", " + entry.getOriginalSize() + " bytes)");
                    
                    // エラーコンテキストを更新
                    entryContext.withCompressionMethod(entry.getCompressionMethod())
                               .withFileSize(entry.getOriginalSize())
                               .withData("compressedSize", entry.getCompressedSize());
                    
                    // ファイルパスの安全性をチェック
                    if (!FileManager.isValidOutputPath(outputDir, entry.getFileName())) {
                        LzhLogger.logSecurityWarning(TAG, "安全でないファイルパス", entry.getFileName());
                        throw new LzhException("Unsafe file path: " + entry.getFileName(), entryContext);
                    }
                    
                    // 出力ファイルパスを作成
                    File outputFile;
                    try {
                        outputFile = FileManager.createSafeFilePath(outputDir, entry.getFileName());
                    } catch (IOException e) {
                        LzhLogger.e(TAG, "出力ファイルパス作成失敗", e);
                        throw new LzhException("Failed to create output file path", entryContext, e);
                    }
                    
                    try {
                        // エントリを解凍
                        extractSingleEntry(inputStream, outputFile, entry, entryContext);
                        LzhLogger.i(TAG, "解凍成功: " + entry.getFileName());
                        return; // ファイルが見つかって解凍完了したので終了
                    } catch (Exception e) {
                        LzhLogger.e(TAG, "エントリ解凍失敗: " + entry.getFileName(), e);
                        throw new LzhException("Failed to extract " + entry.getFileName(), entryContext, e);
                    }
                } else {
                    // 対象ファイルではないのでデータをスキップ
                    try {
                        skipEntryData(inputStream, entry.getCompressedSize());
                        LzhLogger.d(TAG, "エントリスキップ: " + entry.getFileName());
                    } catch (IOException e) {
                        LzhLogger.w(TAG, "エントリデータスキップ失敗: " + entry.getFileName(), e);
                        throw CorruptedArchiveException.forIncompleteData(
                            entry.getCompressedSize(), 0, entryContext);
                    }
                }
            }
            
            // ファイルが見つからなかった場合
            if (!fileFound) {
                if (entryCount == 0) {
                    LzhLogger.e(TAG, "アーカイブに有効なエントリが見つかりません");
                    throw InvalidArchiveException.forEmptyArchive(context);
                } else {
                    LzhLogger.e(TAG, "ファイルが見つかりません: " + targetFileName + " (検索したエントリ数: " + entryCount + ")");
                    throw new com.example.lzh.exception.FileNotFoundException(
                        "File not found in archive: " + targetFileName, 
                        context.withData("searchedEntries", entryCount));
                }
            }
            
        } catch (IOException e) {
            LzhLogger.e(TAG, "アーカイブ読み取りエラー", e);
            throw CorruptedArchiveException.forDataIntegrityError("Archive reading failed during file search", context);
        }
    }
    
    /**
     * 全エントリを解凍
     * @param inputStream 入力ストリーム
     * @param outputDir 出力ディレクトリ
     * @param context エラーコンテキスト
     * @throws IOException I/Oエラー
     * @throws LzhException 解凍エラー
     */
    private void extractAllEntries(InputStream inputStream, File outputDir, ErrorContext context) throws IOException, LzhException {
        LzhEntry entry;
        int entryCount = 0;
        int successCount = 0;
        int skippedCount = 0;
        long totalProcessedBytes = 0;
        
        LzhLogger.i(TAG, "全エントリ解凍開始: " + outputDir.getAbsolutePath());
        
        try {
            // アーカイブ内の各エントリを順次処理
            while ((entry = LzhHeader.parseHeader(inputStream)) != null) {
                entryCount++;
                
                ErrorContext entryContext = context.copy()
                    .withFileName(entry.getFileName())
                    .withCompressionMethod(entry.getCompressionMethod())
                    .withFileSize(entry.getOriginalSize())
                    .withData("entryNumber", entryCount)
                    .withData("compressedSize", entry.getCompressedSize());
                
                LzhLogger.i(TAG, "エントリ処理中 " + entryCount + ": " + entry.getFileName() + 
                           " (" + entry.getCompressionMethod() + ", " + entry.getOriginalSize() + " bytes)");
                
                // ファイルパスの安全性をチェック
                if (!FileManager.isValidOutputPath(outputDir, entry.getFileName())) {
                    LzhLogger.logSecurityWarning(TAG, "安全でないファイルパスをスキップ", entry.getFileName());
                    skippedCount++;
                    skipEntryData(inputStream, entry.getCompressedSize());
                    continue;
                }
                
                // 出力ファイルパスを作成
                File outputFile;
                try {
                    outputFile = FileManager.createSafeFilePath(outputDir, entry.getFileName());
                } catch (IOException e) {
                    LzhLogger.w(TAG, "ファイルパス作成失敗: " + entry.getFileName(), e);
                    skippedCount++;
                    skipEntryData(inputStream, entry.getCompressedSize());
                    continue;
                }
                
                try {
                    // エントリを解凍
                    extractSingleEntry(inputStream, outputFile, entry, entryContext);
                    successCount++;
                    totalProcessedBytes += entry.getOriginalSize();
                    LzhLogger.i(TAG, "解凍成功: " + entry.getFileName());
                } catch (Exception e) {
                    LzhLogger.w(TAG, "エントリ解凍失敗: " + entry.getFileName(), e);
                    skippedCount++;
                    // 個別ファイルの解凍失敗は警告として記録し、処理を継続
                    try {
                        skipEntryData(inputStream, entry.getCompressedSize());
                    } catch (IOException skipError) {
                        LzhLogger.e(TAG, "データスキップ失敗", skipError);
                        throw CorruptedArchiveException.forIncompleteData(
                            entry.getCompressedSize(), 0, entryContext);
                    }
                }
                
                // 進捗をコンテキストに更新
                context.withProcessedBytes(totalProcessedBytes);
            }
            
            // 結果の検証
            if (entryCount == 0) {
                LzhLogger.e(TAG, "アーカイブに有効なエントリが見つかりません");
                throw InvalidArchiveException.forEmptyArchive(context);
            }
            
            if (successCount == 0) {
                LzhLogger.e(TAG, "すべてのエントリの解凍に失敗しました");
                throw new LzhException("All entries failed to extract", context);
            }
            
            // 完了ログ
            String summary = String.format("解凍完了 - 総エントリ数: %d, 成功: %d, スキップ: %d, 処理バイト数: %d", 
                                         entryCount, successCount, skippedCount, totalProcessedBytes);
            LzhLogger.i(TAG, summary);
            LzhLogger.logResourceUsage(TAG, "処理済みデータ", totalProcessedBytes, "bytes");
            
            if (skippedCount > 0) {
                LzhLogger.w(TAG, skippedCount + " 個のエントリがスキップされました");
            }
            
        } catch (IOException e) {
            LzhLogger.e(TAG, "アーカイブ読み取りエラー", e);
            throw CorruptedArchiveException.forDataIntegrityError("Archive reading failed", context);
        }
    }
    
    /**
     * 単一エントリを解凍
     * @param inputStream 入力ストリーム
     * @param outputFile 出力ファイル
     * @param entry エントリ情報
     * @param context エラーコンテキスト
     * @throws IOException I/Oエラー
     * @throws LzhException 解凍エラー
     */
    private void extractSingleEntry(InputStream inputStream, File outputFile, LzhEntry entry, ErrorContext context) 
            throws IOException, LzhException {
        
        long startTime = System.currentTimeMillis();
        LzhLogger.d(TAG, "単一エントリ解凍開始: " + entry.getFileName());
        
        try {
            // 解凍器を作成
            LzhDecompressor decompressor;
            try {
                decompressor = LzhDecompressor.createDecompressor(entry.getCompressionMethod());
            } catch (Exception e) {
                LzhLogger.e(TAG, "解凍器作成失敗: " + entry.getCompressionMethod(), e);
                throw new LzhException("Failed to create decompressor", context, e);
            }
            
            // 出力ファイルの親ディレクトリを作成
            File parentDir = outputFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                try {
                    FileManager.createDirectories(parentDir);
                    LzhLogger.d(TAG, "親ディレクトリ作成: " + parentDir.getAbsolutePath());
                } catch (IOException e) {
                    LzhLogger.e(TAG, "親ディレクトリ作成失敗", e);
                    throw new LzhException("Failed to create parent directory", context, e);
                }
            }
            
            // 圧縮データを制限付きストリームでラップ
            LimitedInputStream limitedInput = new LimitedInputStream(inputStream, entry.getCompressedSize());
            
            // ファイルに解凍
            try (java.io.FileOutputStream fileOutput = new java.io.FileOutputStream(outputFile)) {
                LzhLogger.d(TAG, "解凍処理開始: " + entry.getCompressionMethod());
                
                decompressor.decompressWithBuffering(limitedInput, fileOutput, 
                                                   entry.getCompressedSize(), entry.getOriginalSize());
                
                fileOutput.flush();
                LzhLogger.d(TAG, "解凍処理完了");
                
            } catch (IOException e) {
                LzhLogger.e(TAG, "ファイル書き込みエラー", e);
                // 失敗したファイルを削除
                if (outputFile.exists()) {
                    try {
                        outputFile.delete();
                        LzhLogger.d(TAG, "失敗したファイルを削除: " + outputFile.getAbsolutePath());
                    } catch (Exception deleteError) {
                        LzhLogger.w(TAG, "ファイル削除失敗", deleteError);
                    }
                }
                throw new LzhException("Failed to write decompressed data", context, e);
            }
            
            // ファイルサイズの検証
            long actualSize = outputFile.length();
            if (actualSize != entry.getOriginalSize()) {
                LzhLogger.w(TAG, String.format("ファイルサイズ不一致: 期待値=%d, 実際=%d", 
                                              entry.getOriginalSize(), actualSize));
                // サイズ不一致は警告として記録するが、処理は継続
            }
            
            // 最終更新日時を設定
            if (entry.getLastModified() != null) {
                try {
                    boolean success = outputFile.setLastModified(entry.getLastModified().getTime());
                    if (!success) {
                        LzhLogger.d(TAG, "最終更新日時の設定に失敗: " + entry.getFileName());
                    }
                } catch (Exception e) {
                    LzhLogger.d(TAG, "最終更新日時設定エラー", e);
                    // 日時設定の失敗は致命的ではないので続行
                }
            }
            
            long duration = System.currentTimeMillis() - startTime;
            LzhLogger.d(TAG, "単一エントリ解凍完了: " + entry.getFileName() + " (" + duration + "ms)");
            
            if (duration > PERFORMANCE_LOG_THRESHOLD_MS) {
                LzhLogger.logPerformance(TAG, "単一エントリ解凍", duration, 
                    "ファイル: " + entry.getFileName() + ", サイズ: " + entry.getOriginalSize() + " bytes");
            }
            
        } catch (LzhException e) {
            // LzhExceptionはそのまま再スロー
            throw e;
        } catch (Exception e) {
            LzhLogger.e(TAG, "予期しないエラー", e);
            throw new LzhException("Unexpected error during single entry extraction", context, e);
        }
    }
    
    /**
     * エントリデータをスキップ
     * @param inputStream 入力ストリーム
     * @param bytesToSkip スキップするバイト数
     * @throws IOException I/Oエラー
     */
    private void skipEntryData(InputStream inputStream, long bytesToSkip) throws IOException {
        long totalSkipped = 0;
        byte[] buffer = new byte[8192];
        
        while (totalSkipped < bytesToSkip) {
            long remaining = bytesToSkip - totalSkipped;
            int toRead = (int) Math.min(buffer.length, remaining);
            int bytesRead = inputStream.read(buffer, 0, toRead);
            
            if (bytesRead == -1) {
                break;
            }
            totalSkipped += bytesRead;
        }
    }
    
    /**
     * アーカイブ情報を解析（ヘッダーのみ読み取り、データはスキップ）
     * @param inputStream 入力ストリーム
     * @param context エラーコンテキスト
     * @return アーカイブ情報
     * @throws IOException I/Oエラー
     * @throws LzhException 解析エラー
     */
    private LzhArchive parseArchiveInfo(InputStream inputStream, ErrorContext context) throws IOException, LzhException {
        LzhArchive archive = new LzhArchive();
        LzhEntry entry;
        int entryCount = 0;
        long totalSize = 0;
        
        LzhLogger.i(TAG, "アーカイブ情報解析開始");
        
        try {
            // アーカイブ内の各エントリのヘッダーを順次解析
            while ((entry = LzhHeader.parseHeader(inputStream)) != null) {
                entryCount++;
                
                LzhLogger.d(TAG, "エントリ解析 " + entryCount + ": " + entry.getFileName() + 
                           " (" + entry.getCompressionMethod() + ", " + entry.getOriginalSize() + " bytes)");
                
                // エントリをアーカイブに追加
                archive.addEntry(entry);
                totalSize += entry.getOriginalSize();
                
                // 圧縮データ部分をスキップ（ヘッダー情報のみ必要）
                try {
                    skipEntryData(inputStream, entry.getCompressedSize());
                } catch (IOException e) {
                    LzhLogger.w(TAG, "エントリデータスキップ失敗: " + entry.getFileName(), e);
                    throw CorruptedArchiveException.forIncompleteData(
                        entry.getCompressedSize(), 0, 
                        context.copy().withFileName(entry.getFileName()));
                }
            }
            
            if (entryCount == 0) {
                LzhLogger.e(TAG, "アーカイブに有効なエントリが見つかりません");
                throw InvalidArchiveException.forEmptyArchive(context);
            }
            
            LzhLogger.i(TAG, "アーカイブ情報解析完了 - エントリ数: " + entryCount + 
                       ", 総サイズ: " + totalSize + " bytes");
            
            return archive;
            
        } catch (IOException e) {
            LzhLogger.e(TAG, "アーカイブ解析中にI/Oエラー", e);
            throw CorruptedArchiveException.forDataIntegrityError("Archive parsing failed", context);
        }
    }
    
    /**
     * 制限付き入力ストリーム - 指定されたバイト数のみ読み取り可能
     */
    private static class LimitedInputStream extends InputStream {
        private final InputStream source;
        private long remaining;
        
        public LimitedInputStream(InputStream source, long limit) {
            this.source = source;
            this.remaining = limit;
        }
        
        @Override
        public int read() throws IOException {
            if (remaining <= 0) {
                return -1;
            }
            int result = source.read();
            if (result != -1) {
                remaining--;
            }
            return result;
        }
        
        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if (remaining <= 0) {
                return -1;
            }
            int toRead = (int) Math.min(len, remaining);
            int bytesRead = source.read(b, off, toRead);
            if (bytesRead > 0) {
                remaining -= bytesRead;
            }
            return bytesRead;
        }
        
        @Override
        public long skip(long n) throws IOException {
            long toSkip = Math.min(n, remaining);
            long skipped = source.skip(toSkip);
            remaining -= skipped;
            return skipped;
        }
        
        @Override
        public int available() throws IOException {
            return (int) Math.min(source.available(), remaining);
        }
    }
}