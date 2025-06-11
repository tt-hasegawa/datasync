import groovy.sql.Sql
import groovy.json.JsonSlurper

class ExportTool {
    static void main(String[] args) {
        try {
            // JDBCドライバのロード
            Class.forName("org.h2.Driver")
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver")
            Class.forName("oracle.jdbc.OracleDriver")
            Class.forName("org.postgresql.Driver")

            def configFilePath = args[0] // JSON設定ファイルのパス
            def config = new JsonSlurper().parse(new File(configFilePath))

            def outputDirectory = new File(config.outputDirectory)
            if (!outputDirectory.exists()) {
                outputDirectory.mkdirs()
            }

            def sqlConnection = Sql.newInstance(
                config.jdbc.url,
                config.jdbc.username,
                config.jdbc.password,
                config.jdbc.driver
            )
            if (config.debug) {
                println "JDBC URL: ${config.jdbc.url}"
                println "JDBC Username: ${config.jdbc.username}"
                println "JDBC Driver: ${config.jdbc.driver}"
            }

            config.exports.each { exportConfig ->
                def outputFile = new File(outputDirectory, exportConfig.outputFile)
                def query = exportConfig.sql

                def rowIndex = 0 // 行インデックスの初期化
                outputFile.withWriter("UTF-8") { writer ->
                    sqlConnection.eachRow(query) { row ->
                        if (rowIndex++ == 0) {
                            def metaData = row.getMetaData()
                            def columnNames = (1..metaData.columnCount).collect { metaData.getColumnName(it) }
                            writer.writeLine(columnNames.collect { "\"${it}\"" }.join(",")) // ヘッダー行
                            // デバッグモードの場合、ヘッダ行を表示
                            if (config.debug) {
                                println "Exporting headers: ${columnNames.join(", ")}"
                            }
                        }
                        // デバッグモードの場合、データ行を表示
                        if (config.debug) {
                            println "Exporting row: ${rowIndex} - ${row.toRowResult()}"
                        }
                        writer.writeLine(
                            row.toRowResult().values().collect { 
                                "\"${it?.toString()?.replaceAll("\"", "\"\"")}\""
                            }.join(",")
                        )
                    }
                }

                println "Exported: ${outputFile.absolutePath}"
            }

            sqlConnection.close()
            System.exit(0) // 正常終了
        } catch (Exception e) {
            e.printStackTrace() // スタックトレースを標準出力に出力
            System.exit(9) // 異常終了
        }
    }
}
