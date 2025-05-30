import groovy.sql.Sql
import java.nio.file.Files
import java.nio.file.Paths

class ImportTool {
    static void main(String[] args) {
        def inputFilePath = args[0] // インポートするCSVファイルのパス
        def tableName = args[1] // インポート先のテーブル名

        def sqlConnection = Sql.newInstance(
            "jdbc:oracle:thin:@your-server:1521:your-service",
            "username",
            "password",
            "oracle.jdbc.OracleDriver"
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
    }
}
