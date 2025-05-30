---
marp: true
theme: default
mermaid: true
paginate: true
---

# データ同期ツール

---

## 1. ツールの要件／目的

- **目的**: システムA（SQLServer）にあるマスタ情報を変換し、システムB（OracleまたはPostgres）で参照可能にする。
- **対象データ**:
  - データ形式: CSV形式
  - データ量: 1日1～2回の更新に耐えられる程度
- **同期方法**:
  - システムAからCSV形式でエクスポートし、ファイルサーバに保存
  - ファイルサーバからシステムBにインポート
- **頻度**: 日次で1日1～2回
- **スケジュール**: JP1またはタスクスケジューラで実行

---

## 2-1. アーキテクチャ

- **システムA**: SQLServer
- **ファイルサーバ**: 中間経路としてCSVファイルを保存
- **システムB**: OracleまたはPostgres

---

## 2-2. 実行環境

### エクスポートツール
- 実行環境: システムA上
- 使用言語: Groovy
- 必要ライブラリ: `groovy-all`, `mssql-jdbc`

### インポートツール
- 実行環境: システムB上
- 使用言語: Groovy
- 必要ライブラリ: `groovy-all`, `ojdbc8`, `postgresql`

---

## 3-1. エクスポート側の要件

### 要件
- **データ変換**: SQLの`SELECT`文で列名やデータ型を変換
- **出力形式**:
  - CSV形式
  - UTF-8エンコーディング
  - ヘッダー行を含む

---

## 3-2. エクスポート側の設定

### 設定ファイル例
```json
{
  "outputDirectory": "path/to/export/directory",
  "exports": [
    {
      "outputFile": "output1.csv",
      "sql": "SELECT column1, column2 FROM table1 WHERE condition1"
    },
    {
      "outputFile": "output2.csv",
      "sql": "SELECT columnA, columnB FROM table2 WHERE condition2"
    }
  ]
}
```

---

## 4. 概要図

### 処理の流れ

- **システムA (SQLServer)**
  - ジョブ管理システムでツール実行
    - 設定ファイル参照
    - SELECT SQL実行してCSV生成
    - CSVファイル生成
    - ファイルサーバに保存

- **ファイルサーバ**
  - CSVファイルを格納

- **システムB (Oracle/Postgres)**
  - ジョブ管理システムでツール実行
    - CSVファイル参照
    - データベースにインポート

---

# Thank You for Your Attention!

### Let's Sync the Future Together 🚀
