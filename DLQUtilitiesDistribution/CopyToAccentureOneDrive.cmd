@echo off

setlocal

SET SRC_DIR=%~dp0.\dist
SET DST_DIR="C:\Users\Shawn.Brant\OneDrive - Accenture\Ontario\DLQWatcher\dist"

XCOPY /S /E /V /Y /D %SRC_DIR%\*.* %DST_DIR%

PAUSE
endlocal
