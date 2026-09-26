@echo off
REM =====================================================================
REM  run.bat - CACH DUY NHAT de chay server.
REM
REM  Bien dich bang tools\BuildNhanh (xem chu thich trong tep do):
REM    - src/ khong doi           -> bo qua, chay ngay;
REM    - doi vai tep (git pull)   -> chi dich tep doi + tep nhac toi chung;
REM    - xoa tep / lan dau / doi nhieu -> xoa out/, dich lai het.
REM  Dich ngay trong mot JVM (khong mo them javac), khong con buoc PowerShell.
REM
REM  Log: chi hien cai quan trong, khong ma mau (cua so cmd tren VPS khong hieu
REM  ma mau ANSI -> ra ky tu rac), chu tieng Viet UTF-8.
REM  Xem day du khi do loi: set NRO_JVM=-Dnro.logdaydu=true  roi chay run.bat.
REM =====================================================================
title Anwin Version 3 Server
cd /d "%~dp0"
chcp 65001 >nul

call "%~dp0env.bat"

echo ===============================
echo     Starting Anwin Version 3 Server
echo ===============================

if not exist "tools\BuildNhanh.class" goto dich_cong_cu

:build
"%JAVA_EXE%" -Xmx1536m -Xms256m -XX:+UseParallelGC -XX:TieredStopAtLevel=1 -Dfile.encoding=UTF-8 -cp tools BuildNhanh
if errorlevel 3 goto dich_cong_cu
if errorlevel 1 (
    echo [LOI] Build that bai! Kiem tra loi compile.
    pause
    exit /b
)
goto chay

:dich_cong_cu
REM Cong cu build moi / vua sua: dich lai no (mot tep, vai giay).
"%JAVAC_EXE%" -encoding UTF-8 -nowarn -d tools tools\BuildNhanh.java
if errorlevel 1 (
    echo [LOI] Khong dich duoc tools\BuildNhanh.java
    pause
    exit /b
)
goto build

:chay

REM --- Run server ---
REM -Duser.dir tro ve chinh thu muc nay, vi mo hinh doc du lieu cua server
REM dung duong dan tuong doi (data/config, data/map, backupsql...).
echo [RUN] Starting server...
"%JAVA_EXE%" -Xms128m -Xmx3g -Xss256k ^
 -XX:CompressedClassSpaceSize=128m ^
 -XX:ReservedCodeCacheSize=128m ^
 -XX:CICompilerCount=2 ^
 -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 ^
 -Dnro.khongmau=true ^
 -cp "out;lib\*" ^
 -Duser.dir="%cd%" ^
 %NRO_JVM% nro.server.ServerManager %*

echo ===============================
echo     Server da dung
echo ===============================
pause
