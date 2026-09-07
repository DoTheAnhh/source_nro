@echo off
title Start ALL - MySQL + AWN Server
cd /d "%~dp0"

call "%~dp0env.bat"

echo ===============================
echo   Khoi dong toan bo he thong
echo ===============================

if not defined XAMPP_DIR (
    echo [LOI] Khong tim thay XAMPP tren may nay.
    echo       Dat bien moi truong XAMPP_HOME tro toi thu muc XAMPP roi chay lai.
    pause
    exit /b 1
)
echo     XAMPP = %XAMPP_DIR%

echo [1/2] Kiem tra MySQL...
tasklist /FI "IMAGENAME eq mysqld.exe" | find /I "mysqld.exe" >nul
if errorlevel 1 (
    echo     MySQL chua chay - dang bat...
    if exist "%XAMPP_DIR%\mysql_start.bat" (
        start "MySQL - KHONG DUOC DONG CUA SO NAY" cmd /c "%XAMPP_DIR%\mysql_start.bat"
    ) else (
        start "MySQL - KHONG DUOC DONG CUA SO NAY" cmd /c ""%XAMPP_DIR%\mysql\bin\mysqld.exe" --defaults-file="%XAMPP_DIR%\mysql\bin\my.ini" --standalone"
    )
    echo     Cho MySQL khoi dong...
    timeout /t 10 /nobreak >nul
) else (
    echo     MySQL da chay san.
)

echo [2/2] Khoi dong game server...
call "%~dp0run.bat"
