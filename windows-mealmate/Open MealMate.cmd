@echo off
setlocal
set "APP_FILE=%~dp0MealMate-app.html"
set "EDGE=%ProgramFiles(x86)%\Microsoft\Edge\Application\msedge.exe"
if not exist "%EDGE%" set "EDGE=%ProgramFiles%\Microsoft\Edge\Application\msedge.exe"
if exist "%EDGE%" (
  start "" "%EDGE%" --app="%APP_FILE%" --user-data-dir="%LOCALAPPDATA%\MealMateDesktop"
) else (
  start "" "%APP_FILE%"
)