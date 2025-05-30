import groovy.sql.Sql
import groovy.json.JsonSlurper

class ExportTool {
    static void main(String[] args) {
        try {
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

            config.exports.each { exportConfig ->
                def outputFile = new File(outputDirectory, exportConfig.outputFile)
                def query = exportConfig.sql

                outputFile.withWriter("UTF-8") { writer ->
                    sqlConnection.eachRow(query) { row ->
                        if (writer.getLineNumber() == 0) {
                            writer.writeLine(row.keySet().join(",")) // ヘッダー行
                        }
                        writer.writeLine(row.values().collect { it?.toString()?.replaceAll(",", "\\,") }.join(","))
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
