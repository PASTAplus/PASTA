@ECHO OFF
ECHO DataPackageManager-0.1 test suite for use on local Windows 7 development workstation 

SET SERVICE_HOST=http://localhost:8080
SET AUTH_TOKEN_UCARROLL=dWlkPXVjYXJyb2xsLG89TFRFUixkYz1lY29pbmZvcm1hdGljcyxkYz1vcmcqaHR0cHM6Ly9wYXN0YS5sdGVybmV0LmVkdS9hdXRoZW50aWNhdGlvbioyMDAwMDAwMDAwKmF1dGhlbnRpY2F0ZWQ=
SET AUTH_TOKEN_DCOSTA=dWlkPWRjb3N0YSxvPUxURVIsZGM9ZWNvaW5mb3JtYXRpY3MsZGM9b3JnKmh0dHBzOi8vcGFzdGEubHRlcm5ldC5lZHUvYXV0aGVudGljYXRpb24qMjAwMDAwMDAwMCphdXRoZW50aWNhdGVk
SET EML_FILE=test\data\NoneSuchBugCount.xml
REM SET EML_FILE=test\data\NoneSuchBugCountBadData.xml
REM SET EML_FILE=test\data\NoneSuchBugCountBadPackageId.xml
REM SET EML_FILE=test\data\NoneSuchBugCountBadReference.xml
REM SET EML_FILE=test\data\NoneSuchBugCountDuplicateEntity.xml
REM SET EML_FILE=test\data\Level-1-EML.xml
REM SET EML_FILE=test\data\knb-lter-gce.1.9.xml
REM SET EML_FILE=%DOWNLOADS%\ecotrends.8300.1.xml

ECHO DP-1: Create Data Package
curl -i -b auth-token=%AUTH_TOKEN_UCARROLL% -X POST -H "Content-Type: application/xml" -d @%EML_FILE% %SERVICE_HOST%/package/eml
