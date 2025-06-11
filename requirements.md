# データ同期ツール 要件定義

## 1. 目的
- システムA（SQLServer）にあるマスタ情報を変換し、システムB（OracleまたはPostgres）で参照可能にする。

## 2. 対象データ
- **データ形式**: CSV形式でファイルサーバに出力。
- **データ内容**: マスタ情報（具体的なテーブルやカラムは後ほど詳細化）。
- **データ量**: 1日1～2回の更新に耐えられる程度のデータ量。

## 3. データソースとデスティネーション
- **送信元**: SQLServer（システムA）。
- **送信先**: OracleまたはPostgres（システムB）。
- **中間経路**: ファイルサーバを介してCSVファイルでデータを受け渡し。

## 4. 同期の方法
- **手順**:
  1. システムAからCSV形式でデータをエクスポートし、ファイルサーバに保存。
  2. ファイルサーバからシステムBにデータをインポート。
- **頻度**: 日次で1日1～2回。
- **スケジュール**: JP1またはタスクスケジューラで実行。

## 5. エラーハンドリング
- **エクスポート/インポート失敗時**:
  - エラーログを出力。
  - 必要に応じて通知（メールやログ監視ツールとの連携）。
- **データ不整合**:
  - データ検証ロジックを実装（例: 必須カラムのチェック、データ型の検証）。

## 6. パフォーマンス要件
- **処理時間**: 1回の同期処理が数分～数十分以内に完了すること。
- **リソース使用量**: サーバ負荷を最小限に抑える。

## 7. セキュリティ
- **データ保護**:
  - CSVファイルの暗号化（必要に応じて）。
  - ファイルサーバへのアクセス制御。
- **認証情報**:
  - SQLServer、Oracle/Postgresへの接続情報を安全に管理。

## 8. 拡張性
- 将来的に他のデータソースやデスティネーション（例: MySQL、NoSQLデータベース）を追加可能な設計。

## 9. その他
- **ログ**: 処理ログを出力（成功/失敗の記録）。
- **ツール**: Groovyスクリプトを活用して実装。
- **スケジューリング**: JP1またはタスクスケジューラでの定期実行。

## 10. CSVフォーマット仕様
- **区切り文字**: カンマ（`,`）
- **文字列の囲み**: 二重引用符（`"`）
- **文字エンコーディング**: UTF-8（外字対応）
- **ヘッダー行**: 1行目にカラム名を記載。

### データ例
```csv
"id","name","description","created_at"
"1","商品A","説明A","2025-05-30"
"2","商品B","説明B","2025-05-30"
```

## 12. SQLによるデータ変換
- **変換例**:
```sql
SELECT 
    source_column1 AS target_column1,
    source_column2 AS target_column2,
    CONVERT(VARCHAR, source_date_column, 120) AS target_date_column
FROM 
    source_table
WHERE 
    is_active = 1;
```

## 13. ツール構成

### エクスポートツール
- **スクリプトファイル**: `DataExporter/export_tool.groovy`
- **実行バッチファイル**: `DataExporter/run_export.bat`
- **設定ファイル**: `export_config.json`
- **使用ライブラリ**:
  - `groovy-all-2.4.21.jar`
  - `mssql-jdbc-12.10.0.jre8.jar`

### インポートツール
- **スクリプトファイル**: `DataImporter/import_tool.groovy`
- **実行バッチファイル**: `DataImporter/run_import.bat`
- **使用ライブラリ**:
  - `groovy-all-2.4.21.jar`
  - `ojdbc8-23.6.0.24.10.jar`
  - `postgresql-42.7.5.jar`

### 実行方法
#### エクスポートツール
1. `run_export.bat` を実行。
2. 設定ファイル `export_config.json` に基づいてデータをエクスポート。

#### インポートツール
1. `run_import.bat` を実行。
2. 第一引数にインポートするCSVファイルのパス、第二引数にインポート先のテーブル名を指定。

## 14. インポートツールの拡張仕様（2025-06-11修正）

- **複数ファイルインポート対応**: 1つのテーブル定義（1つのimport定義）で複数CSVファイル（filePaths配列）を順次取り込むことができる。
- **deleteBeforeInsertの条件指定**:
    - 空文字列（""）: 削除処理を行わない。
    - アスタリスク（"*"）: テーブル全件DELETE。
    - 任意の文字列: その内容をWHERE句に追記してDELETE文を実行（例: "COL1 = 'A' AND COL2 > 0"）。
- **import_config.json例**:
```json
{
  "imports": [
    {
      "filePaths": ["file1.csv", "file2.csv"],
      "tableName": "TBL01",
      "deleteBeforeInsert": "*",
      "columnMapping": { "COL1": {"type": "csv", "value": "CSV1"} }
    },
    {
      "filePaths": ["file3.csv"],
      "tableName": "TBL01",
      "deleteBeforeInsert": "COL1 = 'A'",
      "columnMapping": { "COL1": {"type": "csv", "value": "CSV1"} }
    }
  ]
}
```
- **動作例**:
    1. 1つ目のimport定義でTBL01を全件削除し、file1.csv, file2.csvを順次インポート。
    2. 2つ目のimport定義でTBL01の条件付きDELETE後、file3.csvをインポート。
- **テスト**: test_import_config.json, test_import_tool.groovyで上記仕様の自動テストを実装。

