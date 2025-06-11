import groovy.sql.Sql
import groovy.json.JsonSlurper
import java.nio.file.Files
import java.nio.file.Paths

class ImportTool {
    static void main(String[] args) {
        try {
            // JDBCドライバのロード
            Class.forName("org.h2.Driver")
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver")
            Class.forName("oracle.jdbc.OracleDriver")
            Class.forName("org.postgresql.Driver")

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

                if (!columnMapping) {
                    println "Error: Column mapping is not defined for table: ${tableName}"
                    System.exit(2) // リターンコード2で終了
                }

                if (deleteBeforeInsert) {
                    if (deleteBeforeInsert == "*") {
                        sqlConnection.execute("DELETE FROM ${tableName}".toString())
                        println "Deleted all records from table: ${tableName}"
                    } else if (deleteBeforeInsert.trim()) {
                        sqlConnection.execute("DELETE FROM ${tableName} WHERE ${deleteBeforeInsert}".toString())
                        println "Deleted records from table: ${tableName} with condition: ${deleteBeforeInsert}"
                    } else {
                        println "No records deleted from table: ${tableName} as delete condition is empty"
                    }
                }

                def inputFilePaths = importConfig.filePaths // Updated to handle multiple file paths
                inputFilePaths.each { filePath -> // Renamed variable to avoid conflict
                    def lines = Files.readAllLines(Paths.get(filePath), java.nio.charset.StandardCharsets.UTF_8)
                    def headers = lines[0].split(/,(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)/)

                    def mappedHeaders = columnMapping.keySet().collect { it } // Use mapping keys
                    def insertQuery = "INSERT INTO ${tableName} (${mappedHeaders.join(",")}) VALUES (${mappedHeaders.collect { "?" }.join(",")})"

                    lines.drop(1).each { line ->
                        def csvValues = line.split(/,(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)/).collect { it.replaceAll("\"", "") }
                        def headersList = Arrays.asList(headers) // Convert array to list
                        def values = mappedHeaders.collect { column ->
                            def mapping = columnMapping[column]
                            switch (mapping.type) {
                                case "csv":
                                    return csvValues[headersList.indexOf(mapping.value)] // Use list indexOf
                                case "fixed":
                                    return mapping.value
                                case "edit":
                                    def csvValue = csvValues[headersList.indexOf(mapping.csv)] // Use list indexOf
                                    def query = mapping.sql.replace("?", "${csvValue}")
                                    return sqlConnection.firstRow(query).values().first()
                                default:
                                    throw new IllegalArgumentException("Unsupported mapping type: ${mapping.type}")
                            }
                        }
                        sqlConnection.execute(insertQuery, values)
                    }

                    println "Imported: ${filePath} into table ${tableName}"
                }
            }

            sqlConnection.close()
            System.exit(0) // 正常終了
        } catch (Exception e) {
            e.printStackTrace() // スタックトレースを標準出力に出力
            System.exit(9) // 異常終了
        }
    }
}
