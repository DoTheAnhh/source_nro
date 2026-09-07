<#
  setup-web.ps1 - Tro DocumentRoot cua Apache ve thu muc htdocs cua du an,
  o BAT KY vi tri nao du an dang nam.

  Khong goi truc tiep - hay chay setup-web.bat.

  Ly do can script nay: httpd.conf nam trong thu muc cai XAMPP, KHONG di theo
  khi ban chep du an sang may khac. Sang may moi chi can chay setup-web.bat
  mot lan la Apache tro dung cho, khong phai sua tay.
#>
param(
    [Parameter(Mandatory = $true)] [string] $XamppDir,
    [Parameter(Mandatory = $true)] [string] $WebRoot
)

$ErrorActionPreference = 'Stop'

$conf = Join-Path $XamppDir 'apache\conf\httpd.conf'
if (-not (Test-Path $conf)) {
    Write-Host "[LOI] Khong thay httpd.conf tai: $conf" -ForegroundColor Red
    exit 1
}

# Apache chi hieu duong dan dung dau / va khong co dau / o cuoi
$web = (Resolve-Path $WebRoot).Path.Replace([char]92, '/')
$web = $web.TrimEnd('/')

$lines = Get-Content -LiteralPath $conf -Encoding UTF8

# Lay gia tri DocumentRoot hien tai de biet block <Directory> nao di kem no.
# Trong httpd.conf co nhieu block <Directory> (cgi-bin, / ...) - chi duoc doi
# dung block cua DocumentRoot, doi nham la Apache khong khoi dong duoc.
$old = $null
foreach ($l in $lines) {
    if ($l -match '^\s*DocumentRoot\s+"(.+?)"\s*$') { $old = $Matches[1]; break }
}
if (-not $old) {
    Write-Host "[LOI] Khong doc duoc DocumentRoot hien tai trong httpd.conf" -ForegroundColor Red
    exit 1
}

if ($old -eq $web) {
    Write-Host "[OK] DocumentRoot da dung roi: $web" -ForegroundColor Green
    exit 0
}

$backup = "$conf.bak-" + (Get-Date -Format 'yyyyMMdd_HHmmss')
Copy-Item -LiteralPath $conf -Destination $backup
Write-Host "[i] Da sao luu: $backup"

$oldEsc = [regex]::Escape($old)
$out = foreach ($l in $lines) {
    if ($l -match "^\s*DocumentRoot\s+`"$oldEsc`"\s*$") { "DocumentRoot `"$web`"" }
    elseif ($l -match "^\s*<Directory\s+`"$oldEsc`"\s*>\s*$") { "<Directory `"$web`">" }
    else { $l }
}

Set-Content -LiteralPath $conf -Value $out -Encoding UTF8

# Kiem tra cu phap NGAY. Sai thi tra lai ban cu, tuyet doi khong de Apache
# hong config ma khong ai biet.
$httpd = Join-Path $XamppDir 'apache\bin\httpd.exe'
# httpd -t ghi ket qua ra stderr ke ca khi thanh cong. Trong Windows PowerShell
# 5.1, redirect stderr cua mot .exe se boc moi dong thanh ErrorRecord va voi
# ErrorActionPreference='Stop' se thanh loi terminating - du httpd tra ve 0.
# Vi vay ha ErrorActionPreference trong dung loi goi nay roi dua lai.
$prevEAP = $ErrorActionPreference
$ErrorActionPreference = 'Continue'
$check = (& $httpd -t 2>&1 | ForEach-Object { $_.ToString() }) -join ' '
$code = $LASTEXITCODE
$ErrorActionPreference = $prevEAP

if ($code -ne 0) {
    Copy-Item -LiteralPath $backup -Destination $conf -Force
    Write-Host "[LOI] Cu phap httpd.conf sai - da khoi phuc ban cu." -ForegroundColor Red
    Write-Host $check
    exit 1
}

Write-Host "[OK] $old"
Write-Host "     -> $web" -ForegroundColor Green
Write-Host "[OK] httpd -t: $check"
exit 0
