set J2D_UISCALE=96
cd /d "%~dp0.."

set AOT_FILE=holytime\holyrics.aot

if exist %AOT_FILE% (
    holytime\bin\javaw -XX:AOTCache=%AOT_FILE% -jar Holyrics.exe
) else (
    holytime\bin\javaw -XX:AOTCacheOutput=%AOT_FILE% -jar Holyrics.exe
)
pause