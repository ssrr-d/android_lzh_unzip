# LZH Library ProGuard Rules
# 要件 8.1, 8.2, 8.3, 8.4 に対応

# ===== 基本設定 =====
# 最適化を有効にする
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
-dontpreverify

# ===== 公開API保護 =====
# メインAPIクラスを保護
-keep public class com.example.lzh.LzhExtractor {
    public *;
}

# モデルクラスを保護（シリアライゼーション対応）
-keep public class com.example.lzh.model.** {
    public *;
    private *;
}

# 例外クラスを保護
-keep public class com.example.lzh.exception.** {
    public *;
    public <init>(...);
}

# ===== 内部実装保護 =====
# 解凍器の実装を保護
-keep class com.example.lzh.decompressor.** {
    public *;
    protected *;
}

# ユーティリティクラスの公開メソッドを保護
-keep class com.example.lzh.util.** {
    public *;
}

# パーサークラスを保護
-keep class com.example.lzh.parser.** {
    public *;
    protected *;
}

# ===== Android互換性 =====
# Android固有のクラスを保護
-keep class com.example.lzh.util.AndroidCompatibility {
    public *;
}

# Android互換性検証クラスを保護
-keep class com.example.lzh.util.AndroidCompatibilityVerifier {
    public *;
}

-keep class com.example.lzh.util.AndroidCompatibilityVerifier$VerificationResult {
    public *;
}

# ログ関連クラスを保護
-keep class com.example.lzh.util.LzhLogger {
    public *;
}

# ===== リフレクション対応 =====
# エラーコンテキストクラスを保護（リフレクション使用の可能性）
-keep class com.example.lzh.util.ErrorContext {
    public *;
    private *;
}

# ===== 属性保持 =====
# デバッグ情報を保持
-keepattributes SourceFile,LineNumberTable

# 例外情報を保持
-keepattributes Exceptions

# 内部クラス情報を保持
-keepattributes InnerClasses

# 署名情報を保持
-keepattributes Signature

# ===== 警告抑制 =====
# Android標準ライブラリの警告を抑制
-dontwarn android.**
-dontwarn java.lang.invoke.**

# ===== 最適化除外 =====
# ネイティブメソッドを持つクラスは最適化しない
-keepclasseswithmembernames class * {
    native <methods>;
}

# enumクラスを保護
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ===== ログ最適化 =====
# リリースビルドでデバッグログを除去
-assumenosideeffects class com.example.lzh.util.LzhLogger {
    public static void v(...);
    public static void d(...);
}

# ===== 文字列最適化 =====
# 未使用の文字列リソースを除去
-assumenosideeffects class java.lang.String {
    public java.lang.String intern();
}

# ===== パフォーマンス最適化 =====
# 未使用のメソッドを除去（ただし公開APIは除く）
-assumenosideeffects class * {
    void finalize();
}

# Android API 21+最適化
-assumevalues class android.os.Build$VERSION {
    int SDK_INT return 21..35;
}

# API 21未満のコードパスを除去
-assumenosideeffects class com.example.lzh.util.AndroidCompatibility {
    public static boolean isApiLevelSupported() return true;
}

# ===== セキュリティ =====
# スタックトレースの難読化を防ぐ（デバッグ用）
-keepattributes SourceFile,LineNumberTable

# ===== 互換性確保 =====
# Java 8言語機能の互換性を確保
-keep class java.lang.invoke.** { *; }
-keep class java.time.** { *; }