import android.content.Context;
import android.content.res.AssetManager;
import com.example.lzh.AndroidLzhExtractor;
import com.example.lzh.model.LzhArchive;
import com.example.lzh.model.LzhEntry;
import com.example.lzh.exception.LzhException;

import java.io.File;
import java.io.InputStream;
import java.util.List;

/**
 * Android LZH解凍プラグインのAndroid環境での使用例
 */
public class AndroidLzhExtractorExample {
    
    private Context context;
    private AndroidLzhExtractor extractor;
    
    public AndroidLzhExtractorExample(Context context) {
        this.context = context;
        this.extractor = new AndroidLzhExtractor(context);
    }
    
    /**
     * アセットからLZHファイルを解凍する例
     */
    public void extractFromAssets(String assetFileName, String outputSubdirectory) {
        try {
            System.out.println("=== アセットからの解凍テスト ===");
            
            // アセットマネージャーを取得
            AssetManager assetManager = context.getAssets();
            
            // アセットからInputStreamを取得
            try (InputStream assetStream = assetManager.open(assetFileName)) {
                
                // 1. アーカイブ情報を取得
                System.out.println("1. アーカイブ情報の取得:");
                LzhArchive archive = extractor.getArchiveInfo(assetStream);
                
                System.out.println("エントリ数: " + archive.getEntryCount());
                System.out.println("総サイズ: " + archive.getTotalSize() + " bytes");
                
                // ファイル一覧を表示
                List<LzhEntry> entries = archive.getEntries();
                for (LzhEntry entry : entries) {
                    System.out.printf("- %s (%s, %d bytes)\n", 
                        entry.getFileName(), 
                        entry.getCompressionMethod(), 
                        entry.getOriginalSize());
                }
            }
            
            // 2. アセットから内部ストレージに解凍
            System.out.println("\n2. 内部ストレージへの解凍:");
            try (InputStream assetStream = assetManager.open(assetFileName)) {
                extractor.extractToInternalStorage(assetStream, outputSubdirectory);
                System.out.println("解凍完了: " + context.getFilesDir() + "/" + outputSubdirectory);
            }
            
            // 3. 個別ファイルの抽出
            System.out.println("\n3. 個別ファイルの抽出:");
            try (InputStream assetStream = assetManager.open(assetFileName)) {
                LzhArchive archive = extractor.getArchiveInfo(assetStream);
                if (!archive.getEntries().isEmpty()) {
                    String targetFileName = archive.getEntries().get(0).getFileName();
                    
                    try (InputStream assetStream2 = assetManager.open(assetFileName)) {
                        extractor.extractFileToInternalStorage(assetStream2, targetFileName, "specific_files");
                        System.out.println("個別ファイル抽出完了: " + targetFileName);
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("エラー: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 内部ストレージのLZHファイルを解凍する例
     */
    public void extractFromInternalStorage(String lzhFileName, String outputSubdirectory) {
        try {
            System.out.println("=== 内部ストレージからの解凍テスト ===");
            
            // 内部ストレージのファイルパスを取得
            File lzhFile = new File(context.getFilesDir(), lzhFileName);
            
            if (!lzhFile.exists()) {
                System.err.println("ファイルが存在しません: " + lzhFile.getAbsolutePath());
                return;
            }
            
            // 1. アーカイブ情報を取得
            System.out.println("1. アーカイブ情報の取得:");
            LzhArchive archive = extractor.getArchiveInfo(lzhFile);
            
            System.out.println("エントリ数: " + archive.getEntryCount());
            System.out.println("総サイズ: " + archive.getTotalSize() + " bytes");
            
            // 2. 解凍
            System.out.println("\n2. 解凍:");
            extractor.extractToInternalStorage(lzhFile, outputSubdirectory);
            System.out.println("解凍完了: " + context.getFilesDir() + "/" + outputSubdirectory);
            
            // 3. 解凍されたファイルの確認
            System.out.println("\n3. 解凍されたファイルの確認:");
            File outputDir = new File(context.getFilesDir(), outputSubdirectory);
            listFiles(outputDir, "");
            
        } catch (Exception e) {
            System.err.println("エラー: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * ストレージ容量をチェックしてから解凍する例
     */
    public void extractWithStorageCheck(String assetFileName, String outputSubdirectory) {
        try {
            System.out.println("=== ストレージ容量チェック付き解凍 ===");
            
            // 利用可能容量をチェック
            long availableSpace = extractor.getAvailableInternalStorageSpace();
            System.out.println("利用可能容量: " + formatBytes(availableSpace));
            
            // アーカイブサイズを取得
            AssetManager assetManager = context.getAssets();
            try (InputStream assetStream = assetManager.open(assetFileName)) {
                LzhArchive archive = extractor.getArchiveInfo(assetStream);
                long totalSize = archive.getTotalSize();
                
                System.out.println("アーカイブ総サイズ: " + formatBytes(totalSize));
                
                // 容量チェック（余裕を持って2倍の容量が必要と仮定）
                if (availableSpace < totalSize * 2) {
                    System.err.println("警告: ストレージ容量が不足している可能性があります");
                    System.err.println("必要容量: " + formatBytes(totalSize * 2));
                    System.err.println("利用可能容量: " + formatBytes(availableSpace));
                } else {
                    System.out.println("容量チェック: OK");
                }
            }
            
            // 解凍実行
            try (InputStream assetStream = assetManager.open(assetFileName)) {
                extractor.extractToInternalStorage(assetStream, outputSubdirectory);
                System.out.println("解凍完了");
            }
            
        } catch (Exception e) {
            System.err.println("エラー: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * エラーハンドリングの例
     */
    public void demonstrateErrorHandling() {
        System.out.println("=== エラーハンドリングのデモ ===");
        
        // 1. 存在しないファイル
        try {
            extractor.getArchiveInfo(new File("nonexistent.lzh"));
        } catch (LzhException e) {
            System.out.println("1. 存在しないファイル: " + e.getMessage());
        }
        
        // 2. 無効なアーカイブ
        try {
            byte[] invalidData = "This is not a LZH file".getBytes();
            extractor.getArchiveInfo(invalidData);
        } catch (LzhException e) {
            System.out.println("2. 無効なアーカイブ: " + e.getMessage());
        }
        
        // 3. null入力
        try {
            extractor.extractToInternalStorage((File) null, "output");
        } catch (LzhException e) {
            System.out.println("3. null入力: " + e.getMessage());
        }
        
        // 4. 存在しないファイルの個別抽出
        try {
            AssetManager assetManager = context.getAssets();
            try (InputStream assetStream = assetManager.open("test.lzh")) {
                extractor.extractFileToInternalStorage(assetStream, "nonexistent.txt", "output");
            }
        } catch (Exception e) {
            System.out.println("4. 存在しないファイルの個別抽出: " + e.getMessage());
        }
    }
    
    /**
     * ディレクトリ内のファイルを再帰的にリスト表示
     */
    private void listFiles(File dir, String indent) {
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        
        for (File file : files) {
            if (file.isDirectory()) {
                System.out.println(indent + "[DIR] " + file.getName());
                listFiles(file, indent + "  ");
            } else {
                System.out.println(indent + "[FILE] " + file.getName() + " (" + formatBytes(file.length()) + ")");
            }
        }
    }
    
    /**
     * バイト数を人間が読みやすい形式にフォーマット
     */
    private String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }
    
    /**
     * 使用方法の説明
     */
    public static void printAndroidUsage() {
        System.out.println("Android LZH解凍プラグインの使用方法:");
        System.out.println();
        System.out.println("1. 初期化:");
        System.out.println("   AndroidLzhExtractor extractor = new AndroidLzhExtractor(context);");
        System.out.println();
        System.out.println("2. アセットからの解凍:");
        System.out.println("   try (InputStream stream = getAssets().open(\"archive.lzh\")) {");
        System.out.println("       extractor.extractToInternalStorage(stream, \"extracted\");");
        System.out.println("   }");
        System.out.println();
        System.out.println("3. 内部ストレージのファイルから解凍:");
        System.out.println("   File lzhFile = new File(getFilesDir(), \"archive.lzh\");");
        System.out.println("   extractor.extractToInternalStorage(lzhFile, \"extracted\");");
        System.out.println();
        System.out.println("4. 個別ファイルの抽出:");
        System.out.println("   extractor.extractFileToInternalStorage(stream, \"filename.txt\", \"output\");");
        System.out.println();
        System.out.println("5. アーカイブ情報の取得:");
        System.out.println("   LzhArchive archive = extractor.getArchiveInfo(lzhFile);");
        System.out.println("   for (LzhEntry entry : archive.getEntries()) {");
        System.out.println("       Log.d(TAG, \"File: \" + entry.getFileName());");
        System.out.println("   }");
        System.out.println();
        System.out.println("注意事項:");
        System.out.println("- ファイルは内部ストレージ (Context.getFilesDir()) に保存されます");
        System.out.println("- 外部ストレージへの書き込み権限は不要です");
        System.out.println("- API 21 (Android 5.0) 以上が必要です");
    }
}