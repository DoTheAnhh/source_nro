@echo off
REM =====================================================================
REM  run-log.bat - Bien dich src/ roi chay server, GHI LOG RA FILE
REM
REM  Khac run.bat duy nhat o cho: moi thu in ra deu do vao
REM  server-console.log thay vi hien tren cua so. Dung khi can gui log
REM  cho nguoi khac xem, hoac soi lai loi da troi qua.
REM
REM  BUOC BIEN DICH LA BAT BUOC. Truoc day file nay chi chay thang out/,
REM  nen sua ma nguon xong chay no la chay lai BAN CU ma khong bao gi ca:
REM  log van sach, server van len, chi la khong co thay doi vua sua. Rat
REM  ton thoi gian moi nhan ra, vi khong co dau hieu nao chi ra dieu do.
REM =====================================================================
setlocal
cd /d "%~dp0"

call "%~dp0env.bat"

echo ===============================
echo   Bien dich roi chay server
echo   File log: server-console.log
echo ===============================
echo.
echo LUU Y: phai tat server cu truoc khi chay cai nay!
echo.

echo [CLEAN] Xoa thu muc out...
if exist out rmdir /s /q out
mkdir out

echo [BUILD] Compiling source... ^(javac = %JAVAC_EXE%^)
if exist sources.txt del sources.txt
dir /s /b src\*.java > sources.txt
"%JAVAC_EXE%" -J-Xmx512m -J-Xms64m -encoding UTF-8 ^
 -cp "lib\*" ^
 -d out ^
 @sources.txt
if errorlevel 1 (
    echo [LOI] Build that bai! Kiem tra loi compile.
    pause
    exit /b 1
)
echo [OK] Build thanh cong!

echo [RES] Copy icons/images...
xcopy /s /y /i /q "src\icons"  "out\icons"  >nul
xcopy /s /y /i /q "src\images" "out\images" >nul

echo [RUN] Starting server... ^(log: server-console.log^)
"%JAVA_EXE%" -Xms128m -Xmx3g -Xss256k ^
 -XX:CompressedClassSpaceSize=128m ^
 -XX:ReservedCodeCacheSize=128m ^
 -XX:CICompilerCount=2 ^
 -cp "out;lib\*" ^
 -Duser.dir="%cd%" ^
 nro.server.ServerManager %* > server-console.log 2>&1

echo Server da dung. Xem log trong server-console.log
pause
endlocal
