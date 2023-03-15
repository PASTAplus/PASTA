@ECHO OFF
echo.
echo Working On: localhost
echo.
SET SERVICE_HOST=http://localhost:8888
curl -X GET "%SERVICE_HOST%/package/workingon/eml/"
echo.
echo Working On: pasta-d
echo.
SET SERVICE_HOST=http://pasta-d.lternet.edu
curl -X GET "%SERVICE_HOST%/package/workingon/eml/"
echo.
echo Working On: pasta-s
echo.
SET SERVICE_HOST=http://pasta-s.lternet.edu
curl -X GET "%SERVICE_HOST%/package/workingon/eml/"
echo.
echo Working On: pasta
echo.
SET SERVICE_HOST=http://pasta.lternet.edu
curl -X GET "%SERVICE_HOST%/package/workingon/eml/"
