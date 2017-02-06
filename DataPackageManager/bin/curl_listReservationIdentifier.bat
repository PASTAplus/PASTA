@ECHO OFF
echo.
echo edi identifiers reserved on: localhost
SET SERVICE_HOST=http://localhost:8888
SET SCOPE=edi
curl -X GET "%SERVICE_HOST%/package/reservations/eml/%scope%"
echo.
echo.
echo edi identifiers reserved on: pasta-d
SET SERVICE_HOST=http://pasta-d.lternet.edu
curl -X GET "%SERVICE_HOST%/package/reservations/eml/%scope%"
echo.
echo.
echo edi identifiers reserved on: pasta-s
SET SERVICE_HOST=http://pasta-s.lternet.edu
curl -X GET "%SERVICE_HOST%/package/reservations/eml/%scope%"
echo.
echo.
REM echo edi identifiers reserved on: pasta
SET SERVICE_HOST=http://pasta.lternet.edu
REM curl -X GET "%SERVICE_HOST%/package/reservations/eml/%scope%"
