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
                    filePaths: ["../test/dump/test_table1.csv", "../test/dump/test_table2.csv"],
                    tableName: "TEST_TABLE",
                    deleteBeforeInsert: "COLUMN1 = 'value1'",
                    columnMapping: [
                        COLUMN1: [type: "csv", value: "CSV_COLUMN1"],
                        COLUMN2: [type: "csv", value: "CSV_COLUMN2"]
                    ]
                ]
            ]
        ]
        new File(configFilePath).text = groovy.json.JsonOutput.toJson(config)

        def sqlConnection = Sql.newInstance(
            config.jdbc.url,
            config.jdbc.username,
            config.jdbc.password,
            config.jdbc.driver
        )

        // テスト用のテーブル作成
        sqlConnection.execute("CREATE TABLE TEST_TABLE (COLUMN1 VARCHAR(255), COLUMN2 VARCHAR(255))")

        // テスト用のCSVファイル作成
        def csvFile1 = new File("../test/dump/test_table1.csv")
        csvFile1.parentFile.mkdirs()
        csvFile1.text = "CSV_COLUMN1,CSV_COLUMN2\nvalue1,value2"

        def csvFile2 = new File("../test/dump/test_table2.csv")
        csvFile2.text = "CSV_COLUMN1,CSV_COLUMN2\nvalue3,value4"

        // インポートツールの実行
        def importTool = new ImportTool()
        importTool.main([configFilePath] as String[])

        // インポート結果の検証
        def result = sqlConnection.rows("SELECT * FROM TEST_TABLE")
        assert result.size() == 2
        assert result[0].COLUMN1 == 'value3'
        assert result[0].COLUMN2 == 'value4'

        println "Import tool test with multiple files and conditional DELETE passed."
    }
}
