@ECHO OFF
echo.
echo Data package changes for: localhost
echo.
SET FROM_DATE=%1
SET SERVICE_HOST=http://localhost:8888
echo "fromDate=2017-01-01T17:00:00&toDate=2017-03-01T17:00:00&scope=knb-lter-hfr"
curl -X GET "%SERVICE_HOST%/package/changes/eml?fromDate=2017-01-01T17:00:00&toDate=2017-03-01T17:00:00&scope=knb-lter-hfr"
echo.
echo "toDate=2017-01-01T17:00:00&scope=edi"
curl -X GET "%SERVICE_HOST%/package/changes/eml?toDate=2017-01-01T17:00:00&scope=edi"
echo.
echo "scope=edi"
curl -X GET "%SERVICE_HOST%/package/changes/eml?scope=edi"
echo.
echo "fromDate=2017-02-01T17:00:00&toDate=2017-02-08T17:00:00"
curl -X GET "%SERVICE_HOST%/package/changes/eml?fromDate=2017-02-01T17:00:00&toDate=2017-02-08T17:00:00"
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
