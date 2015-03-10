@ECHO OFF
REM SET SERVICE_HOST=http://localhost:8080
SET SERVICE_HOST=http://audit-d.lternet.edu:8080
SET AUTH_TOKEN_UCARROLL=dWlkPXVjYXJyb2xsLG89TFRFUixkYz1lY29pbmZvcm1hdGljcyxkYz1vcmcqaHR0cHM6Ly9wYXN0YS5sdGVybmV0LmVkdS9hdXRoZW50aWNhdGlvbioyMDAwMDAwMDAwKmF1dGhlbnRpY2F0ZWQ=
SET AUTH_TOKEN_DCOSTA=dWlkPWRjb3N0YSxvPUxURVIsZGM9ZWNvaW5mb3JtYXRpY3MsZGM9b3JnKmh0dHBzOi8vcGFzdGEubHRlcm5ldC5lZHUvYXV0aGVudGljYXRpb24qMjAwMDAwMDAwMCphdXRoZW50aWNhdGVk

REM curl -i -b auth-token=%AUTH_TOKEN_UCARROLL% -G "%SERVICE_HOST%/audit/report?category=warn&user=uid=dcosta,o=LTER,dc=ecoinformatics,dc=org&status=400&fromTime=2013-04-01&toTime=2013-04-20"
REM curl -i -b auth-token=%AUTH_TOKEN_UCARROLL% -G "%SERVICE_HOST%/audit/report/5752
REM curl -i -b auth-token=%AUTH_TOKEN_UCARROLL% -G "%SERVICE_HOST%/audit/report?fromTime=2013-12-09&limit=3&serviceMethod=readMetadata"
REM curl -i -b auth-token=%AUTH_TOKEN_UCARROLL% -G "%SERVICE_HOST%/audit/report?fromTime=2013-12-09&limit=x&serviceMethod=readMetadata"
REM curl -i -b auth-token=%AUTH_TOKEN_UCARROLL% -G "%SERVICE_HOST%/audit/report?limit=3&fromTime=2013-12-09"
REM curl -i -b auth-token=%AUTH_TOKEN_UCARROLL% -G "%SERVICE_HOST%/audit/report?limit=x&serviceMethod=readMetadata"
curl -i -b auth-token=%AUTH_TOKEN_UCARROLL% -G "%SERVICE_HOST%/audit/report?limit=100&category=info&serviceMethod=createDataPackage&serviceMethod=uploadDataPackage&fromTime=2014-10-01
curl -i -b auth-token=%AUTH_TOKEN_UCARROLL% -G "%SERVICE_HOST%/audit/report?limit=100&category=info&serviceMethod=updateDataPackage&serviceMethod=uploadDataPackage&fromTime=2014-10-01
