@echo off
setlocal enabledelayedexpansion
title Smart IoT Launcher
set "ROOT=%~dp0"

echo ============================================================
echo   Smart IoT Quality Inspection - launcher
echo ============================================================
echo.

REM --- Find a Java 21 (preferred) or 23 JDK for the backend ---
REM    The default 'java' may be JDK 25, which Spring Boot 3.4 does not
REM    fully support at runtime, so we point JAVA_HOME at a known-good JDK.
set "JDK="
for %%P in (
  "C:\Program Files\Java\jdk-21"
  "C:\Program Files\Eclipse Adoptium\jdk-21"
  "C:\Program Files\Microsoft\jdk-21"
  "C:\Program Files\Java\jdk-23"
  "C:\Program Files\Eclipse Adoptium\jdk-23"
  "C:\Program Files\Microsoft\jdk-23"
) do if not defined JDK if exist "%%~P\bin\java.exe" set "JDK=%%~P"

if not defined JDK for /d %%P in ("C:\Program Files\Eclipse Adoptium\jdk-21*") do if not defined JDK set "JDK=%%~P"
if not defined JDK for /d %%P in ("C:\Program Files\Eclipse Adoptium\jdk-23*") do if not defined JDK set "JDK=%%~P"

if defined JDK (
  echo Backend Java: !JDK!
  set "JAVA_HOME=!JDK!"
) else (
  echo WARNING: No Java 21/23 JDK found - using the default 'java' on PATH.
  echo          If the backend fails to start, install Temurin/Adoptium JDK 21.
)
echo.

echo Starting BACKEND   -^>  http://localhost:8080
start "Smart IoT - Backend" /D "%ROOT%backend" cmd /k mvnw.cmd spring-boot:run

echo Starting FRONTEND  -^>  http://localhost:5173
start "Smart IoT - Frontend" /D "%ROOT%frontend" cmd /k "npm install && npm run dev"

echo.
echo ------------------------------------------------------------
echo  Two windows opened (backend + frontend).
echo  Open http://localhost:5173 and sign in (admin / admin123).
echo  Close those windows, or press Ctrl+C in them, to stop.
echo ------------------------------------------------------------
echo.
pause
endlocal
