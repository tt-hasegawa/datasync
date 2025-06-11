@echo off
setlocal enabledelayedexpansion
rem MavenリポジトリのベースURL
set BASE_URL=https://repo1.maven.org/maven2

rem Tools/libフォルダのパス
set LIB_DIR=%~dp0lib

mkdir %LIB_DIR%

for %%f in (
com/oracle/database/jdbc/ojdbc8/23.6.0.24.10/ojdbc8-23.6.0.24.10.jar 
com/microsoft/sqlserver/mssql-jdbc/12.10.0.jre8/mssql-jdbc-12.10.0.jre8.jar
org/codehaus/groovy/groovy-all/2.4.21/groovy-all-2.4.21.jar 
org/postgresql/postgresql/42.7.5/postgresql-42.7.5.jar 
com/h2database/h2/2.1.214/h2-2.1.214.jar
) do (
    set "JAR_PATH=%%f"
    set "FILENAME=%%~nxf"
    bitsadmin /TRANSFER "myDownloadJob" /download /priority normal !BASE_URL!/!JAR_PATH! !LIB_DIR!\!FILENAME!
)

echo All jars have been downloaded.
pause

