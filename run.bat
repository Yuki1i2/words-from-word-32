@echo off
cd /d "%~dp0"
java -jar Words.jar
if errorlevel 1 pause
