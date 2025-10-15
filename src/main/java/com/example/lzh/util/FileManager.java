package com.example.lzh.util;

import android.content.Context;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * ファイル出力管理ユーティリティ
 */
public class FileManager {
    
    private static final int BUFFER_SIZE = 8192;
    
    /**
     * ディレクトリを作成
     * @param outputDir 出力ディレクトリ
     * @throws IOException I/Oエラー
     */
    public static void createDirectories(File outputDir) throws IOException {
        if (outputDir == null) {
            throw new IllegalArgumentException("Output directory cannot be null");
        }
        
        if (!outputDir.exists()) {
            boolean created = outputDir.mkdirs();
            if (!created && !outputDir.exists()) {
                throw new IOException("Failed to create directory: " + outputDir.getAbsolutePath());
            }
        }
        
        if (!outputDir.isDirectory()) {
            throw new IOException("Output path exists but is not a directory: " + outputDir.getAbsolutePath());
        }
        
        if (!outputDir.canWrite()) {
            throw new IOException("Cannot write to directory: " + outputDir.getAbsolutePath());
        }
    }
    
    /**
     * ファイルを書き込み
     * @param outputFile 出力ファイル
     * @param input 入力ストリーム
     * @throws IOException I/Oエラー
     */
    public static void writeFile(File outputFile, InputStream input) throws IOException {
        if (outputFile == null) {
            throw new IllegalArgumentException("Output file cannot be null");
        }
        if (input == null) {
            throw new IllegalArgumentException("Input stream cannot be null");
        }
        
        // 親ディレクトリが存在しない場合は作成
        File parentDir = outputFile.getParentFile();
        if (parentDir != null) {
            createDirectories(parentDir);
        }
        
        // ファイルを書き込み
        try (FileOutputStream output = new FileOutputStream(outputFile)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
            output.flush();
        }
    }
    
    /**
     * 出力パスが有効かチェック（パストラバーサル攻撃防止）
     * @param outputDir 出力ディレクトリ
     * @param fileName ファイル名
     * @return 有効な場合true
     */
    public static boolean isValidOutputPath(File outputDir, String fileName) {
        if (outputDir == null || fileName == null) {
            return false;
        }
        
        // 空文字列や不正な文字をチェック
        if (fileName.trim().isEmpty()) {
            return false;
        }
        
        // 危険な文字列パターンをチェック
        if (fileName.contains("..") || fileName.contains("./") || fileName.contains(".\\")) {
            return false;
        }
        
        // 絶対パスの場合は拒否
        if (fileName.startsWith("/") || fileName.startsWith("\\") || 
            (fileName.length() > 1 && fileName.charAt(1) == ':')) {
            return false;
        }
        
        try {
            // 正規化されたパスを取得
            Path outputDirPath = outputDir.toPath().normalize();
            Path filePath = outputDirPath.resolve(fileName).normalize();
            
            // 出力ディレクトリ内に収まっているかチェック
            return filePath.startsWith(outputDirPath);
        } catch (Exception e) {
            // パス解決でエラーが発生した場合は無効とする
            return false;
        }
    }
    
    /**
     * 安全なファイルパスを作成
     * @param outputDir 出力ディレクトリ
     * @param fileName ファイル名
     * @return 安全なファイルパス
     * @throws IOException 無効なパスの場合
     */
    public static File createSafeFilePath(File outputDir, String fileName) throws IOException {
        if (!isValidOutputPath(outputDir, fileName)) {
            throw new IOException("Invalid or unsafe file path: " + fileName);
        }
        
        return new File(outputDir, fileName);
    }
    
    /**
     * Android内部ストレージにディレクトリを作成
     * @param context Androidコンテキスト
     * @param subdirectory サブディレクトリ名（nullの場合はfiles直下）
     * @return 作成されたディレクトリ
     * @throws IOException I/Oエラー
     */
    public static File createInternalStorageDirectory(Context context, String subdirectory) throws IOException {
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }
        
        File filesDir = context.getFilesDir();
        if (filesDir == null) {
            throw new IOException("Cannot access internal storage files directory");
        }
        
        File outputDir;
        if (subdirectory != null && !subdirectory.trim().isEmpty()) {
            // サブディレクトリ名の安全性をチェック
            if (!isValidDirectoryName(subdirectory)) {
                throw new IOException("Invalid subdirectory name: " + subdirectory);
            }
            outputDir = new File(filesDir, subdirectory);
        } else {
            outputDir = filesDir;
        }
        
        createDirectories(outputDir);
        return outputDir;
    }
    
    /**
     * Android内部ストレージにファイルを書き込み
     * @param context Androidコンテキスト
     * @param fileName ファイル名
     * @param input 入力ストリーム
     * @throws IOException I/Oエラー
     */
    public static void writeToInternalStorage(Context context, String fileName, InputStream input) throws IOException {
        writeToInternalStorage(context, null, fileName, input);
    }
    
    /**
     * Android内部ストレージの指定サブディレクトリにファイルを書き込み
     * @param context Androidコンテキスト
     * @param subdirectory サブディレクトリ名（nullの場合はfiles直下）
     * @param fileName ファイル名
     * @param input 入力ストリーム
     * @throws IOException I/Oエラー
     */
    public static void writeToInternalStorage(Context context, String subdirectory, String fileName, InputStream input) throws IOException {
        File outputDir = createInternalStorageDirectory(context, subdirectory);
        File outputFile = createSafeFilePath(outputDir, fileName);
        writeFile(outputFile, input);
    }
    
    /**
     * Android内部ストレージの使用可能容量をチェック
     * @param context Androidコンテキスト
     * @return 使用可能バイト数
     */
    public static long getAvailableInternalStorageSpace(Context context) {
        if (context == null) {
            return 0;
        }
        
        File filesDir = context.getFilesDir();
        if (filesDir == null) {
            return 0;
        }
        
        return filesDir.getUsableSpace();
    }
    
    /**
     * ディレクトリ名が有効かチェック
     * @param directoryName ディレクトリ名
     * @return 有効な場合true
     */
    private static boolean isValidDirectoryName(String directoryName) {
        if (directoryName == null || directoryName.trim().isEmpty()) {
            return false;
        }
        
        // 危険な文字列パターンをチェック
        if (directoryName.contains("..") || directoryName.contains("/") || 
            directoryName.contains("\\") || directoryName.contains(":")) {
            return false;
        }
        
        // 予約語をチェック（Windows）
        String[] reservedNames = {"CON", "PRN", "AUX", "NUL", "COM1", "COM2", "COM3", "COM4", 
                                 "COM5", "COM6", "COM7", "COM8", "COM9", "LPT1", "LPT2", 
                                 "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"};
        
        String upperName = directoryName.toUpperCase();
        for (String reserved : reservedNames) {
            if (upperName.equals(reserved)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Android内部ストレージ内のファイルを削除
     * @param context Androidコンテキスト
     * @param fileName ファイル名
     * @return 削除成功の場合true
     */
    public static boolean deleteFromInternalStorage(Context context, String fileName) {
        return deleteFromInternalStorage(context, null, fileName);
    }
    
    /**
     * Android内部ストレージの指定サブディレクトリ内のファイルを削除
     * @param context Androidコンテキスト
     * @param subdirectory サブディレクトリ名（nullの場合はfiles直下）
     * @param fileName ファイル名
     * @return 削除成功の場合true
     */
    public static boolean deleteFromInternalStorage(Context context, String subdirectory, String fileName) {
        if (context == null || fileName == null) {
            return false;
        }
        
        try {
            File filesDir = context.getFilesDir();
            if (filesDir == null) {
                return false;
            }
            
            File targetDir;
            if (subdirectory != null && !subdirectory.trim().isEmpty()) {
                if (!isValidDirectoryName(subdirectory)) {
                    return false;
                }
                targetDir = new File(filesDir, subdirectory);
            } else {
                targetDir = filesDir;
            }
            
            if (!isValidOutputPath(targetDir, fileName)) {
                return false;
            }
            
            File targetFile = new File(targetDir, fileName);
            return targetFile.exists() && targetFile.delete();
        } catch (Exception e) {
            return false;
        }
    }
}