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

REM --- Dau van tay cua src/: so tep + tong dung luong + gio sua moi nhat ---
REM Giong het lan build truoc (luu o out\.dau_van_tay) thi BO QUA bien dich,
REM chay luon. Khac (git pull co thay doi, sua tay, XOA tep) thi don sach out/
REM va bien dich lai tu dau nhu cu. Chay lai server khi khong doi gi: vai giay
REM thay vi mot phut.
set "DAU_MOI="
for /f "delims=" %%H in ('powershell -NoProfile -Command "$f = Get-ChildItem -Path src -Recurse -File; $m = ($f | Measure-Object -Property Length -Sum); $t = ($f | Measure-Object -Property LastWriteTimeUtc -Maximum).Maximum.Ticks; '' + $m.Count + '-' + $m.Sum + '-' + $t"') do set "DAU_MOI=%%H"
set "DAU_CU="
if exist "out\.dau_van_tay" set /p DAU_CU=<"out\.dau_van_tay"
if defined DAU_MOI if "%DAU_MOI%"=="%DAU_CU%" if exist "out\nro\server\ServerManager.class" (
    echo [NHANH] src/ khong doi tu lan build truoc - bo qua bien dich.
    goto chay
)

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

REM Thong so javac (do tren 763 tep: 69 giay -> 36 giay):
REM   -J-Xmx1536m  : 512m cu qua chat, javac mat phan lon thoi gian don rac.
REM                  Server chua chay luc nay nen bo nho du.
REM   ParallelGC   : don rac nhieu luong, nhanh hon cho mot lan chay ngan.
REM   TieredStopAtLevel=1 : JIT khoi dong nhanh — javac chay mot lan roi thoi.
REM   -g:source,lines : giu so dong trong log loi (bo -g:vars cho gon).
"%JAVAC_EXE%" -J-Xmx1536m -J-Xms256m -J-XX:+UseParallelGC -J-XX:TieredStopAtLevel=1 ^
 -encoding UTF-8 -nowarn -Xlint:none -g:source,lines ^
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

REM Ghi dau van tay SAU CUNG — build hong giua chung thi lan sau van build lai.
if defined DAU_MOI >"out\.dau_van_tay" echo %DAU_MOI%

:chay

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
