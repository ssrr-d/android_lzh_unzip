# 設計文書

## 概要

Android LZH解凍ライブラリは、LZH（LHA）アーカイブファイルを解凍するためのJavaライブラリです。このライブラリは、Android API 21以上をサポートし、アプリの内部ストレージにファイルを抽出する機能を提供します。

## アーキテクチャ

### 全体構成

```
LzhExtractor (メインAPI)
├── LzhArchive (アーカイブ表現)
├── LzhHeader (ヘッダー解析)
├── LzhDecompressor (解凍エンジン)
│   ├── Lh0Decompressor (無圧縮)
│   ├── Lh1Decompressor (LZSS)
│   └── Lh5Decompressor (LZSS改良版)
├── EncodingDetector (文字エンコーディング検出)
└── FileManager (ファイル出力管理)
```

### レイヤー構成

1. **API Layer**: 開発者向けの公開インターフェース
2. **Core Layer**: LZH解凍の中核ロジック
3. **Decompression Layer**: 各圧縮方式の実装
4. **I/O Layer**: ファイル入出力とストリーム処理

## コンポーネントと インターフェース

### 1. LzhExtractor (メインAPI)

```java
public class LzhExtractor {
    // ファイルから解凍
    public void extract(File lzhFile, File outputDir) throws LzhException
    
    // InputStreamから解凍
    public void extract(InputStream inputStream, File outputDir) throws LzhException
    
    // バイト配列から解凍
    public void extract(byte[] lzhData, File outputDir) throws LzhException
    
    // アーカイブ情報の取得
    public LzhArchive getArchiveInfo(File lzhFile) throws LzhException
    
    // 特定ファイルの抽出
    public void extractFile(File lzhFile, String fileName, File outputDir) throws LzhException
}
```

### 2. LzhArchive (アーカイブ表現)

```java
public class LzhArchive {
    private List<LzhEntry> entries;
    
    public List<LzhEntry> getEntries()
    public LzhEntry getEntry(String fileName)
    public int getEntryCount()
    public long getTotalSize()
}

public class LzhEntry {
    private String fileName;
    private long originalSize;
    private long compressedSize;
    private String compressionMethod;
    private int crc16;
    private Date lastModified;
    
    // getters...
}
```

### 3. LzhHeader (ヘッダー解析)

```java
public class LzhHeader {
    public static LzhEntry parseHeader(InputStream input) throws IOException
    private static String decodeFileName(byte[] nameBytes, String encoding)
    private static Date parseDateTime(byte[] dateBytes)
}
```

### 4. LzhDecompressor (解凍エンジン)

```java
public abstract class LzhDecompressor {
    public abstract void decompress(InputStream input, OutputStream output, 
                                  long compressedSize, long originalSize) throws IOException;
    
    public static LzhDecompressor createDecompressor(String method) throws UnsupportedMethodException
}
```

### 5. EncodingDetector (文字エンコーディング検出)

```java
public class EncodingDetector {
    public static String detectEncoding(byte[] data)
    public static String decodeFileName(byte[] nameBytes)
    
    private static final String[] SUPPORTED_ENCODINGS = {"UTF-8", "Shift_JIS"};
}
```

### 6. FileManager (ファイル出力管理)

```java
public class FileManager {
    public static void createDirectories(File outputDir) throws IOException
    public static void writeFile(File outputFile, InputStream input) throws IOException
    public static boolean isValidOutputPath(File outputDir, String fileName)
}
```

## データモデル

### LZHファイル構造

```
LZHアーカイブ
├── ファイルエントリ1
│   ├── ヘッダー (可変長)
│   │   ├── ヘッダーサイズ (1byte)
│   │   ├── チェックサム (1byte)
│   │   ├── 圧縮方式 (5bytes)
│   │   ├── 圧縮後サイズ (4bytes)
│   │   ├── 元サイズ (4bytes)
│   │   ├── 最終更新日時 (4bytes)
│   │   ├── ファイル属性 (1byte)
│   │   ├── ファイル名長 (1byte)
│   │   └── ファイル名 (可変長)
│   └── 圧縮データ
├── ファイルエントリ2
│   └── ...
└── 終端マーカー (0x00)
```

### 圧縮方式マッピング

```java
public enum CompressionMethod {
    LH0("-lh0-", "無圧縮"),
    LH1("-lh1-", "LZSS"),
    LH5("-lh5-", "LZSS改良版");
    
    private final String signature;
    private final String description;
}
```

## エラーハンドリング

### 例外階層

```java
public class LzhException extends Exception {
    // 基底例外クラス
}

public class InvalidArchiveException extends LzhException {
    // 無効なアーカイブ形式
}

public class CorruptedArchiveException extends LzhException {
    // 破損したアーカイブ
}

public class UnsupportedMethodException extends LzhException {
    // サポートされていない圧縮方式
}

public class EncodingException extends LzhException {
    // 文字エンコーディングエラー
}
```

### エラー処理戦略

1. **入力検証**: ファイル形式とヘッダーの妥当性チェック
2. **リソース管理**: try-with-resourcesによる自動クリーンアップ
3. **部分的失敗**: 一部ファイルの解凍失敗時の継続処理オプション
4. **詳細ログ**: デバッグ用の詳細なエラー情報

## テスト戦略

### 単体テスト

1. **ヘッダー解析テスト**
   - 各圧縮方式のヘッダー解析
   - 文字エンコーディング検出
   - 不正ヘッダーの処理

2. **解凍アルゴリズムテスト**
   - LH0, LH1, LH5の解凍精度
   - 大きなファイルの処理
   - メモリ効率の検証

3. **ファイル操作テスト**
   - ディレクトリ作成
   - ファイル書き込み
   - 権限エラーの処理

### 統合テスト

1. **実際のLZHファイルテスト**
   - 様々なツールで作成されたアーカイブ
   - 日本語ファイル名を含むアーカイブ
   - 大容量アーカイブ

2. **Android環境テスト**
   - 異なるAPIレベルでの動作確認
   - 内部ストレージへの書き込み
   - メモリ制約下での動作

### テストデータ

```
test-archives/
├── simple-lh0.lzh (無圧縮アーカイブ)
├── simple-lh1.lzh (LZSS圧縮)
├── simple-lh5.lzh (LZSS改良版)
├── japanese-names.lzh (日本語ファイル名)
├── large-archive.lzh (大容量テスト)
├── corrupted.lzh (破損ファイル)
└── invalid-format.txt (無効形式)
```

## 実装考慮事項

### パフォーマンス最適化

1. **ストリーミング処理**: 大きなファイルのメモリ効率的な処理
2. **バッファリング**: 適切なバッファサイズによるI/O最適化
3. **遅延読み込み**: アーカイブ情報の必要時読み込み

### セキュリティ考慮事項

1. **パストラバーサル攻撃防止**: ファイルパスの検証
2. **リソース制限**: 解凍サイズの上限設定
3. **入力検証**: 不正なヘッダー値の検証

### Android固有の考慮事項

1. **API互換性**: API 21以上での動作保証
2. **内部ストレージ**: Context.getFilesDir()の使用
3. **権限管理**: 内部ストレージアクセスのみ
4. **ProGuard対応**: 難読化対応のための設定

## 依存関係

### 外部依存関係

- なし（Android標準ライブラリのみ使用）

### 内部モジュール構成

```
com.example.lzh
├── LzhExtractor.java (メインAPI)
├── model/
│   ├── LzhArchive.java
│   └── LzhEntry.java
├── parser/
│   └── LzhHeader.java
├── decompressor/
│   ├── LzhDecompressor.java
│   ├── Lh0Decompressor.java
│   ├── Lh1Decompressor.java
│   └── Lh5Decompressor.java
├── util/
│   ├── EncodingDetector.java
│   └── FileManager.java
└── exception/
    ├── LzhException.java
    ├── InvalidArchiveException.java
    ├── CorruptedArchiveException.java
    ├── UnsupportedMethodException.java
    └── EncodingException.java
```