import groovy.sql.Sql
import groovy.json.JsonSlurper
import java.nio.file.Files

class TestImportTool {
    static void main(String[] args) {
        // JDBCドライバのロード
        Class.forName("org.h2.Driver")

        // テスト用の接続設定
        def configFilePath = "test_import_config.json"
        def config = [
            jdbc: [
                url: "jdbc:h2:mem:testdb",
                username: "sa",
                password: "",
                driver: "org.h2.Driver"
            ],
            imports: [
                [
                    debug: true,
                    filePaths: ["../test/dump/test_table1.csv", "../test/dump/test_table2.csv"],
                    tableName: "TEST_TABLE",
                    deleteBeforeInsert: "COLUMN1 = 'value1'",
                    columnMapping: [
                        COLUMN1: [type: "csv", value: "CSV_COLUMN1", dataType: "string"],
                        COLUMN2: [type: "csv", value: "CSV_COLUMN2", dataType: "int"]
                    ]
                ]
            ]
        ]
        new File(configFilePath).text = groovy.json.JsonOutput.prettyPrint(groovy.json.JsonOutput.toJson(config))

        def sqlConnection = Sql.newInstance(
            config.jdbc.url,
            config.jdbc.username,
            config.jdbc.password,
            config.jdbc.driver
        )

        // テスト用のテーブル作成
        sqlConnection.execute("CREATE TABLE TEST_TABLE (COLUMN1 VARCHAR(255), COLUMN2 VARCHAR(255))")

        // テスト用のCSVファイル作成

        // テーブルを文字列と数値の列で作成し直す
        sqlConnection.execute("DROP TABLE IF EXISTS TEST_TABLE")
        sqlConnection.execute("CREATE TABLE TEST_TABLE (COLUMN1 VARCHAR(255), COLUMN2 INT)")
        def csvFile1 = new File("../test/dump/test_table1.csv")
        csvFile1.parentFile.mkdirs()
        csvFile1.text = "CSV_COLUMN1,CSV_COLUMN2\nvalue1,123"

        def csvFile2 = new File("../test/dump/test_table2.csv")
        csvFile2.text = "CSV_COLUMN1,CSV_COLUMN2\nvalue3,456"

        // インポートツールの実行
        def importTool = new ImportTool()
        importTool.main([configFilePath] as String[])

        // インポート結果の検証
        def result = sqlConnection.rows("SELECT * FROM TEST_TABLE")
        assert result.size() == 2
        assert result[0].COLUMN1 == 'value3'
        assert result[0].COLUMN2 == 456

        println "Import tool test with multiple files and conditional DELETE passed."

        // edit型のテスト用CSVファイル作成
        def csvFile3 = new File("../test/dump/test_table3.csv")
        csvFile3.text = "CSV_COLUMN1,CSV_COLUMN2,CSV_COLUMN3\nvalue5,789,abc-def"

        // edit型のテスト用設定を追加
        config.imports << [
            debug: true,
            filePaths: ["../test/dump/test_table3.csv"],
            tableName: "TEST_TABLE_EDIT",
            deleteBeforeInsert: "*",
            columnMapping: [
                COLUMN1: [type: "csv", value: "CSV_COLUMN1", dataType: "string"],
                COLUMN2: [type: "csv", value: "CSV_COLUMN2", dataType: "numeric"],
                COLUMN3: [type: "edit", csv: "CSV_COLUMN3", sql: "REPLACE(?, '-', '/')", dataType: "string"]
            ]
        ]
        new File(configFilePath).text = groovy.json.JsonOutput.prettyPrint(groovy.json.JsonOutput.toJson(config))

        // edit型用テーブル作成
        sqlConnection.execute("DROP TABLE IF EXISTS TEST_TABLE_EDIT")
        sqlConnection.execute("CREATE TABLE TEST_TABLE_EDIT (COLUMN1 VARCHAR(255), COLUMN2 INT, COLUMN3 VARCHAR(255))")

        // インポートツールの実行（再度）
        importTool.main([configFilePath] as String[])

        // edit型のインポート結果検証
        def resultEdit = sqlConnection.rows("SELECT * FROM TEST_TABLE_EDIT")
        assert resultEdit.size() == 1
        assert resultEdit[0].COLUMN1 == 'value5'
        assert resultEdit[0].COLUMN2 == 789
        assert resultEdit[0].COLUMN3 == 'abc/def'

        println "Import tool test with edit type column passed."
    }
}
