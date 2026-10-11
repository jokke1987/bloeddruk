@echo off
echo ===========================================
echo   Bloeddruk App - Uploaden naar GitHub
echo ===========================================
echo.
git add .
git commit -m "Automatische update van app - %date% %time%"
git push origin main
echo.
echo ===========================================
echo   Klaar! Je wijzigingen staan op GitHub.
echo ===========================================
pause
