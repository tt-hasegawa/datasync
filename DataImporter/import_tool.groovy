import groovy.sql.Sql
import groovy.json.JsonSlurper
import java.nio.file.Files
import java.nio.file.Paths

class ImportTool {
    static void main(String[] args) {
        try {
            def configFilePath = args[0] // JSON設定ファイルのパス
            def config = new JsonSlurper().parse(new File(configFilePath))

            def sqlConnection = Sql.newInstance(
                config.jdbc.url,
                config.jdbc.username,
                config.jdbc.password,
                config.jdbc.driver
            )

            config.imports.each { importConfig ->
                def inputFilePath = importConfig.filePath
                def tableName = importConfig.tableName
                def deleteBeforeInsert = importConfig.deleteBeforeInsert // DELETEフラグ
                def columnMapping = importConfig.columnMapping // 列定義マッピング

                if (deleteBeforeInsert) {
                    sqlConnection.execute("DELETE FROM ${tableName}".toString())
                    println "Deleted all records from table: ${tableName}"
                }

                def lines = Files.readAllLines(Paths.get(inputFilePath), java.nio.charset.StandardCharsets.UTF_8)
                def headers = lines[0].split(/,(?=(?:[^"]*"[^"]*")*[^"]*$)/)
                // headersから前後のクォートを削除
                headers = headers.collect { it.replaceAll(/^"|"$/, "") }
                def mappedHeaders = headers.collect { columnMapping[it] ?: it } // マッピング適用
                def insertQuery = "INSERT INTO ${tableName} (${mappedHeaders.join(",")}) VALUES (${mappedHeaders.collect { "?" }.join(",")})"

                lines.drop(1).each { line ->
                    def values = line.split(/,(?=(?:[^"]*"[^"]*")*[^"]*$)/).collect { it.replaceAll("\"", "") }
                    sqlConnection.execute(insertQuery, values)
                }

                println "Imported: ${inputFilePath} into table ${tableName}"
            }

            sqlConnection.close()
            System.exit(0) // 正常終了
        } catch (Exception e) {
            e.printStackTrace() // スタックトレースを標準出力に出力
            System.exit(9) // 異常終了
        }
    }
}
