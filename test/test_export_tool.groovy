import groovy.sql.Sql
import groovy.json.JsonSlurper
import java.nio.file.Files

class TestExportTool {
    static void main(String[] args) {
        // JDBCドライバのロード
        Class.forName("org.h2.Driver")

        def configFilePath = "test_export_config.json"
        def config = new JsonSlurper().parse(new File(configFilePath))

        def sqlConnection = Sql.newInstance(
            config.jdbc.url,
            config.jdbc.username,
            config.jdbc.password,
            config.jdbc.driver
        )

        // テスト用のテーブル作成
        sqlConnection.execute("CREATE TABLE TEST_TABLE (COLUMN1 VARCHAR(255), COLUMN2 VARCHAR(255))")
        sqlConnection.execute("INSERT INTO TEST_TABLE VALUES ('value1', 'value2')")

        // エクスポートツールの実行
        def exportTool = new ExportTool()
        exportTool.main([configFilePath] as String[])

        // エクスポート結果の検証
        def exportedFile = new File(config.outputDirectory, "test_table.csv")
        assert exportedFile.exists()
        def lines = Files.readAllLines(exportedFile.toPath())
        assert lines[0] == '"COLUMN1","COLUMN2"'
        assert lines[1] == '"value1","value2"'

        println "Export tool test passed."
    }
}
