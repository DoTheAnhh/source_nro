@echo off
REM =====================================================================
REM  setup-web.bat - Tro Apache ve thu muc htdocs cua du an NAY.
REM
REM  CHAY KHI NAO: sau khi chep / di chuyen ca thu muc du an sang cho khac
REM  (o dia khac, may khac). Chay mot lan la xong.
REM
REM  Script tu xac dinh vi tri cua chinh no, nen khong co duong dan cung.
REM =====================================================================
setlocal
cd /d "%~dp0"

call "%~dp0env.bat"

if not defined XAMPP_DIR (
    echo [LOI] Khong tim thay XAMPP tren may nay.
    echo       Dat bien moi truong XAMPP_HOME tro toi thu muc XAMPP roi chay lai.
    pause
    exit /b 1
)

echo ===============================
echo   Tro Apache ve du an nay
echo ===============================
echo   XAMPP   = %XAMPP_DIR%
echo   htdocs  = %~dp0htdocs
echo.

powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0setup-web.ps1" -XamppDir "%XAMPP_DIR%" -WebRoot "%~dp0htdocs"
if errorlevel 1 (
    echo.
    echo [LOI] Cap nhat cau hinh that bai.
    pause
    exit /b 1
)

echo.
echo [i] Khoi dong lai Apache de nhan cau hinh moi...
if exist "%XAMPP_DIR%\apache_stop.bat"  call "%XAMPP_DIR%\apache_stop.bat"
if exist "%XAMPP_DIR%\apache_start.bat" (
    start "Apache" cmd /c ""%XAMPP_DIR%\apache_start.bat""
) else (
    echo [!] Khong thay apache_start.bat - hay bat Apache bang XAMPP Control Panel.
)

echo.
echo Xong. Mo http://localhost de kiem tra.
pause
endlocal
