# Consumer ProGuard rules for LZH Library
# These rules will be applied to projects that use this library
# 要件 8.1, 8.2, 8.3, 8.4 に対応

# ===== 必須API保護 =====
# メインAPIクラス - アプリケーションが直接使用
-keep public class com.example.lzh.LzhExtractor {
    public *;
}

# ===== モデルクラス保護 =====
# アプリケーションが参照する可能性があるモデルクラス
-keep public class com.example.lzh.model.LzhArchive {
    public *;
}

-keep public class com.example.lzh.model.LzhEntry {
    public *;
}

# ===== 例外クラス保護 =====
# アプリケーションがキャッチする可能性がある例外クラス
-keep public class com.example.lzh.exception.LzhException {
    public *;
    public <init>(...);
}

-keep public class com.example.lzh.exception.InvalidArchiveException {
    public *;
    public <init>(...);
}

-keep public class com.example.lzh.exception.CorruptedArchiveException {
    public *;
    public <init>(...);
}

-keep public class com.example.lzh.exception.UnsupportedMethodException {
    public *;
    public <init>(...);
}

-keep public class com.example.lzh.exception.EncodingException {
    public *;
    public <init>(...);
}

-keep public class com.example.lzh.exception.FileNotFoundException {
    public *;
    public <init>(...);
}

# ===== Android互換性クラス =====
# アプリケーションが使用する可能性があるユーティリティクラス
-keep public class com.example.lzh.util.AndroidCompatibility {
    public *;
}

-keep public class com.example.lzh.util.AndroidCompatibility$StorageInfo {
    public *;
}

-keep public class com.example.lzh.util.AndroidCompatibility$MemoryInfo {
    public *;
}

# Android互換性検証クラス
-keep public class com.example.lzh.util.AndroidCompatibilityVerifier {
    public *;
}

-keep public class com.example.lzh.util.AndroidCompatibilityVerifier$VerificationResult {
    public *;
}

# ===== エラーコンテキスト =====
# デバッグ情報として使用される可能性
-keep public class com.example.lzh.util.ErrorContext {
    public *;
}

# ===== 属性保持 =====
# 例外のスタックトレースを保持
-keepattributes SourceFile,LineNumberTable,Exceptions

# ===== 警告抑制 =====
# ライブラリ内部の実装に関する警告を抑制
-dontwarn com.example.lzh.decompressor.**
-dontwarn com.example.lzh.parser.**
-dontwarn com.example.lzh.util.LzhLogger
-dontwarn com.example.lzh.util.FileManager
-dontwarn com.example.lzh.util.EncodingDetector