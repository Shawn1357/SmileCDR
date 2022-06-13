@echo off

setlocal

SET SRC_DIR=%~dp0.\dist
SET DST_DIR=S:\DLQUtilities

XCOPY /S /E /V /Y /D %SRC_DIR%\*.* %DST_DIR%

PAUSE
endlocal
