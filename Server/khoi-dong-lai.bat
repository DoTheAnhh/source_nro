@echo off
title NRO
rem Tep nay do ServerManager.taoBatKhoiDongLai() tu ghi ra.
rem Doi cho tien trinh cu nha cong 14445 roi moi bat lai.
timeout /t 5 >nul
cd /d "F:\NgocRong\Server"
"C:\Program Files\Java\jdk-21.0.12.1\bin\java.exe" -Xms128m -Xmx3g -Xss256k -XX:CompressedClassSpaceSize=128m -XX:ReservedCodeCacheSize=128m -XX:CICompilerCount=2 -cp "C:/Users/admin/AppData/Local/Temp/claude/f--NgocRong/00365c0d-3d7a-49b3-8ffa-e4c4235c0b64/scratchpad;out;lib/flatlaf-3.4.1.jar;lib/flatlaf-intellij-themes-3.6.1.jar;lib/NgocRongOnline.jar;lib/okhttp-3.0.0.jar;lib/pcap4j-core-1.7.4.jar" nro.server.ServerManager > server-console.log 2>&1
