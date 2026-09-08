@echo off
REM =====================================================================
REM  chay.bat - CHAY may chu tren may trien khai (VPS). KHONG bien dich.
REM
REM  CACH THUONG DUNG LA run.bat, KHONG PHAI FILE NAY.
REM
REM      git pull
REM      run.bat
REM
REM  run.bat xoa out\ roi javac lai tu src\, nen no luon chay dung ban vua
REM  keo ve. Tu 08/09/2026 out\ va AWN_Version.jar da BO THEO DOI trong git
REM  (xem Server/.gitignore), nen bien dich lai o may chay khong con lam
REM  ket `git pull` nua - do la ly do truoc day phai tranh run.bat.
REM
REM  File nay chi con dung cho MOT canh: may khong co javac. No goi thang
REM  java tren out\ da bien dich san. Neu out\ trong hoac thieu class thi no
REM  bao ngay chu khong de java nem ClassNotFound kho hieu.
REM =====================================================================
title Anwin Version 3 Server
cd /d "%~dp0"

call "%~dp0env.bat"

if not exist "out\nro\server\ServerManager.class" (
    echo.
    echo [LOI] Khong thay out\nro\server\ServerManager.class
    echo.
    echo       out\ khong nam trong git nua - phai bien dich truoc.
    echo       Cach don gian nhat: dong cua so nay va chay run.bat,
    echo       no tu bien dich roi chay luon.
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
