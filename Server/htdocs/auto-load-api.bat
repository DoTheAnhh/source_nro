@echo off
title Auto Load API MBBank

REM Chay ngay tai thu muc chua file .bat nay, va lay php.exe do env.bat
REM tu tim tren may -> khong phu thuoc vao o dia hay thu muc cai dat.
cd /d "%~dp0"
call "%~dp0..\env.bat"

if not defined PHP_EXE (
    echo [LOI] Khong tim thay php.exe tren may nay.
    echo       Dat bien moi truong XAMPP_HOME tro toi thu muc XAMPP roi chay lai.
    pause
    exit /b 1
)

echo ===============================
echo    AUTO LOAD API MBBANK
echo    Chay moi 30 giay
echo    php = %PHP_EXE%
echo ===============================
echo.

:loop
echo [%date% %time%] Dang load API...
"%PHP_EXE%" auto-load-api.php

echo [%date% %time%] Hoan thanh. Cho 30 giay...
echo.
timeout /t 30 /nobreak >nul
goto loop
