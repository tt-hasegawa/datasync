@echo off
setlocal

REM Javaランタイムとクラスパスの設定
set JAVA_HOME=..\Tools\jre
set PATH=%JAVA_HOME%\bin;%PATH%
set CLASSPATH=..\Tools\lib\groovy-all-2.4.21.jar;..\Tools\lib\mssql-jdbc-12.10.0.jre8.jar

REM エクスポートツールの実行
java -cp %CLASSPATH% groovy.ui.GroovyMain export_tool.groovy ..\export_config.json

endlocal
