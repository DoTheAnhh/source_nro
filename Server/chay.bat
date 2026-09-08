@echo off
REM =====================================================================
REM  chay.bat - CHAY may chu tren may trien khai (VPS). KHONG bien dich.
REM
REM  DUNG CAI NAY O VPS, KHONG DUNG run.bat.
REM
REM  Khac nhau o dau, va vi sao quan trong:
REM
REM    run.bat  = danh cho may lam viec. No XOA sach out\ roi javac lai
REM               toan bo src\, va sinh lai sources.txt.
REM    chay.bat = danh cho may chay. No chi goi java tren out\ da co san
REM               trong git.
REM
REM  Chay run.bat o VPS thi out\*.class va sources.txt bi ghi de bang ban
REM  do chinh may do bien dich. Git thay chung "da bi sua o ca hai dau" va
REM  tu choi keo ve:
REM      error: Your local changes to the following files would be
REM             overwritten by merge
REM  Da xay ra dung nhu vay ngay 08/09/2026, phai reset --hard moi go duoc.
REM  sources.txt gio da bo theo doi, nhung out\ thi VAN theo doi - phai the,
REM  vi do la cach de VPS khong can javac. Nen dung run.bat o do la kep lai
REM  ngay.
REM
REM  Quy trinh o VPS chi con hai buoc:
REM      git pull
REM      chay.bat
REM =====================================================================
title Anwin Version 3 Server
cd /d "%~dp0"

call "%~dp0env.bat"

if not exist "out\nro\server\ServerManager.class" (
    echo.
    echo [LOI] Khong thay out\nro\server\ServerManager.class
    echo.
    echo       Thu muc out\ nam trong git, dang le phai co san sau khi
    echo       "git pull". Kiem tra lai:
    echo           git status
    echo           git pull
    echo.
    pause
    exit /b 1
)

echo ===============================
echo     Anwin Version 3 Server
echo     ^(chay tu out\ da bien dich san - khong build lai^)
echo ===============================

REM Giu nguyen cac tham so bo nho nhu run.bat.
REM -Duser.dir tro ve chinh thu muc nay: mo hinh doc du lieu cua may chu
REM dung duong dan tuong doi (data/config, data/map, backupsql...).
"%JAVA_EXE%" -Xms128m -Xmx3g -Xss256k ^
 -XX:CompressedClassSpaceSize=128m ^
 -XX:ReservedCodeCacheSize=128m ^
 -XX:CICompilerCount=2 ^
 -cp "out;lib\*" ^
 -Duser.dir="%cd%" ^
 nro.server.ServerManager %*

echo ===============================
echo     Server da dung
echo ===============================
pause
