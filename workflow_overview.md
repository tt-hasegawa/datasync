# データ同期ツール ワークフロー

## 処理の流れ

```mermaid
flowchart TD
    subgraph SystemA[システムA - SQLServer]
        Start[開始] --> Config[エクスポートツール実行]
        Config -->|設定ファイル参照| Query[CSVファイル生成]
        Query -->|SELECT SQL実行してCSV生成| GenerateCSV[CSVファイル生成]
        GenerateCSV -->|ファイルサーバに保存| FileServer[ファイルサーバ]
    end
    subgraph FileServer[ファイルサーバ]
        
    end

    subgraph SystemB[システムB - Oracle/Postgres]
        FileServer --> Import[インポートツール実行]
        Import -->|CSVファイル参照| LoadDB[データベースにインポート]
        LoadDB --> End[終了]
    end
```

---

## 2025-06-11 拡張仕様

- インポート時、1つのテーブルに対して複数ファイルを順次取り込むパターンをサポート。
- deleteBeforeInsertの条件分岐（全件削除・条件付き削除・削除なし）をサポート。
- 詳細は import_config.example.json, test_import_config.json を参照。

---
