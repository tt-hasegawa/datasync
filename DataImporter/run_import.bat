@echo off
setlocal

REM Javaランタイムとクラスパスの設定
set JAVA_HOME=..\Tools\jre
set PATH=%JAVA_HOME%\bin;%PATH%
set CLASSPATH=..\Tools\lib\groovy-all-2.4.21.jar;..\Tools\lib\ojdbc8-23.6.0.24.10.jar;..\Tools\lib\postgresql-42.7.5.jar

REM インポートツールの実行
REM 第一引数: JDBC URL
REM 第二引数: JDBC ユーザー名
REM 第三引数: JDBC パスワード
REM 第四引数: JDBC ドライバ
REM 第五引数: インポートするCSVファイルのパス
REM 第六引数: インポート先のテーブル名
java -cp %CLASSPATH% groovy.ui.GroovyMain import_tool.groovy %1 %2 %3 %4 %5 %6

endlocal
