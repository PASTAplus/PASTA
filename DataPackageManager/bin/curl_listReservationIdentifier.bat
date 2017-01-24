@ECHO OFF
echo.
echo Working On: localhost
echo.
SET SERVICE_HOST=http://localhost:8888
SET SCOPE=edi
curl -X GET "%SERVICE_HOST%/package/reservations/eml/%scope%"
echo.
REM echo Working On: pasta-d
echo.
SET SERVICE_HOST=http://pasta-d.lternet.edu
REM curl -X GET "%SERVICE_HOST%/package/workingon/eml/%scope%"
echo.
REM echo Working On: pasta-s
echo.
SET SERVICE_HOST=http://pasta-s.lternet.edu
REM curl -X GET "%SERVICE_HOST%/package/workingon/eml/%scope%"
echo.
REM echo Working On: pasta
echo.
SET SERVICE_HOST=http://pasta.lternet.edu
REM curl -X GET "%SERVICE_HOST%/package/workingon/eml/%scope%"
