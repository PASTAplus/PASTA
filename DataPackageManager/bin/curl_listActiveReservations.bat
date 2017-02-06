@ECHO OFF
echo.
echo Reservations for: localhost
echo.
SET SERVICE_HOST=http://localhost:8888
SET SCOPE=edi
curl -X GET "%SERVICE_HOST%/package/reservations/eml"
echo.
echo Reservations for: pasta-d
echo.
SET SERVICE_HOST=http://pasta-d.lternet.edu
curl -X GET "%SERVICE_HOST%/package/reservations/eml"
echo.
echo Reservations for: pasta-s
echo.
SET SERVICE_HOST=http://pasta-s.lternet.edu
curl -X GET "%SERVICE_HOST%/package/reservations/eml"
echo.
REM echo Reservations for: pasta
echo.
SET SERVICE_HOST=http://pasta.lternet.edu
REM curl -X GET "%SERVICE_HOST%/package/reservations/eml"
