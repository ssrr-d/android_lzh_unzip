import com.example.lzh.LzhExtractor;
import com.example.lzh.model.LzhArchive;
import com.example.lzh.model.LzhEntry;
import com.example.lzh.exception.LzhException;
import com.example.lzh.exception.FileNotFoundException;

import java.io.File;
import java.util.List;

/**
 * Android LZH解凍プラグインの個別ファイル抽出機能のサンプル
 */
public class ExtractSpecificFileExample {
    
    public static void main(String[] args) {
        // LZH解凍器のインスタンスを作成
        LzhExtractor extractor = new LzhExtractor();
        
        // テスト用のファイルパス（実際のLZHファイルに置き換えてください）
        File lzhFile = new File("test.lzh");
        File outputDir = new File("extracted");
        
        try {
            System.out.println("=== Android LZH解凍プラグインのテスト ===");
            
            // 1. アーカイブ情報を取得
            System.out.println("\n1. アーカイブ情報の取得:");
            LzhArchive archive = extractor.getArchiveInfo(lzhFile);
            
            System.out.println("エントリ数: " + archive.getEntryCount());
            System.out.println("総サイズ: " + archive.getTotalSize() + " bytes");
            
            // アーカイブ内のファイル一覧を表示
            System.out.println("\nアーカイブ内のファイル:");
            List<LzhEntry> entries = archive.getEntries();
            for (int i = 0; i < entries.size(); i++) {
                LzhEntry entry = entries.get(i);
                System.out.printf("%d. %s (%s, %d bytes)\n", 
                    i + 1, 
                    entry.getFileName(), 
                    entry.getCompressionMethod(), 
                    entry.getOriginalSize());
            }
            
            // 2. 全ファイルを解凍
            System.out.println("\n2. 全ファイルの解凍:");
            extractor.extract(lzhFile, outputDir);
            System.out.println("全ファイルの解凍が完了しました: " + outputDir.getAbsolutePath());
            
            // 3. 個別ファイルの抽出（最初のファイルを例として）
            if (!entries.isEmpty()) {
                String targetFileName = entries.get(0).getFileName();
                File specificOutputDir = new File("extracted_specific");
                
                System.out.println("\n3. 個別ファイルの抽出:");
                System.out.println("抽出対象: " + targetFileName);
                
                extractor.extractFile(lzhFile, targetFileName, specificOutputDir);
                System.out.println("個別ファイルの抽出が完了しました: " + specificOutputDir.getAbsolutePath());
            }
            
            // 4. 存在しないファイルの抽出テスト
            System.out.println("\n4. 存在しないファイルの抽出テスト:");
            try {
                extractor.extractFile(lzhFile, "nonexistent.txt", new File("test_output"));
                System.out.println("エラー: 存在しないファイルが抽出されました");
            } catch (FileNotFoundException e) {
                System.out.println("正常: 存在しないファイルに対して適切にエラーが発生しました");
                System.out.println("エラーメッセージ: " + e.getMessage());
            }
            
            // 5. バイト配列からの解凍テスト
            System.out.println("\n5. バイト配列からの解凍テスト:");
            try (java.io.FileInputStream fis = new java.io.FileInputStream(lzhFile)) {
                byte[] lzhData = fis.readAllBytes();
                File byteArrayOutputDir = new File("extracted_from_bytes");
                
                extractor.extract(lzhData, byteArrayOutputDir);
                System.out.println("バイト配列からの解凍が完了しました: " + byteArrayOutputDir.getAbsolutePath());
                
                // バイト配列から個別ファイル抽出
                if (!entries.isEmpty()) {
                    String targetFileName = entries.get(0).getFileName();
                    File specificByteOutputDir = new File("extracted_specific_from_bytes");
                    
                    extractor.extractFile(lzhData, targetFileName, specificByteOutputDir);
                    System.out.println("バイト配列から個別ファイルの抽出が完了しました: " + specificByteOutputDir.getAbsolutePath());
                }
            }
            
            System.out.println("\n=== テスト完了 ===");
            
        } catch (LzhException e) {
            System.err.println("LZH処理エラー: " + e.getMessage());
            if (e.getErrorContext() != null) {
                System.err.println("エラーコンテキスト: " + e.getErrorContext());
            }
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("予期しないエラー: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 使用方法の説明を表示
     */
    public static void printUsage() {
        System.out.println("Android LZH解凍プラグインの使用方法:");
        System.out.println();
        System.out.println("1. 基本的な使用方法:");
        System.out.println("   LzhExtractor extractor = new LzhExtractor();");
        System.out.println("   extractor.extract(new File(\"archive.lzh\"), new File(\"output\"));");
        System.out.println();
        System.out.println("2. アーカイブ情報の取得:");
        System.out.println("   LzhArchive archive = extractor.getArchiveInfo(new File(\"archive.lzh\"));");
        System.out.println("   List<LzhEntry> entries = archive.getEntries();");
        System.out.println();
        System.out.println("3. 個別ファイルの抽出:");
        System.out.println("   extractor.extractFile(new File(\"archive.lzh\"), \"filename.txt\", new File(\"output\"));");
        System.out.println();
        System.out.println("4. バイト配列からの解凍:");
        System.out.println("   byte[] lzhData = ...; // LZHファイルのバイト配列");
        System.out.println("   extractor.extract(lzhData, new File(\"output\"));");
        System.out.println();
        System.out.println("5. InputStreamからの解凍:");
        System.out.println("   try (FileInputStream fis = new FileInputStream(\"archive.lzh\")) {");
        System.out.println("       extractor.extract(fis, new File(\"output\"));");
        System.out.println("   }");
        System.out.println();
        System.out.println("サポートされている圧縮方式:");
        System.out.println("- LH0 (無圧縮)");
        System.out.println("- LH1 (LZSS)");
        System.out.println("- LH5 (LZSS改良版)");
        System.out.println();
        System.out.println("特徴:");
        System.out.println("- 日本語ファイル名対応 (UTF-8, Shift_JIS自動検出)");
        System.out.println("- パストラバーサル攻撃防止");
        System.out.println("- メモリ効率的なストリーミング処理");
        System.out.println("- 包括的なエラーハンドリング");
        System.out.println("- Android API 21以上対応");
    }
}