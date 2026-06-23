@echo off
setlocal enabledelayedexpansion
title 数据库初始化 - WebTrackDB

echo ============================================
echo   上网记录跟踪软件 - 数据库初始化
echo ============================================
echo.

where sqlcmd >nul 2>&1
if not errorlevel 1 goto sqlcmd_found

echo [错误] 未找到 sqlcmd 命令。
echo 请确保已安装 SQL Server 并已将 sqlcmd 加入系统 PATH。
echo 下载地址: https://learn.microsoft.com/zh-cn/sql/tools/sqlcmd/
echo.
pause
exit /b 1

:sqlcmd_found
echo 请选择身份验证方式：
echo   [1] Windows 身份验证（无需密码，推荐）
echo   [2] SQL Server 身份验证（需输入用户名和密码）
echo.
set /p auth=请输入选择 (1 或 2):

if "%auth%"=="1" (
    set "CONNECT=-S localhost,1433 -E"
    goto run_init
)
if "%auth%"=="2" goto sql_auth

echo 输入无效，请重新运行。
pause
exit /b 1

:sql_auth
echo.
set /p sql_user=请输入用户名（默认 sa）:
if "!sql_user!"=="" set sql_user=sa
set /p sql_pass=请输入密码:
set "CONNECT=-S localhost,1433 -U !sql_user! -P !sql_pass!"
goto run_init

:run_init
set "LOGFILE=%~dp0init.log"
set "SQLFILE=%~dp0database\init.sql"

echo.
echo [步骤1] 测试数据库连接...
echo.
sqlcmd %CONNECT% -b -o "%LOGFILE%" -Q "PRINT 'Connection OK';"
if errorlevel 1 (
    echo [失败] 无法连接 SQL Server，请检查：
    echo   1. SQL Server 服务是否启动（运行 services.msc）
    echo   2. 端口 1433 是否被防火墙拦截
    echo   3. 连接参数是否正确
    echo.
    echo 详细错误信息:
    type "%LOGFILE%"
    pause
    exit /b 1
)
echo [通过] 连接成功。
echo.

echo [步骤2] 执行初始化脚本...
sqlcmd %CONNECT% -b -d master -o "%LOGFILE%" -i "%SQLFILE%"
if %errorlevel% equ 0 (
    echo [成功] 数据库初始化完成！
) else (
    echo [失败] 初始化过程中出现错误：
    type "%LOGFILE%"
    echo.
    echo 可能原因：
    echo   1. 没有 CREATE DATABASE 权限
    echo   2. 数据库文件路径无写入权限
    echo   3. SQL 语法不兼容当前 SQL Server 版本
)
echo.
pause
