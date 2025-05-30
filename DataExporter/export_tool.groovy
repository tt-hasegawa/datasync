import groovy.sql.Sql
import groovy.json.JsonSlurper

class ExportTool {
    static void main(String[] args) {
        def configFilePath = args[0] // JSON設定ファイルのパス
        def config = new JsonSlurper().parse(new File(configFilePath))

        def outputDirectory = new File(config.outputDirectory)
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs()
        }

        def sqlConnection = Sql.newInstance(
            "jdbc:sqlserver://your-server;databaseName=your-database",
            "username",
            "password",
            "com.microsoft.sqlserver.jdbc.SQLServerDriver"
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
    }
}
