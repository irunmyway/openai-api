@echo off
set JAVA_OPTS=-Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8 -Duser.timezone=Asia/Shanghai
chcp 65001 >nul
echo "Starting with UTF-8 encoding..."
java %JAVA_OPTS% -jar target/tencent-llm-api-1.0.0.jar