import groovy.sql.Sql
import java.nio.file.Files
import java.nio.file.Paths

class ImportTool {
    static void main(String[] args) {
        try {
            def jdbcUrl = args[0] // JDBC URL
            def jdbcUser = args[1] // JDBC ユーザー名
            def jdbcPassword = args[2] // JDBC パスワード
            def jdbcDriver = args[3] // JDBC ドライバ
            def inputFilePath = args[4] // インポートするCSVファイルのパス
            def tableName = args[5] // インポート先のテーブル名

            def sqlConnection = Sql.newInstance(
                jdbcUrl,
                jdbcUser,
                jdbcPassword,
                jdbcDriver
            )

            def lines = Files.readAllLines(Paths.get(inputFilePath), java.nio.charset.StandardCharsets.UTF_8)
            def headers = lines[0].split(",")

            def insertQuery = "INSERT INTO ${tableName} (${headers.join(",")}) VALUES (${headers.collect { "?" }.join(",")})"

            sqlConnection.withBatch(insertQuery) { ps ->
                lines.drop(1).each { line ->
                    def values = line.split(",").collect { it.replaceAll("\\"", "") }
                    ps.addBatch(values)
                }
            }

            println "Imported: ${inputFilePath} into table ${tableName}"

            sqlConnection.close()
            System.exit(0) // 正常終了
        } catch (Exception e) {
            e.printStackTrace() // スタックトレースを標準出力に出力
            System.exit(9) // 異常終了
        }
    }
}
