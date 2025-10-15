package com.example.lzh.decompressor;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import com.example.lzh.exception.UnsupportedMethodException;
import com.example.lzh.exception.CorruptedArchiveException;
import com.example.lzh.util.LzhLogger;
import com.example.lzh.util.ErrorContext;

/**
 * LZH解凍処理の抽象基底クラス
 * 共通的な解凍処理フローを定義し、各圧縮方式の具体的な実装を提供する
 * 包括的なエラーハンドリングと詳細なログ出力機能を含む
 * 要件 7.1, 7.2, 7.3, 7.4 に対応
 */
public abstract class LzhDecompressor {
    
    private static final String TAG = "LzhDecompressor";
    
    /** デフォルトバッファサイズ */
    protected static final int DEFAULT_BUFFER_SIZE = 8192;
    
    /** 最大許可ファイルサイズ（1GB） */
    protected static final long MAX_FILE_SIZE = 1024L * 1024L * 1024L;
    
    /**
     * 解凍処理を実行
     * @param input 圧縮データの入力ストリーム
     * @param output 解凍データの出力ストリーム
     * @param compressedSize 圧縮後サイズ
     * @param originalSize 元のサイズ
     * @throws IOException I/Oエラー
     */
    public abstract void decompress(InputStream input, OutputStream output, 
                                  long compressedSize, long originalSize) throws IOException;
    
    /**
     * 共通的な解凍処理フロー
     * バッファリングされたストリームを使用してメモリ効率的な処理を行う
     * @param input 圧縮データの入力ストリーム
     * @param output 解凍データの出力ストリーム
     * @param compressedSize 圧縮後サイズ
     * @param originalSize 元のサイズ
     * @throws IOException I/Oエラー
     */
    public final void decompressWithBuffering(InputStream input, OutputStream output, 
                                            long compressedSize, long originalSize) throws IOException {
        ErrorContext context = new ErrorContext("バッファリング解凍")
            .withFileSize(originalSize)
            .withData("compressedSize", compressedSize)
            .withData("decompressorType", this.getClass().getSimpleName());
        
        LzhLogger.d(TAG, "バッファリング解凍開始: " + this.getClass().getSimpleName() + 
                   " - 圧縮サイズ: " + compressedSize + ", 元サイズ: " + originalSize);
        
        // 入力検証
        validateDecompressionInput(input, output, compressedSize, originalSize, context);
        
        // バッファリングされたストリームでラップ
        BufferedInputStream bufferedInput = input instanceof BufferedInputStream ? 
            (BufferedInputStream) input : new BufferedInputStream(input, DEFAULT_BUFFER_SIZE);
        BufferedOutputStream bufferedOutput = output instanceof BufferedOutputStream ? 
            (BufferedOutputStream) output : new BufferedOutputStream(output, DEFAULT_BUFFER_SIZE);
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 具体的な解凍処理を実行
            decompress(bufferedInput, bufferedOutput, compressedSize, originalSize);
            
            // 出力バッファをフラッシュ
            bufferedOutput.flush();
            
            long duration = System.currentTimeMillis() - startTime;
            LzhLogger.d(TAG, "バッファリング解凍完了: " + duration + "ms");
            
            if (duration > 5000) { // 5秒以上かかった場合は警告
                LzhLogger.logPerformance(TAG, "バッファリング解凍", duration, 
                    "サイズ: " + originalSize + " bytes");
            }
            
        } catch (IOException e) {
            LzhLogger.logExceptionDetails(TAG, "バッファリング解凍", e);
            throw new CorruptedArchiveException("Decompression failed", context, e);
        } catch (Exception e) {
            LzhLogger.logExceptionDetails(TAG, "バッファリング解凍", e);
            throw new IOException("Unexpected error during decompression", e);
        } finally {
            // バッファリングされたストリームのみフラッシュ（元のストリームはクローズしない）
            try {
                if (bufferedOutput != output) {
                    bufferedOutput.flush();
                }
            } catch (IOException e) {
                LzhLogger.w(TAG, "バッファフラッシュ中にエラーが発生", e);
            }
        }
    }
    
    /**
     * 解凍処理の入力パラメータを検証
     * @param input 入力ストリーム
     * @param output 出力ストリーム
     * @param compressedSize 圧縮後サイズ
     * @param originalSize 元のサイズ
     * @param context エラーコンテキスト
     * @throws IOException 検証エラー
     */
    protected void validateDecompressionInput(InputStream input, OutputStream output, 
                                            long compressedSize, long originalSize, 
                                            ErrorContext context) throws IOException {
        if (input == null) {
            LzhLogger.e(TAG, "入力ストリームがnullです");
            throw new IllegalArgumentException("Input stream cannot be null");
        }
        
        if (output == null) {
            LzhLogger.e(TAG, "出力ストリームがnullです");
            throw new IllegalArgumentException("Output stream cannot be null");
        }
        
        if (compressedSize < 0) {
            LzhLogger.e(TAG, "圧縮サイズが負の値です: " + compressedSize);
            throw new IllegalArgumentException("Compressed size cannot be negative: " + compressedSize);
        }
        
        if (originalSize < 0) {
            LzhLogger.e(TAG, "元サイズが負の値です: " + originalSize);
            throw new IllegalArgumentException("Original size cannot be negative: " + originalSize);
        }
        
        // ファイルサイズの上限チェック
        if (originalSize > MAX_FILE_SIZE) {
            LzhLogger.logSecurityWarning(TAG, "ファイルサイズが上限を超過", 
                "サイズ: " + originalSize + " bytes, 上限: " + MAX_FILE_SIZE + " bytes");
            throw new IOException("File size exceeds maximum allowed size: " + originalSize + " > " + MAX_FILE_SIZE);
        }
        
        // 圧縮率の妥当性チェック（異常に高い圧縮率は攻撃の可能性）
        if (compressedSize > 0 && originalSize > 0) {
            double compressionRatio = (double) originalSize / compressedSize;
            if (compressionRatio > 1000) { // 1000倍以上の圧縮率は異常
                LzhLogger.logSecurityWarning(TAG, "異常に高い圧縮率を検出", 
                    "圧縮率: " + String.format("%.1f", compressionRatio) + ":1");
                LzhLogger.logDataIntegrityError(TAG, "圧縮率", "< 1000:1", 
                    String.format("%.1f:1", compressionRatio), "zip bomb攻撃の可能性");
            }
        }
        
        LzhLogger.d(TAG, "解凍入力検証完了");
    }
    
    /**
     * 圧縮方式に応じた解凍器を作成
     * @param method 圧縮方式（例: "-lh0-", "-lh1-", "-lh5-"）
     * @return 対応する解凍器
     * @throws UnsupportedMethodException サポートされていない圧縮方式
     */
    public static LzhDecompressor createDecompressor(String method) throws UnsupportedMethodException {
        ErrorContext context = new ErrorContext("解凍器作成")
            .withCompressionMethod(method);
        
        LzhLogger.d(TAG, "解凍器作成: " + method);
        
        if (method == null) {
            LzhLogger.e(TAG, "圧縮方式がnullです");
            throw UnsupportedMethodException.forMethod(null, context);
        }
        
        if (method.length() != 5) {
            LzhLogger.e(TAG, "無効な圧縮方式フォーマット: " + method + " (長さ: " + method.length() + ")");
            throw UnsupportedMethodException.forMethod(method, context);
        }
        
        String normalizedMethod = method.toLowerCase();
        
        try {
            switch (normalizedMethod) {
                case "-lh0-":
                    LzhLogger.d(TAG, "LH0解凍器を作成");
                    return new Lh0Decompressor();
                case "-lh1-":
                    LzhLogger.d(TAG, "LH1解凍器を作成");
                    return new Lh1Decompressor();
                case "-lh5-":
                    LzhLogger.d(TAG, "LH5解凍器を作成");
                    return new Lh5Decompressor();
                default:
                    LzhLogger.e(TAG, "サポートされていない圧縮方式: " + method);
                    throw UnsupportedMethodException.forMethod(method, context);
            }
        } catch (Exception e) {
            if (e instanceof UnsupportedMethodException) {
                throw e;
            }
            LzhLogger.logExceptionDetails(TAG, "解凍器作成", e);
            throw new UnsupportedMethodException("Failed to create decompressor for method: " + method, context, e);
        }
    }
    
    /**
     * 指定されたバイト数を読み取り、実際に読み取ったバイト数を返す
     * @param input 入力ストリーム
     * @param buffer 読み取りバッファ
     * @param offset バッファ内のオフセット
     * @param length 読み取るバイト数
     * @return 実際に読み取ったバイト数
     * @throws IOException I/Oエラー
     */
    protected static int readFully(InputStream input, byte[] buffer, int offset, int length) throws IOException {
        if (input == null) {
            throw new IllegalArgumentException("Input stream cannot be null");
        }
        if (buffer == null) {
            throw new IllegalArgumentException("Buffer cannot be null");
        }
        if (offset < 0 || length < 0 || offset + length > buffer.length) {
            throw new IllegalArgumentException("Invalid buffer parameters: offset=" + offset + 
                                             ", length=" + length + ", bufferLength=" + buffer.length);
        }
        
        int totalRead = 0;
        int attempts = 0;
        final int maxAttempts = 1000; // 無限ループ防止
        
        while (totalRead < length && attempts < maxAttempts) {
            attempts++;
            
            try {
                int bytesRead = input.read(buffer, offset + totalRead, length - totalRead);
                if (bytesRead == -1) {
                    // ストリーム終端に到達
                    break;
                }
                if (bytesRead == 0) {
                    // データが利用できない場合は少し待機
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Read operation interrupted", e);
                    }
                    continue;
                }
                totalRead += bytesRead;
            } catch (IOException e) {
                LzhLogger.logExceptionDetails(TAG, "データ読み取り", e);
                throw new IOException("Failed to read data after " + totalRead + " bytes", e);
            }
        }
        
        if (attempts >= maxAttempts) {
            LzhLogger.logDataIntegrityError(TAG, "読み取り試行回数", "< " + maxAttempts, attempts, 
                "無限ループの可能性");
            throw new IOException("Too many read attempts: " + attempts);
        }
        
        return totalRead;
    }
    
    /**
     * 指定されたバイト数をスキップする
     * @param input 入力ストリーム
     * @param bytesToSkip スキップするバイト数
     * @throws IOException I/Oエラー
     */
    protected static void skipBytes(InputStream input, long bytesToSkip) throws IOException {
        if (input == null) {
            throw new IllegalArgumentException("Input stream cannot be null");
        }
        if (bytesToSkip < 0) {
            throw new IllegalArgumentException("Bytes to skip cannot be negative: " + bytesToSkip);
        }
        if (bytesToSkip == 0) {
            return;
        }
        
        long totalSkipped = 0;
        int attempts = 0;
        final int maxAttempts = 10000; // 無限ループ防止
        byte[] skipBuffer = new byte[Math.min(8192, (int) bytesToSkip)];
        
        LzhLogger.d(TAG, "データスキップ開始: " + bytesToSkip + " bytes");
        
        while (totalSkipped < bytesToSkip && attempts < maxAttempts) {
            attempts++;
            
            try {
                long remaining = bytesToSkip - totalSkipped;
                long skipped = input.skip(remaining);
                
                if (skipped > 0) {
                    totalSkipped += skipped;
                } else {
                    // skip()が機能しない場合は読み取りでスキップ
                    int toRead = (int) Math.min(skipBuffer.length, remaining);
                    int bytesRead = input.read(skipBuffer, 0, toRead);
                    
                    if (bytesRead == -1) {
                        // ストリーム終端に到達
                        break;
                    } else if (bytesRead > 0) {
                        totalSkipped += bytesRead;
                    } else {
                        // データが利用できない場合は少し待機
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            throw new IOException("Skip operation interrupted", e);
                        }
                    }
                }
            } catch (IOException e) {
                LzhLogger.logExceptionDetails(TAG, "データスキップ", e);
                throw new IOException("Failed to skip data after " + totalSkipped + " bytes", e);
            }
        }
        
        if (attempts >= maxAttempts) {
            LzhLogger.logDataIntegrityError(TAG, "スキップ試行回数", "< " + maxAttempts, attempts, 
                "無限ループの可能性");
            throw new IOException("Too many skip attempts: " + attempts);
        }
        
        if (totalSkipped < bytesToSkip) {
            LzhLogger.logDataIntegrityError(TAG, "スキップバイト数", bytesToSkip, totalSkipped, 
                "ストリーム終端に到達");
        }
        
        LzhLogger.d(TAG, "データスキップ完了: " + totalSkipped + "/" + bytesToSkip + " bytes");
    }
}