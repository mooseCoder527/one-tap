@echo off
setlocal
if exist gradlew.bat (
  call gradlew.bat desktop:run
  exit /b %errorlevel%
)
where gradle >nul 2>nul
if %errorlevel%==0 (
  call gradle desktop:run
  exit /b %errorlevel%
)
echo Gradle is required to run this project because the wrapper jar could not be vendored in the offline assembly environment.
exit /b 1
