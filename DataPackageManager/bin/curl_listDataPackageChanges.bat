@ECHO OFF
echo.
echo Reservations for: localhost
echo.
SET SERVICE_HOST=http://localhost:8888
curl -X GET "%SERVICE_HOST%/package/changes/eml?fromDate=2017-02-06T17:00:00"
echo.
echo Reservations for: pasta-d
echo.
SET SERVICE_HOST=http://pasta-d.lternet.edu
REM curl -X GET "%SERVICE_HOST%/package/changes/eml?fromDate=2017-01-01"
echo.
echo Reservations for: pasta-s
echo.
SET SERVICE_HOST=http://pasta-s.lternet.edu
REM curl -X GET "%SERVICE_HOST%/package/changes/eml?fromDate=2017-01-01"
echo.
echo Reservations for: pasta
echo.
SET SERVICE_HOST=http://pasta.lternet.edu
REM curl -X GET "%SERVICE_HOST%/package/changes/eml?fromDate=2017-01-01"
