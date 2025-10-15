package com.example.lzh.decompressor;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import com.example.lzh.exception.CorruptedArchiveException;
import com.example.lzh.util.LzhLogger;
import com.example.lzh.util.ErrorContext;

/**
 * LH1（LZSS）解凍器
 * LZSS（Lempel-Ziv-Storer-Szymanski）アルゴリズムによる解凍処理
 * 包括的なエラーハンドリングと詳細なログ出力機能を含む
 * 要件 7.1, 7.2, 7.3, 7.4 に対応
 */
public class Lh1Decompressor extends LzhDecompressor {
    
    private static final String TAG = "Lh1Decompressor";
    
    /** 辞書サイズ（4KB） */
    private static final int DICTIONARY_SIZE = 4096;
    
    /** 最大マッチ長 */
    private static final int MAX_MATCH_LENGTH = 18;
    
    /** 最小マッチ長 */
    private static final int MIN_MATCH_LENGTH = 3;
    
    /** 辞書の初期化値 */
    private static final byte DICTIONARY_INIT_VALUE = 0x20; // スペース文字
    
    /**
     * LH1解凍処理（LZSS解凍）
     * 辞書ベースの解凍処理とバッファ管理を行う
     * 
     * @param input 圧縮データの入力ストリーム
     * @param output 解凍データの出力ストリーム
     * @param compressedSize 圧縮後サイズ
     * @param originalSize 元のサイズ
     * @throws IOException I/Oエラー
     */
    @Override
    public void decompress(InputStream input, OutputStream output, 
                          long compressedSize, long originalSize) throws IOException {
        
        ErrorContext context = new ErrorContext("LH1解凍")
            .withCompressionMethod("-lh1-")
            .withFileSize(originalSize)
            .withData("compressedSize", compressedSize);
        
        LzhLogger.d(TAG, "LH1解凍開始: 圧縮サイズ=" + compressedSize + ", 元サイズ=" + originalSize);
        long startTime = System.currentTimeMillis();
        
        try {
            // データサイズが0の場合は何もしない
            if (originalSize == 0) {
                LzhLogger.d(TAG, "LH1解凍: 空ファイル");
                return;
            }
            
            // 辞書バッファを初期化
            byte[] dictionary = new byte[DICTIONARY_SIZE];
            for (int i = 0; i < DICTIONARY_SIZE; i++) {
                dictionary[i] = DICTIONARY_INIT_VALUE;
            }
            
            int dictPos = DICTIONARY_SIZE - MAX_MATCH_LENGTH; // 辞書の現在位置
            long outputCount = 0; // 出力したバイト数
            long lastProgressLog = 0;
            int flagByteCount = 0;
            
            // 圧縮データを処理
            while (outputCount < originalSize) {
                // フラグバイトを読み取り
                int flags;
                try {
                    flags = input.read();
                    if (flags == -1) {
                        LzhLogger.logDataIntegrityError(TAG, "フラグバイト", "有効な値", "EOF", 
                            "出力位置: " + outputCount + "/" + originalSize);
                        throw CorruptedArchiveException.forIncompleteData(originalSize, outputCount, context);
                    }
                    flagByteCount++;
                } catch (IOException e) {
                    LzhLogger.logExceptionDetails(TAG, "LH1フラグバイト読み取り", e);
                    context.withProcessedBytes(outputCount);
                    throw CorruptedArchiveException.forDecompressionError(
                        "Failed to read flag byte at position " + outputCount, context);
                }
                
                // 8ビットのフラグを処理
                for (int i = 0; i < 8 && outputCount < originalSize; i++) {
                    try {
                        if ((flags & (1 << i)) != 0) {
                            // リテラルバイト
                            int literal = input.read();
                            if (literal == -1) {
                                LzhLogger.logDataIntegrityError(TAG, "リテラルバイト", "有効な値", "EOF", 
                                    "出力位置: " + outputCount + "/" + originalSize);
                                throw CorruptedArchiveException.forIncompleteData(originalSize, outputCount, context);
                            }
                            
                            // 辞書に追加し、出力
                            dictionary[dictPos] = (byte) literal;
                            output.write(literal);
                            dictPos = (dictPos + 1) % DICTIONARY_SIZE;
                            outputCount++;
                            
                        } else {
                            // 参照（オフセット + 長さ）
                            int byte1 = input.read();
                            int byte2 = input.read();
                            if (byte1 == -1 || byte2 == -1) {
                                LzhLogger.logDataIntegrityError(TAG, "参照バイト", "2バイト", 
                                    (byte1 == -1 ? "0" : "1") + "バイト", 
                                    "出力位置: " + outputCount + "/" + originalSize);
                                throw CorruptedArchiveException.forIncompleteData(originalSize, outputCount, context);
                            }
                            
                            // オフセットと長さを抽出
                            int offset = byte1 | ((byte2 & 0xF0) << 4);
                            int length = (byte2 & 0x0F) + MIN_MATCH_LENGTH;
                            
                            // 参照の妥当性をチェック
                            if (offset >= DICTIONARY_SIZE) {
                                LzhLogger.logDataIntegrityError(TAG, "辞書オフセット", 
                                    "< " + DICTIONARY_SIZE, offset, "無効な辞書参照");
                                throw CorruptedArchiveException.forDataIntegrityError(
                                    "Invalid dictionary offset: " + offset, context);
                            }
                            
                            if (length > MAX_MATCH_LENGTH) {
                                LzhLogger.logDataIntegrityError(TAG, "マッチ長", 
                                    "<= " + MAX_MATCH_LENGTH, length, "無効なマッチ長");
                                throw CorruptedArchiveException.forDataIntegrityError(
                                    "Invalid match length: " + length, context);
                            }
                            
                            // 参照データをコピー
                            for (int j = 0; j < length && outputCount < originalSize; j++) {
                                byte b = dictionary[offset];
                                dictionary[dictPos] = b;
                                output.write(b);
                                
                                offset = (offset + 1) % DICTIONARY_SIZE;
                                dictPos = (dictPos + 1) % DICTIONARY_SIZE;
                                outputCount++;
                            }
                        }
                        
                        // 進捗ログ（大きなファイルの場合）
                        if (originalSize > 1024 * 1024 && outputCount - lastProgressLog > 1024 * 1024) {
                            LzhLogger.logProgress(TAG, "LH1解凍", outputCount, originalSize, "bytes");
                            lastProgressLog = outputCount;
                        }
                        
                    } catch (IOException e) {
                        LzhLogger.logExceptionDetails(TAG, "LH1データ処理", e);
                        context.withProcessedBytes(outputCount)
                               .withData("flagByte", String.format("0x%02X", flags))
                               .withData("bitPosition", i);
                        throw CorruptedArchiveException.forDecompressionError(
                            "Failed to process LH1 data at bit " + i + " of flag byte " + flagByteCount, context);
                    }
                }
            }
            
            // 出力ストリームをフラッシュ
            try {
                output.flush();
            } catch (IOException e) {
                LzhLogger.logExceptionDetails(TAG, "LH1出力フラッシュ", e);
                throw new IOException("Failed to flush output stream", e);
            }
            
            long duration = System.currentTimeMillis() - startTime;
            LzhLogger.d(TAG, "LH1解凍完了: " + outputCount + " bytes, " + duration + "ms, " + 
                       flagByteCount + " flag bytes processed");
            
            if (duration > 1000) { // 1秒以上かかった場合はパフォーマンスログ
                LzhLogger.logPerformance(TAG, "LH1解凍", duration, 
                    "サイズ: " + outputCount + " bytes, スループット: " + 
                    (outputCount * 1000 / duration) + " bytes/sec");
            }
            
        } catch (CorruptedArchiveException e) {
            throw e;
        } catch (Exception e) {
            LzhLogger.logExceptionDetails(TAG, "LH1解凍", e);
            throw new IOException("Unexpected error during LH1 decompression", e);
        }
        
        // 出力ストリームをフラッシュ
        output.flush();
    }
}