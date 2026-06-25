@echo off
setlocal
set "TARGET=%~dp0Open MealMate.cmd"
powershell -NoProfile -ExecutionPolicy Bypass -Command "$desktop=[Environment]::GetFolderPath('Desktop'); $shortcut=(New-Object -ComObject WScript.Shell).CreateShortcut((Join-Path $desktop 'MealMate.lnk')); $shortcut.TargetPath=$env:TARGET; $shortcut.WorkingDirectory=Split-Path $env:TARGET; $shortcut.IconLocation='%SystemRoot%\System32\shell32.dll,44'; $shortcut.Save()"
echo MealMate shortcut created on the Desktop.
pause