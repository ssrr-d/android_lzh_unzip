package com.example.lzh.util;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;
import com.example.lzh.exception.EncodingException;

/**
 * 文字エンコーディング検出ユーティリティ
 * UTF-8とShift_JISの自動検出機能を提供し、適切なファイル名デコード処理を行う
 */
public class EncodingDetector {
    
    private static final Logger logger = Logger.getLogger(EncodingDetector.class.getName());
    
    private static final String[] SUPPORTED_ENCODINGS = {"UTF-8", "Shift_JIS"};
    private static final String DEFAULT_ENCODING = "UTF-8";
    
    // 検出結果を保持するクラス
    public static class DetectionResult {
        private final String encoding;
        private final boolean isConfident;
        private final String warning;
        
        public DetectionResult(String encoding, boolean isConfident, String warning) {
            this.encoding = encoding;
            this.isConfident = isConfident;
            this.warning = warning;
        }
        
        public String getEncoding() { return encoding; }
        public boolean isConfident() { return isConfident; }
        public String getWarning() { return warning; }
    }
    
    /**
     * バイト配列からエンコーディングを検出
     * @param data バイト配列
     * @return 検出されたエンコーディング
     */
    public static String detectEncoding(byte[] data) {
        DetectionResult result = detectEncodingWithConfidence(data);
        return result.getEncoding();
    }
    
    /**
     * バイト配列からエンコーディングを検出し、信頼度と警告も返す
     * @param data バイト配列
     * @return 検出結果（エンコーディング、信頼度、警告）
     */
    public static DetectionResult detectEncodingWithConfidence(byte[] data) {
        if (data == null || data.length == 0) {
            return new DetectionResult(DEFAULT_ENCODING, true, null);
        }
        
        // ASCII文字のみかチェック（最も確実）
        if (isAsciiOnly(data)) {
            return new DetectionResult("UTF-8", true, null); // ASCIIはUTF-8と互換
        }
        
        // UTF-8の妥当性をチェック
        boolean isValidUtf8 = isValidUtf8(data);
        boolean hasUtf8Sequences = hasMultiByteUtf8Sequences(data);
        
        // Shift_JISの妥当性をチェック
        boolean isValidShiftJis = isValidShiftJis(data);
        boolean hasShiftJisSequences = containsJapaneseCharacters(data);
        
        // 検出ロジック
        if (isValidUtf8 && hasUtf8Sequences && !hasShiftJisSequences) {
            // UTF-8として明確に識別可能
            return new DetectionResult("UTF-8", true, null);
        }
        
        if (isValidShiftJis && hasShiftJisSequences && !hasUtf8Sequences) {
            // Shift_JISとして明確に識別可能
            return new DetectionResult("Shift_JIS", true, null);
        }
        
        if (isValidUtf8 && isValidShiftJis) {
            // 両方で有効な場合は、より可能性の高い方を選択
            if (hasUtf8Sequences) {
                return new DetectionResult("UTF-8", false, 
                    "文字エンコーディングの自動検出が曖昧です。UTF-8として解釈しますが、文字化けの可能性があります。");
            } else if (hasShiftJisSequences) {
                return new DetectionResult("Shift_JIS", false,
                    "文字エンコーディングの自動検出が曖昧です。Shift_JISとして解釈しますが、文字化けの可能性があります。");
            }
        }
        
        if (isValidUtf8) {
            return new DetectionResult("UTF-8", false,
                "UTF-8として検出されましたが、確実性が低いです。文字化けの可能性があります。");
        }
        
        if (isValidShiftJis) {
            return new DetectionResult("Shift_JIS", false,
                "Shift_JISとして検出されましたが、確実性が低いです。文字化けの可能性があります。");
        }
        
        // どちらでも無効な場合はデフォルトエンコーディングを使用
        return new DetectionResult(DEFAULT_ENCODING, false,
            "文字エンコーディングを自動検出できませんでした。デフォルトエンコーディング(" + DEFAULT_ENCODING + ")を使用します。文字化けの可能性があります。");
    }
    
    /**
     * ファイル名をデコード
     * @param nameBytes ファイル名のバイト配列
     * @return デコードされたファイル名
     */
    public static String decodeFileName(byte[] nameBytes) {
        DecodeResult result = decodeFileNameWithWarning(nameBytes);
        
        // 警告がある場合はログに出力
        if (result.getWarning() != null) {
            logger.warning(result.getWarning());
        }
        
        return result.getDecodedName();
    }
    
    /**
     * ファイル名をデコードし、警告情報も返す
     * @param nameBytes ファイル名のバイト配列
     * @return デコード結果（ファイル名と警告）
     */
    public static DecodeResult decodeFileNameWithWarning(byte[] nameBytes) {
        if (nameBytes == null || nameBytes.length == 0) {
            return new DecodeResult("", null);
        }
        
        // エンコーディングを検出
        DetectionResult detection = detectEncodingWithConfidence(nameBytes);
        String encoding = detection.getEncoding();
        String warning = detection.getWarning();
        
        try {
            String decoded = new String(nameBytes, encoding);
            
            // デコード結果の妥当性をチェック
            if (isValidDecodedString(decoded)) {
                return new DecodeResult(decoded, warning);
            }
            
            // 検出されたエンコーディングで失敗した場合、他のエンコーディングを試行
            for (String fallbackEncoding : SUPPORTED_ENCODINGS) {
                if (!fallbackEncoding.equals(encoding)) {
                    try {
                        decoded = new String(nameBytes, fallbackEncoding);
                        if (isValidDecodedString(decoded)) {
                            String fallbackWarning = "検出されたエンコーディング(" + encoding + ")でのデコードに失敗しました。" +
                                fallbackEncoding + "を使用してデコードしましたが、文字化けの可能性があります。";
                            return new DecodeResult(decoded, fallbackWarning);
                        }
                    } catch (UnsupportedEncodingException e) {
                        // 次のエンコーディングを試行
                    }
                }
            }
            
            // 最後の手段としてISO-8859-1を使用
            decoded = new String(nameBytes, StandardCharsets.ISO_8859_1);
            String fallbackWarning = "すべてのサポートされたエンコーディングでのデコードに失敗しました。" +
                "ISO-8859-1を使用してデコードしましたが、文字化けが発生している可能性があります。";
            return new DecodeResult(decoded, fallbackWarning);
            
        } catch (UnsupportedEncodingException e) {
            // フォールバック
            String decoded = new String(nameBytes, StandardCharsets.ISO_8859_1);
            String fallbackWarning = "エンコーディングエラーが発生しました。ISO-8859-1を使用してデコードしましたが、" +
                "文字化けが発生している可能性があります。";
            return new DecodeResult(decoded, fallbackWarning);
        }
    }
    
    /**
     * デコード結果を保持するクラス
     */
    public static class DecodeResult {
        private final String decodedName;
        private final String warning;
        
        public DecodeResult(String decodedName, String warning) {
            this.decodedName = decodedName;
            this.warning = warning;
        }
        
        public String getDecodedName() { return decodedName; }
        public String getWarning() { return warning; }
    }
    
    /**
     * UTF-8として妥当かチェック
     * @param data バイト配列
     * @return UTF-8として妥当な場合true
     */
    private static boolean isValidUtf8(byte[] data) {
        int i = 0;
        while (i < data.length) {
            int b = data[i] & 0xFF;
            
            if ((b & 0x80) == 0) {
                // ASCII文字 (0xxxxxxx)
                i++;
            } else if ((b & 0xE0) == 0xC0) {
                // 2バイト文字 (110xxxxx 10xxxxxx)
                if (i + 1 >= data.length) return false;
                if ((data[i + 1] & 0xC0) != 0x80) return false;
                i += 2;
            } else if ((b & 0xF0) == 0xE0) {
                // 3バイト文字 (1110xxxx 10xxxxxx 10xxxxxx)
                if (i + 2 >= data.length) return false;
                if ((data[i + 1] & 0xC0) != 0x80) return false;
                if ((data[i + 2] & 0xC0) != 0x80) return false;
                i += 3;
            } else if ((b & 0xF8) == 0xF0) {
                // 4バイト文字 (11110xxx 10xxxxxx 10xxxxxx 10xxxxxx)
                if (i + 3 >= data.length) return false;
                if ((data[i + 1] & 0xC0) != 0x80) return false;
                if ((data[i + 2] & 0xC0) != 0x80) return false;
                if ((data[i + 3] & 0xC0) != 0x80) return false;
                i += 4;
            } else {
                // 無効なUTF-8シーケンス
                return false;
            }
        }
        return true;
    }
    
    /**
     * マルチバイトUTF-8シーケンスが含まれているかチェック
     * @param data バイト配列
     * @return マルチバイトUTF-8シーケンスが含まれている場合true
     */
    private static boolean hasMultiByteUtf8Sequences(byte[] data) {
        for (int i = 0; i < data.length; i++) {
            int b = data[i] & 0xFF;
            if ((b & 0x80) != 0) {
                // 非ASCII文字が見つかった
                return true;
            }
        }
        return false;
    }
    
    /**
     * Shift_JISとして妥当かチェック
     * @param data バイト配列
     * @return Shift_JISとして妥当な場合true
     */
    private static boolean isValidShiftJis(byte[] data) {
        int i = 0;
        while (i < data.length) {
            int b = data[i] & 0xFF;
            
            if (b <= 0x7F) {
                // ASCII文字
                i++;
            } else if (b >= 0xA1 && b <= 0xDF) {
                // 半角カナ
                i++;
            } else if ((b >= 0x81 && b <= 0x9F) || (b >= 0xE0 && b <= 0xFC)) {
                // 2バイト文字の1バイト目
                if (i + 1 >= data.length) return false;
                int b2 = data[i + 1] & 0xFF;
                if (!((b2 >= 0x40 && b2 <= 0x7E) || (b2 >= 0x80 && b2 <= 0xFC))) {
                    return false;
                }
                i += 2;
            } else {
                // 無効なShift_JISバイト
                return false;
            }
        }
        return true;
    }
    
    /**
     * ASCII文字のみかチェック
     * @param data バイト配列
     * @return ASCII文字のみの場合true
     */
    private static boolean isAsciiOnly(byte[] data) {
        for (byte b : data) {
            if ((b & 0x80) != 0) { // 最上位ビットが1の場合は非ASCII
                return false;
            }
        }
        return true;
    }
    
    /**
     * 日本語文字が含まれている可能性をチェック
     * @param data バイト配列
     * @return 日本語文字が含まれている可能性がある場合true
     */
    private static boolean containsJapaneseCharacters(byte[] data) {
        for (int i = 0; i < data.length; i++) {
            int b = data[i] & 0xFF;
            
            // 半角カナの範囲をチェック
            if (b >= 0xA1 && b <= 0xDF) {
                return true;
            }
            
            // Shift_JISの1バイト目の範囲をチェック
            if ((b >= 0x81 && b <= 0x9F) || (b >= 0xE0 && b <= 0xFC)) {
                if (i + 1 < data.length) {
                    int b2 = data[i + 1] & 0xFF;
                    // Shift_JISの2バイト目の範囲をチェック
                    if ((b2 >= 0x40 && b2 <= 0x7E) || (b2 >= 0x80 && b2 <= 0xFC)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    /**
     * デコードされた文字列が妥当かチェック
     * @param decoded デコードされた文字列
     * @return 妥当な場合true
     */
    private static boolean isValidDecodedString(String decoded) {
        if (decoded == null) {
            return false;
        }
        
        // 制御文字や置換文字が含まれていないかチェック
        for (char c : decoded.toCharArray()) {
            if (c == '\uFFFD' || // 置換文字
                (c < 0x20 && c != '\t' && c != '\n' && c != '\r')) { // 制御文字（タブ、改行、復帰以外）
                return false;
            }
        }
        
        return true;
    }
}