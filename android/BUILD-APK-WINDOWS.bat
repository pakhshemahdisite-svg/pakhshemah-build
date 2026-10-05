@echo off
setlocal EnableExtensions
chcp 65001 >nul

set "ROOT=%~dp0"
cd /d "%ROOT%"

echo ================================================
echo   Pakhsh Mahdi - Android APK Builder
echo ================================================

if not defined JAVA_HOME (
  if exist "%ProgramFiles%\Android\Android Studio\jbr\bin\java.exe" set "JAVA_HOME=%ProgramFiles%\Android\Android Studio\jbr"
)

if not defined JAVA_HOME (
  echo [ERROR] JAVA_HOME is not set and Android Studio JBR was not found.
  echo Install Android Studio, then run this file again.
  pause
  exit /b 1
)

set "PATH=%JAVA_HOME%\bin;%PATH%"

if not defined ANDROID_HOME (
  if exist "%LOCALAPPDATA%\Android\Sdk" set "ANDROID_HOME=%LOCALAPPDATA%\Android\Sdk"
)
if not defined ANDROID_SDK_ROOT set "ANDROID_SDK_ROOT=%ANDROID_HOME%"

if not exist "%ANDROID_HOME%\platforms\android-35\android.jar" (
  echo Android SDK Platform 35 was not found.
  if exist "%ANDROID_HOME%\cmdline-tools\latest\bin\sdkmanager.bat" (
    echo Installing required Android SDK packages...
    call "%ANDROID_HOME%\cmdline-tools\latest\bin\sdkmanager.bat" "platforms;android-35" "build-tools;35.0.0" "platform-tools"
    if errorlevel 1 goto :fail
  ) else (
    echo [ERROR] Android SDK Platform 35 is missing.
    echo Open Android Studio ^> SDK Manager and install:
    echo   - Android SDK Platform 35
    echo   - Android SDK Build-Tools 35.0.0
    pause
    exit /b 1
  )
)

set "TOOLS=%ROOT%.tools"
set "GRADLE_HOME=%TOOLS%\gradle-8.9"
set "GRADLE_ZIP=%TOOLS%\gradle-8.9-bin.zip"

if not exist "%GRADLE_HOME%\bin\gradle.bat" (
  echo Gradle 8.9 not found. Downloading it once...
  if not exist "%TOOLS%" mkdir "%TOOLS%"
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -UseBasicParsing 'https://services.gradle.org/distributions/gradle-8.9-bin.zip' -OutFile '%GRADLE_ZIP%'"
  if errorlevel 1 goto :fail
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -Path '%GRADLE_ZIP%' -DestinationPath '%TOOLS%' -Force"
  if errorlevel 1 goto :fail
)

echo.
echo Building installable Debug APK...
call "%GRADLE_HOME%\bin\gradle.bat" --no-daemon clean assembleDebug
if errorlevel 1 goto :fail

set "APK=%ROOT%app\build\outputs\apk\debug\app-debug.apk"
set "OUT=%ROOT%PakhshMahdi-v0.1.1-debug.apk"
if not exist "%APK%" goto :fail
copy /Y "%APK%" "%OUT%" >nul

echo.
echo ================================================
echo BUILD SUCCESSFUL
echo APK: %OUT%
echo ================================================
echo You can copy this APK to your Android phone and install it.
pause
exit /b 0

:fail
echo.
echo [ERROR] APK build failed. Copy the error text and send it to ChatGPT.
pause
exit /b 1
