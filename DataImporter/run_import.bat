@echo off
setlocal enabledelayedexpansion

REM Javaランタイムとクラスパスの設定
set JAVA_HOME=..\Tools\jre
set PATH=%JAVA_HOME%\bin;%PATH%
set CLASSPATH=
for %%f in (..\Tools\lib\*.jar) do (
    set CLASSPATH=!CLASSPATH!;%%f
)

echo !CLASSPATH!
REM インポートツールの実行
java -cp !CLASSPATH! groovy.ui.GroovyMain ImportTool.groovy import_config.json

endlocal
