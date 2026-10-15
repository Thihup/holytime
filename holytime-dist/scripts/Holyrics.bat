set J2D_UISCALE=96
cd /d "%~dp0.."

set AOT_FILE=holytime\holyrics.aot

if exist %AOT_FILE% (
    holytime\bin\java -XX:AOTCache=%AOT_FILE% -jar Holyrics.exe
) else (
    holytime\bin\java -XX:AOTCacheOutput=%AOT_FILE% -jar Holyrics.exe
)
pause