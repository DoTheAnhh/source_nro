@echo off
REM =====================================================================
REM  run.bat - CACH DUY NHAT de chay server.
REM
REM  Truoc day co ba file gan giong nhau: run.bat, run-log.bat (chi khac o
REM  cho do log ra file) va start-all.bat. Chay sai cai thi server van len,
REM  log van sach, chi la khong co thay doi vua sua - ma khong co dau hieu
REM  nao chi ra dieu do. Rat ton thoi gian moi nhan ra.
REM
REM  Gio chi con file nay. No bien dich lai src/ roi chay, va GHI LOG ra
REM  log-run.txt dong thoi hien tren cua so, nen khong can ban thu hai.
REM  run-log.bat cu nam trong backupsrc/script-cu-*.
REM =====================================================================
title Anwin Version 3 Server
cd /d "%~dp0"

call "%~dp0env.bat"

echo ===============================
echo     Starting Anwin Version 3 Server
echo ===============================

REM --- Xoa ket qua bien dich cu ---
REM QUAN TRONG: out/ tung chua .class cua nhung tinh nang da go khoi src/
REM (MaiTienDungManager, NangCapDeTu...). Khong xoa thi cac class rac do van
REM nam tren classpath va van duoc nap luc chay -> sua code xong loi cu van con.
echo [CLEAN] Xoa thu muc out...
if exist out rmdir /s /q out
mkdir out

REM --- Build source ---
echo [BUILD] Compiling source... ^(javac = %JAVAC_EXE%^)

if exist sources.txt del sources.txt
dir /s /b src\*.java > sources.txt

REM -J-Xmx512m: gioi han heap cho chinh tien trinh javac. Mac dinh javac xin
REM mot phan tu bo nho vat ly; khi Unity dang mo thi khong con du va build
REM chet voi "paging file is too small" du ma nguon khong he co loi.
"%JAVAC_EXE%" -J-Xmx512m -J-Xms64m -encoding UTF-8 ^
 -cp "lib\*" ^
 -d out ^
 @sources.txt

if errorlevel 1 (
    echo [LOI] Build that bai! Kiem tra loi compile.
    pause
    exit /b
)

echo [OK] Build thanh cong!

REM --- Copy resource vao out ---
REM javac chi bien dich .java, KHONG copy .png. Ma MenuUI nap icon bang
REM getClass().getResource("/icons/...") tuc la doc tu classpath -> thieu
REM buoc nay thi sau khi clean out/ toan bo icon cua giao dien se bi null.
echo [RES] Copy icons/images...
xcopy /s /y /i /q "src\icons"  "out\icons"  >nul
xcopy /s /y /i /q "src\images" "out\images" >nul

REM --- Run server ---
REM -Duser.dir tro ve chinh thu muc nay, vi mo hinh doc du lieu cua server
REM dung duong dan tuong doi (data/config, data/map, backupsql...).
echo [RUN] Starting server...
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
