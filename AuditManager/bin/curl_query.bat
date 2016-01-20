@ECHO OFF
REM SET SERVICE_HOST=http://localhost:8080
REM SET SERVICE_HOST=http://audit.lternet.edu
SET SERVICE_HOST=http://pasta.lternet.edu
SET AUTH_TOKEN_UCARROLL=dWlkPXVjYXJyb2xsLG89TFRFUixkYz1lY29pbmZvcm1hdGljcyxkYz1vcmcqaHR0cHM6Ly9wYXN0YS5sdGVybmV0LmVkdS9hdXRoZW50aWNhdGlvbioyMDAwMDAwMDAwKmF1dGhlbnRpY2F0ZWQ=
SET AUTH_TOKEN_DCOSTA=dWlkPWRjb3N0YSxvPUxURVIsZGM9ZWNvaW5mb3JtYXRpY3MsZGM9b3JnKmh0dHBzOi8vcGFzdGEubHRlcm5ldC5lZHUvYXV0aGVudGljYXRpb24qMjAwMDAwMDAwMCphdXRoZW50aWNhdGVk

REM curl -i -b auth-token=%AUTH_TOKEN_UCARROLL% -G "%SERVICE_HOST%/audit/report?category=warn&user=uid=dcosta,o=LTER,dc=ecoinformatics,dc=org&status=400&fromTime=2013-04-01&toTime=2013-04-20"
REM curl -i -b auth-token=%AUTH_TOKEN_UCARROLL% -G "%SERVICE_HOST%/audit/report/5752
REM curl -i -b auth-token=%AUTH_TOKEN_UCARROLL% -G "%SERVICE_HOST%/audit/report?fromTime=2013-12-09&limit=3&serviceMethod=readMetadata"
REM curl -i -b auth-token=%AUTH_TOKEN_UCARROLL% -G "%SERVICE_HOST%/audit/report?fromTime=2013-12-09&limit=x&serviceMethod=readMetadata"
REM curl -i -b auth-token=%AUTH_TOKEN_UCARROLL% -G "%SERVICE_HOST%/audit/report?limit=3&fromTime=2013-12-09"
REM curl -i -b auth-token=%AUTH_TOKEN_UCARROLL% -G "%SERVICE_HOST%/audit/report?limit=x&serviceMethod=readMetadata"
REM curl -i -b auth-token=%AUTH_TOKEN_UCARROLL% -G "%SERVICE_HOST%/audit/report?limit=100&category=info&serviceMethod=createDataPackage&serviceMethod=uploadDataPackage&fromTime=2014-10-01
REM curl -i -b auth-token=%AUTH_TOKEN_UCARROLL% -G "%SERVICE_HOST%/audit/report?limit=100&category=info&serviceMethod=updateDataPackage&serviceMethod=uploadDataPackage&fromTime=2014-10-01

REM curl -i -u "uid=ucarroll,o=LTER,dc=ecoinformatics,dc=org:S@ltL@ke" -X GET "%SERVICE_HOST%/audit/report?category=info&serviceMethod=createDataPackage&fromTime=2015-09-22"
REM curl -i -u "uid=ucarroll,o=LTER,dc=ecoinformatics,dc=org:S@ltL@ke" -X GET "%SERVICE_HOST%/audit/report?category=info&user=uid=SBC,o=LTER,dc=ecoinformatics,dc=org&serviceMethod=createDataPackage&serviceMethod=updateDataPackage&fromTime=2012-09-01"
curl -i -u "uid=ucarroll,o=LTER,dc=ecoinformatics,dc=org:S@ltL@ke" -X GET "%SERVICE_HOST%/audit/report/?serviceMethod=deleteDataPackage&status=200&toTime=2015-01-31"
