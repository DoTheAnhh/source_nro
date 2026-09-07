@echo off
REM =====================================================================
REM  build.bat - Bien dich src/ va dong goi AWN_Version.jar (fat jar)
REM  Chay: build.bat
REM  Ket qua:
REM    out/               .class + resource (dung cho run.bat)
REM    AWN_Version.jar    artifact chay duoc doc lap (java -jar)
REM
REM  Moi duong dan deu tuong doi theo vi tri file nay (%~dp0), cong cu JDK
REM  do env.bat tu tim tren may -> chep ca thu muc du an di dau cung chay.
REM =====================================================================
setlocal
cd /d "%~dp0"

call "%~dp0env.bat"

echo [1/5] Xoa ket qua bien dich cu...
if exist out rmdir /s /q out
if exist build\jarstage rmdir /s /q build\jarstage
mkdir out
mkdir build\jarstage

echo [2/5] Liet ke source...
if exist sources.txt del sources.txt
dir /s /b src\*.java > sources.txt

echo [3/5] Bien dich... ^(javac = %JAVAC_EXE%^)
REM -J-Xmx512m: gioi han heap cho chinh tien trinh javac. Mac dinh javac xin
REM mot phan tu bo nho vat ly; khi Unity dang mo thi khong con du va build
REM chet voi "paging file is too small" du ma nguon khong he co loi.
"%JAVAC_EXE%" -J-Xmx512m -J-Xms64m -encoding UTF-8 -cp "lib\*" -d out @sources.txt
if errorlevel 1 (
    echo [LOI] Bien dich that bai.
    exit /b 1
)

echo [4/5] Copy resource ^(icons, images^)...
xcopy /s /y /i /q "src\icons"  "out\icons"  >nul
xcopy /s /y /i /q "src\images" "out\images" >nul

REM  jar/javac khong phai luc nao cung nam trong PATH - env.bat da do san.
REM  Khong lam viec nay thi buoc 5 chi in "'jar' is not recognized" roi bo qua,
REM  va AWN_Version.jar giu nguyen ban CU trong khi out/ da moi.
echo       jar = %JAR_EXE%

REM jar ghi tep tam vao java.io.tmpdir, mac dinh nam tren o he thong. O do day
REM thi jar bao "not enough space on the disk" du o du an con trong hang tram GB.
REM Tro tmpdir vao ngay canh du an de khong phu thuoc cho trong cua o he thong.
set "TMPJAR=%~dp0_tmp"
if not exist "%TMPJAR%" mkdir "%TMPJAR%"
set "JAROPT=-J-Djava.io.tmpdir=%TMPJAR%"

echo [5/5] Dong goi fat jar...
REM Bung tat ca thu vien trong lib/ vao staging roi chong .class cua minh len,
REM de jar chay duoc bang "java -jar" ma khong can classpath ngoai.
for %%J in (lib\*.jar) do (
    pushd build\jarstage
    "%JAR_EXE%" --extract --file "..\..\%%J"
    popd
)
if exist build\jarstage\META-INF\MANIFEST.MF del build\jarstage\META-INF\MANIFEST.MF
del /q build\jarstage\META-INF\*.SF  2>nul
del /q build\jarstage\META-INF\*.RSA 2>nul
del /q build\jarstage\META-INF\*.DSA 2>nul

xcopy /s /y /i /q "out\*" "build\jarstage\" >nul

"%JAR_EXE%" %JAROPT% --create --file AWN_Version.jar --main-class nro.server.ServerManager -C build\jarstage .
if errorlevel 1 (
    echo [LOI] Dong goi jar that bai.
    exit /b 1
)

rmdir /s /q build\jarstage

echo.
echo ==========================================
echo  Build xong.
echo    out/            -^> dung cho run.bat
echo    AWN_Version.jar -^> java -jar AWN_Version.jar [--headless]
echo ==========================================
endlocal
