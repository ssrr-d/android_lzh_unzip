# Android LZH Library

LZH（LHA）アーカイブファイルを解凍するためのAndroidライブラリです。

## 概要

このライブラリは、Android API 21以上をサポートし、LZHアーカイブからファイルを抽出する機能を提供します。

## プロジェクト構造

```
src/main/java/com/example/lzh/
├── LzhExtractor.java              # メインAPI
├── model/
│   ├── LzhArchive.java           # アーカイブ表現
│   └── LzhEntry.java             # ファイルエントリ
├── parser/
│   └── LzhHeader.java            # ヘッダー解析
├── decompressor/
│   └── LzhDecompressor.java      # 解凍エンジン基底クラス
├── util/
│   ├── EncodingDetector.java     # 文字エンコーディング検出
│   └── FileManager.java          # ファイル出力管理
└── exception/
    ├── LzhException.java         # 基底例外
    ├── InvalidArchiveException.java
    ├── CorruptedArchiveException.java
    ├── UnsupportedMethodException.java
    └── EncodingException.java
```

## 使用方法

```java
// LZHファイルを解凍
LzhExtractor extractor = new LzhExtractor();
File lzhFile = new File("archive.lzh");
File outputDir = new File(context.getFilesDir(), "extracted");
extractor.extract(lzhFile, outputDir);

// アーカイブ情報を取得
LzhArchive archive = extractor.getArchiveInfo(lzhFile);
for (LzhEntry entry : archive.getEntries()) {
    System.out.println("File: " + entry.getFileName() + 
                      " Size: " + entry.getOriginalSize());
}
```

## 要件

- Android API 21以上
- Java 8以上

## サポートする圧縮方式

- LH0（無圧縮）
- LH1（LZSS）
- LH5（LZSS改良版）

## ライセンス

TBD