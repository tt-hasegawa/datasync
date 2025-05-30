@echo off
setlocal

REM Javaランタイムとクラスパスの設定
set JAVA_HOME=..\Tools\jre
set PATH=%JAVA_HOME%\bin;%PATH%
set CLASSPATH=..\Tools\lib\groovy-all-2.4.21.jar;..\Tools\lib\ojdbc8-23.6.0.24.10.jar;..\Tools\lib\postgresql-42.7.5.jar

REM インポートツールの実行
REM 第一引数: インポート先フォルダ
REM 第二引数: インポートするCSVファイルのパス
REM 第三引数: インポート先のテーブル名
set IMPORT_DIR=path\to\import\directory
java -cp %CLASSPATH% groovy.ui.GroovyMain import_tool.groovy %IMPORT_DIR% %1 %2

endlocal
