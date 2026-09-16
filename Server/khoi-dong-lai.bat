@echo off
title NRO
rem Script khoi dong lai dung duoc khi double-click tay.
rem File do Java sinh ra luc chay co the ghi de file nay bang lenh chi tiet hon.
timeout /t 5 >nul
cd /d "%~dp0"
call run.bat
