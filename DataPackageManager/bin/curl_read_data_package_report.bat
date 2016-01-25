@ECHO OFF
ECHO DataPackageManager-0.1 test suite for use on local Windows 7 development workstation 

REM SET SERVICE_HOST=http://localhost:8080
SET SERVICE_HOST=http://localhost:8888
SET AUTH_TOKEN_UCARROLL=dWlkPXVjYXJyb2xsLG89TFRFUixkYz1lY29pbmZvcm1hdGljcyxkYz1vcmcqaHR0cHM6Ly9wYXN0YS5sdGVybmV0LmVkdS9hdXRoZW50aWNhdGlvbioyMDAwMDAwMDAwKmF1dGhlbnRpY2F0ZWQ=
SET AUTH_TOKEN_DCOSTA=dWlkPWRjb3N0YSxvPUxURVIsZGM9ZWNvaW5mb3JtYXRpY3MsZGM9b3JnKmh0dHBzOi8vcGFzdGEubHRlcm5ldC5lZHUvYXV0aGVudGljYXRpb24qMjAwMDAwMDAwMCphdXRoZW50aWNhdGVk
SET SCOPE=knb-lter-nin
SET IDENTIFIER=1
SET REVISION=1

REM curl -i -b auth-token=%AUTH_TOKEN_UCARROLL% -G %SERVICE_HOST%/package/report/eml/%SCOPE%/%IDENTIFIER%/%REVISION%
REM curl -i -b auth-token=%AUTH_TOKEN_UCARROLL% -H "Accept: text/html" -G %SERVICE_HOST%/package/report/eml/%SCOPE%/%IDENTIFIER%/%REVISION%
curl -i -G -H "Accept: text/html" -G %SERVICE_HOST%/package/report/eml/%SCOPE%/%IDENTIFIER%/%REVISION%