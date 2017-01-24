@ECHO OFF
echo.
echo Working On: localhost
echo.
SET PASSWORD=%1
SET SERVICE_HOST=http://localhost:8888
SET SCOPE=edi
curl -i -u uid=LNO,o=LTER,dc=ecoinformatics,dc=org:%PASSWORD% -X POST "%SERVICE_HOST%/package/reservations/eml/%scope%"
