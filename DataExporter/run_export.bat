@echo off
setlocal enabledelayedexpansion

REM Javaランタイムとクラスパスの設定
set JAVA_HOME=..\Tools\jre
set PATH=%JAVA_HOME%\bin;%PATH%
set CLASSPATH=
for %%f in (..\Tools\lib\*.jar) do (
    set CLASSPATH=!CLASSPATH!;%%f
)

REM エクスポートツールの実行
java -cp !CLASSPATH! groovy.ui.GroovyMain ExportTool.groovy export_config.json

endlocal
pause
