package com.example.lzh.decompressor;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

/**
 * LH5（LZSS改良版）解凍器
 * 改良版LZSSアルゴリズムとハフマン符号化による解凍処理
 */
public class Lh5Decompressor extends LzhDecompressor {
    
    /** 辞書サイズ（8KB） */
    private static final int DICTIONARY_SIZE = 8192;
    
    /** 最大マッチ長 */
    private static final int MAX_MATCH_LENGTH = 256;
    
    /** 最小マッチ長 */
    private static final int MIN_MATCH_LENGTH = 3;
    
    /** 辞書の初期化値 */
    private static final byte DICTIONARY_INIT_VALUE = 0x20; // スペース文字
    
    /** ハフマンテーブルのサイズ */
    private static final int HUFFMAN_TABLE_SIZE = 314;
    
    /** ビット読み取り用のバッファ */
    private int bitBuffer = 0;
    private int bitCount = 0;
    
    /**
     * LH5解凍処理（改良版LZSS + ハフマン符号化）
     * ハフマン符号化の処理を含む解凍機能
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
        
        // データサイズが0の場合は何もしない
        if (originalSize == 0) {
            return;
        }
        
        // 辞書バッファを初期化
        byte[] dictionary = new byte[DICTIONARY_SIZE];
        for (int i = 0; i < DICTIONARY_SIZE; i++) {
            dictionary[i] = DICTIONARY_INIT_VALUE;
        }
        
        // ビットバッファを初期化
        bitBuffer = 0;
        bitCount = 0;
        
        int dictPos = DICTIONARY_SIZE - MAX_MATCH_LENGTH; // 辞書の現在位置
        long outputCount = 0; // 出力したバイト数
        
        // ハフマンテーブルを読み込み
        int[] huffmanTable = readHuffmanTable(input);
        
        // 圧縮データを処理
        while (outputCount < originalSize) {
            // ハフマン符号を読み取り
            int code = readHuffmanCode(input, huffmanTable);
            
            if (code < 256) {
                // リテラルバイト
                dictionary[dictPos] = (byte) code;
                output.write(code);
                dictPos = (dictPos + 1) % DICTIONARY_SIZE;
                outputCount++;
                
            } else {
                // 参照（長さ + オフセット）
                int length = code - 256 + MIN_MATCH_LENGTH;
                
                // オフセットを読み取り
                int offset = readOffset(input, length);
                
                // 参照データをコピー
                for (int j = 0; j < length && outputCount < originalSize; j++) {
                    byte b = dictionary[(dictPos - offset + j) % DICTIONARY_SIZE];
                    dictionary[dictPos] = b;
                    output.write(b);
                    
                    dictPos = (dictPos + 1) % DICTIONARY_SIZE;
                    outputCount++;
                }
            }
        }
        
        // 出力ストリームをフラッシュ
        output.flush();
    }
    
    /**
     * ハフマンテーブルを読み込む
     * @param input 入力ストリーム
     * @return ハフマンテーブル
     * @throws IOException I/Oエラー
     */
    private int[] readHuffmanTable(InputStream input) throws IOException {
        int[] table = new int[HUFFMAN_TABLE_SIZE];
        
        // 簡略化されたハフマンテーブル読み込み
        // 実際の実装では、より複雑なハフマンテーブル構築が必要
        for (int i = 0; i < HUFFMAN_TABLE_SIZE; i++) {
            table[i] = i; // 仮の実装
        }
        
        return table;
    }
    
    /**
     * ハフマン符号を読み取る
     * @param input 入力ストリーム
     * @param huffmanTable ハフマンテーブル
     * @return デコードされた値
     * @throws IOException I/Oエラー
     */
    private int readHuffmanCode(InputStream input, int[] huffmanTable) throws IOException {
        // 簡略化されたハフマンデコード
        // 実際の実装では、ビット単位でのハフマンデコードが必要
        int bits = readBits(input, 8);
        return bits < huffmanTable.length ? huffmanTable[bits] : bits;
    }
    
    /**
     * オフセットを読み取る
     * @param input 入力ストリーム
     * @param length マッチ長
     * @return オフセット値
     * @throws IOException I/Oエラー
     */
    private int readOffset(InputStream input, int length) throws IOException {
        // 長さに応じてオフセットのビット数を決定
        int offsetBits = 13; // LH5では通常13ビット
        if (length == MIN_MATCH_LENGTH) {
            offsetBits = 8;
        } else if (length < 8) {
            offsetBits = 10;
        }
        
        return readBits(input, offsetBits);
    }
    
    /**
     * 指定されたビット数を読み取る
     * @param input 入力ストリーム
     * @param bits 読み取るビット数
     * @return 読み取った値
     * @throws IOException I/Oエラー
     */
    private int readBits(InputStream input, int bits) throws IOException {
        int result = 0;
        
        for (int i = 0; i < bits; i++) {
            if (bitCount == 0) {
                int nextByte = input.read();
                if (nextByte == -1) {
                    throw new IOException("Unexpected end of stream while reading bits");
                }
                bitBuffer = nextByte;
                bitCount = 8;
            }
            
            result = (result << 1) | (bitBuffer & 1);
            bitBuffer >>= 1;
            bitCount--;
        }
        
        return result;
    }
}