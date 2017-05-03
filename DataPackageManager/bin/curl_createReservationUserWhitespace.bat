@ECHO OFF
echo.
echo Working On: localhost
echo.
echo Remember to provide a password as the command-line argument to this script
SET PASSWORD=%1
SET SERVICE_HOST=http://localhost:8888
REM SET SCOPE=edi
REM curl -i -u uid=LNO,o=LTER,dc=ecoinformatics,dc=org:%PASSWORD% -X POST "%SERVICE_HOST%/package/reservations/eml/%scope%"
SET SCOPE=bogus-scope
curl -i -u "uid=LNO   ,o=LTER,dc=ecoinformatics,dc=org  :%PASSWORD%" -X POST "%SERVICE_HOST%/package/reservations/eml/%scope%"
