@echo off
REM =====================================================================
REM  env.bat - Do tim moi truong (XAMPP, PHP, MySQL, JDK) tren may dang chay.
REM
REM  MUC DICH: khong nhung cung duong dan tuyet doi vao bat cu file nao,
REM  de chep ca thu muc du an sang may khac / o dia khac van chay duoc.
REM
REM  CACH DUNG:  call "%~dp0env.bat"
REM
REM  Sau khi goi, cac bien duoi day duoc dat (neu tim thay tren may):
REM    PROJ_DIR        thu muc Server (co dau \ o cuoi)
REM    XAMPP_DIR       thu muc goc XAMPP
REM    PHP_EXE         php.exe
REM    MYSQL_EXE       mysql.exe
REM    MYSQLDUMP_EXE   mysqldump.exe
REM    JAVA_EXE        java.exe
REM    JAVAC_EXE       javac.exe
REM    JAR_EXE         jar.exe
REM
REM  MUON EP CUNG: dat san bien moi truong XAMPP_HOME hoac JAVA_HOME
REM  truoc khi chay, script se uu tien dung gia tri do.
REM =====================================================================

set "PROJ_DIR=%~dp0"

REM ------------------------------------------------------------------
REM  1) Tim thu muc XAMPP
REM     Thu tu: XAMPP_HOME -> xampp di kem canh du an -> theo PATH ->
REM             quet cac o dia thong dung
REM ------------------------------------------------------------------
set "XAMPP_DIR="

if defined XAMPP_HOME if exist "%XAMPP_HOME%\php\php.exe" set "XAMPP_DIR=%XAMPP_HOME%"

REM xampp dat ngay canh thu muc du an (kieu ban portable)
if not defined XAMPP_DIR if exist "%PROJ_DIR%..\xampp\php\php.exe" (
    for %%D in ("%PROJ_DIR%..\xampp") do set "XAMPP_DIR=%%~fD"
)

REM suy nguoc tu php.exe co san trong PATH
if not defined XAMPP_DIR (
    for /f "delims=" %%P in ('where php 2^>nul') do (
        if not defined XAMPP_DIR (
            for %%R in ("%%~dpP..") do if exist "%%~fR\mysql\bin\mysql.exe" set "XAMPP_DIR=%%~fR"
        )
    )
)

REM quet o dia
if not defined XAMPP_DIR (
    for %%D in (C D E F G H) do (
        if not defined XAMPP_DIR if exist "%%D:\xampp\php\php.exe" set "XAMPP_DIR=%%D:\xampp"
    )
)

REM ------------------------------------------------------------------
REM  2) PHP
REM ------------------------------------------------------------------
set "PHP_EXE="
if defined XAMPP_DIR if exist "%XAMPP_DIR%\php\php.exe" set "PHP_EXE=%XAMPP_DIR%\php\php.exe"
if not defined PHP_EXE (
    for /f "delims=" %%P in ('where php 2^>nul') do if not defined PHP_EXE set "PHP_EXE=%%P"
)

REM ------------------------------------------------------------------
REM  3) MySQL / mysqldump
REM ------------------------------------------------------------------
set "MYSQL_EXE="
set "MYSQLDUMP_EXE="
if defined XAMPP_DIR (
    if exist "%XAMPP_DIR%\mysql\bin\mysql.exe"     set "MYSQL_EXE=%XAMPP_DIR%\mysql\bin\mysql.exe"
    if exist "%XAMPP_DIR%\mysql\bin\mysqldump.exe" set "MYSQLDUMP_EXE=%XAMPP_DIR%\mysql\bin\mysqldump.exe"
)
if not defined MYSQL_EXE (
    for /f "delims=" %%P in ('where mysql 2^>nul') do if not defined MYSQL_EXE set "MYSQL_EXE=%%P"
)
if not defined MYSQLDUMP_EXE (
    for /f "delims=" %%P in ('where mysqldump 2^>nul') do if not defined MYSQLDUMP_EXE set "MYSQLDUMP_EXE=%%P"
)

REM ------------------------------------------------------------------
REM  4) JDK - can javac va jar de build, java de chay
REM     Thu tu: JAVA_HOME -> PATH -> quet Program Files
REM ------------------------------------------------------------------
set "JAVA_EXE="
set "JAVAC_EXE="
set "JAR_EXE="

if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\java.exe"  set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
    if exist "%JAVA_HOME%\bin\javac.exe" set "JAVAC_EXE=%JAVA_HOME%\bin\javac.exe"
    if exist "%JAVA_HOME%\bin\jar.exe"   set "JAR_EXE=%JAVA_HOME%\bin\jar.exe"
)

if not defined JAVA_EXE (
    for /f "delims=" %%P in ('where java 2^>nul') do if not defined JAVA_EXE set "JAVA_EXE=%%P"
)
if not defined JAVAC_EXE (
    for /f "delims=" %%P in ('where javac 2^>nul') do if not defined JAVAC_EXE set "JAVAC_EXE=%%P"
)
if not defined JAR_EXE (
    for /f "delims=" %%P in ('where jar 2^>nul') do if not defined JAR_EXE set "JAR_EXE=%%P"
)

REM javac co trong PATH thi jar hau nhu chac chan nam canh no
if not defined JAR_EXE if defined JAVAC_EXE (
    for %%P in ("%JAVAC_EXE%") do if exist "%%~dpPjar.exe" set "JAR_EXE=%%~dpPjar.exe"
)

REM Quet cac thu muc cai JDK quen thuoc.
REM Kiem tra tung cong cu MOT CACH DOC LAP: tren may co the co javac trong PATH
REM (qua javapath cua Oracle) nhung KHONG co jar.exe canh do -> buoc dong goi
REM se im lang that bai va AWN_Version.jar giu nguyen ban cu.
if not defined JAVAC_EXE set "NEED_JDK_SCAN=1"
if not defined JAR_EXE   set "NEED_JDK_SCAN=1"
if defined NEED_JDK_SCAN (
    for %%B in ("%ProgramFiles%\Java" "%ProgramFiles%\Eclipse Adoptium" "%ProgramFiles%\Microsoft" "%ProgramFiles%\Amazon Corretto" "%ProgramFiles%\Apache NetBeans" "%ProgramFiles%\Android\Android Studio") do (
        if exist "%%~B" (
            for /d %%J in ("%%~B\*") do (
                if not defined JAVAC_EXE if exist "%%~J\bin\javac.exe" set "JAVAC_EXE=%%~J\bin\javac.exe"
                if not defined JAR_EXE   if exist "%%~J\bin\jar.exe"   set "JAR_EXE=%%~J\bin\jar.exe"
                if not defined JAVA_EXE  if exist "%%~J\bin\java.exe"  set "JAVA_EXE=%%~J\bin\java.exe"
            )
        )
    )
    if exist "%ProgramFiles%\Apache NetBeans\jdk\bin\jar.exe" if not defined JAR_EXE set "JAR_EXE=%ProgramFiles%\Apache NetBeans\jdk\bin\jar.exe"
)
set "NEED_JDK_SCAN="

REM cuoi cung: dung ten tran, de he dieu hanh tu tim trong PATH
if not defined JAVA_EXE  set "JAVA_EXE=java"
if not defined JAVAC_EXE set "JAVAC_EXE=javac"
if not defined JAR_EXE   set "JAR_EXE=jar"

exit /b 0
