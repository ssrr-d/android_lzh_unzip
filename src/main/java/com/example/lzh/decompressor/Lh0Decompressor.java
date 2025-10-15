package com.example.lzh.decompressor;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import com.example.lzh.exception.CorruptedArchiveException;
import com.example.lzh.util.LzhLogger;
import com.example.lzh.util.ErrorContext;

/**
 * LH0（無圧縮）解凍器
 * 圧縮されていないデータを直接コピーする
 * 包括的なエラーハンドリングと詳細なログ出力機能を含む
 * 要件 7.1, 7.2, 7.3, 7.4 に対応
 */
public class Lh0Decompressor extends LzhDecompressor {
    
    private static final String TAG = "Lh0Decompressor";
    
    /**
     * LH0解凍処理（無圧縮データの直接コピー）
     * ストリーミング処理によりメモリ効率的にデータを転送する
     * 
     * @param input 圧縮データの入力ストリーム
     * @param output 解凍データの出力ストリーム
     * @param compressedSize 圧縮後サイズ（LH0では元サイズと同じ）
     * @param originalSize 元のサイズ
     * @throws IOException I/Oエラー
     */
    @Override
    public void decompress(InputStream input, OutputStream output, 
                          long compressedSize, long originalSize) throws IOException {
        
        ErrorContext context = new ErrorContext("LH0解凍")
            .withCompressionMethod("-lh0-")
            .withFileSize(originalSize)
            .withData("compressedSize", compressedSize);
        
        LzhLogger.d(TAG, "LH0解凍開始: " + originalSize + " bytes");
        long startTime = System.currentTimeMillis();
        
        try {
            // LH0では圧縮後サイズと元サイズが同じであることを確認
            if (compressedSize != originalSize) {
                LzhLogger.logDataIntegrityError(TAG, "LH0サイズ整合性", originalSize, compressedSize, 
                    "LH0では圧縮サイズと元サイズが同じである必要があります");
                throw CorruptedArchiveException.forDataIntegrityError(
                    "Invalid LH0 data: compressed size (" + compressedSize + 
                    ") does not match original size (" + originalSize + ")", context);
            }
            
            // データサイズが0の場合は何もしない
            if (originalSize == 0) {
                LzhLogger.d(TAG, "LH0解凍: 空ファイル");
                return;
            }
            
            // ストリーミング処理でデータをコピー
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            long totalCopied = 0;
            long lastProgressLog = 0;
            
            while (totalCopied < originalSize) {
                // 残りのデータサイズを計算
                long remaining = originalSize - totalCopied;
                int bytesToRead = (int) Math.min(buffer.length, remaining);
                
                try {
                    // データを読み取り
                    int bytesRead = readFully(input, buffer, 0, bytesToRead);
                    if (bytesRead <= 0) {
                        LzhLogger.logDataIntegrityError(TAG, "読み取りバイト数", bytesToRead, bytesRead, 
                            "ストリーム終端に予期せず到達");
                        throw CorruptedArchiveException.forIncompleteData(originalSize, totalCopied, context);
                    }
                    
                    // データを出力
                    output.write(buffer, 0, bytesRead);
                    totalCopied += bytesRead;
                    
                    // 進捗ログ（大きなファイルの場合）
                    if (originalSize > 1024 * 1024 && totalCopied - lastProgressLog > 1024 * 1024) {
                        LzhLogger.logProgress(TAG, "LH0解凍", totalCopied, originalSize, "bytes");
                        lastProgressLog = totalCopied;
                    }
                    
                } catch (IOException e) {
                    LzhLogger.logExceptionDetails(TAG, "LH0データ読み取り", e);
                    context.withProcessedBytes(totalCopied);
                    throw CorruptedArchiveException.forDecompressionError(
                        "Failed to read LH0 data at position " + totalCopied, context);
                }
            }
            
            // 出力ストリームをフラッシュ
            try {
                output.flush();
            } catch (IOException e) {
                LzhLogger.logExceptionDetails(TAG, "LH0出力フラッシュ", e);
                throw new IOException("Failed to flush output stream", e);
            }
            
            long duration = System.currentTimeMillis() - startTime;
            LzhLogger.d(TAG, "LH0解凍完了: " + totalCopied + " bytes, " + duration + "ms");
            
            if (duration > 1000) { // 1秒以上かかった場合はパフォーマンスログ
                LzhLogger.logPerformance(TAG, "LH0解凍", duration, 
                    "サイズ: " + totalCopied + " bytes, スループット: " + 
                    (totalCopied * 1000 / duration) + " bytes/sec");
            }
            
        } catch (CorruptedArchiveException e) {
            throw e;
        } catch (Exception e) {
            LzhLogger.logExceptionDetails(TAG, "LH0解凍", e);
            throw new IOException("Unexpected error during LH0 decompression", e);
        }
    }
}