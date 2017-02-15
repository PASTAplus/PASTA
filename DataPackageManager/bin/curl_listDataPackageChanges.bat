@ECHO OFF
echo.
echo Data package changes for: localhost
echo.
SET FROM_DATE=%1
SET SERVICE_HOST=http://localhost:8888
curl -X GET "%SERVICE_HOST%/package/changes/eml?fromDate=%FROM_DATE%"
echo.
echo Data package changes for: pasta-d
echo.
SET SERVICE_HOST=http://pasta-d.lternet.edu
REM curl -X GET "%SERVICE_HOST%/package/changes/eml?fromDate=%FROM_DATE%"
echo.
echo Data package changes for: pasta-s
echo.
SET SERVICE_HOST=http://pasta-s.lternet.edu
REM curl -X GET "%SERVICE_HOST%/package/changes/eml?fromDate=%FROM_DATE%"
echo.
echo Data package changes for: pasta
echo.
SET SERVICE_HOST=http://pasta.lternet.edu
REM curl -X GET "%SERVICE_HOST%/package/changes/eml?fromDate=%FROM_DATE%"
