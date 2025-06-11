@echo off
setlocal enabledelayedexpansion

REM Javaランタイムとクラスパスの設定
set JAVA_HOME=..\Tools\jre
set PATH=%JAVA_HOME%\bin;%PATH%
set CLASSPATH=
for %%f in (..\Tools\lib\*.jar) do (
    if not "!CLASSPATH!"=="" (
        set CLASSPATH=!CLASSPATH!;%%f
    ) else (
        set CLASSPATH=%%f
    )
)

REM DataExporterとDataImporterフォルダをクラスパスに追加
set CLASSPATH=!CLASSPATH!;..\DataExporter;..\DataImporter

echo !CLASSPATH!
REM テストスクリプトの実行
echo Running Export Tool Test...
java -cp "!CLASSPATH!" groovy.ui.GroovyMain test_export_tool.groovy
if %ERRORLEVEL% neq 0 (
    echo Export Tool Test Failed.
    exit /b %ERRORLEVEL%
)

echo Running Import Tool Test...
java -cp "!CLASSPATH!" groovy.ui.GroovyMain test_import_tool.groovy
if %ERRORLEVEL% neq 0 (
    echo Import Tool Test Failed.
    exit /b %ERRORLEVEL%
)

echo All Tests Passed Successfully.
endlocal
